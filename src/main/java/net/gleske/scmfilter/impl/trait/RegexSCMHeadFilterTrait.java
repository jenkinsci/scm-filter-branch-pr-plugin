/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
 * Copyright (c) 2017, Sam Gleske - https://github.com/samrocketman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.gleske.scmfilter.impl.trait;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.FormValidation;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import jenkins.scm.api.mixin.TagSCMHead;
import jenkins.scm.api.trait.SCMHeadPrefilter;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.trait.Selection;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Decorates a {@link SCMSource} with a {@link SCMHeadPrefilter} that excludes {@link SCMHead} instances with names that
 * do not match a user supplied regular expression.
 *
 * @since 0.1
 */
public class RegexSCMHeadFilterTrait extends SCMSourceTrait {

    /**
     * The branch regular expression.
     */
    @NonNull
    private final String regex;

    /**
     * The tag regular expression.
     */
    @NonNull
    private final String tagRegex;

    /**
     * The compiled branch {@link Pattern}.
     */
    @CheckForNull
    private transient Pattern pattern;

    /**
     * The compiled tag {@link Pattern}.
     */
    @CheckForNull
    private transient Pattern tagPattern;

    /**
     * Stapler constructor.
     *
     * @param regex the branch regular expression.
     * @param tagRegex the tag regular expression.
     */
    @DataBoundConstructor
    public RegexSCMHeadFilterTrait(@NonNull String regex, @NonNull String tagRegex) {
        pattern = Pattern.compile(regex);
        this.regex = regex;
        tagPattern = Pattern.compile(tagRegex);
        this.tagRegex = tagRegex;
    }

    /**
     * Deprecated constructor kept around for compatibility and migration.
     *
     * @param regex the regular expression.
     */
    @Deprecated
    public RegexSCMHeadFilterTrait(@NonNull String regex) {
        pattern = Pattern.compile(regex);
        this.regex = regex;
        tagPattern = Pattern.compile("(?!.*)");
        this.tagRegex = "(?!.*)";
    }

    /**
     * Gets the branch regular expression.
     *
     * @return the branch regular expression.
     */
    @NonNull
    public String getRegex() {
        return regex;
    }

    /**
     * Gets the tag regular expression.
     *
     * @return the tag regular expression.
     */
    @NonNull
    public String getTagRegex() {
        return tagRegex;
    }

    /**
     * Gets the compiled branch {@link Pattern}.
     *
     * @return the compiled branch {@link Pattern}.
     */
    @NonNull
    private Pattern getPattern() {
        if (pattern == null) {
            // idempotent
            pattern = Pattern.compile(regex);
        }
        return pattern;
    }

    /**
     * Gets the compiled tag {@link Pattern}.
     *
     * @return the compiled tag {@link Pattern}.
     */
    @NonNull
    private Pattern getTagPattern() {
        if (tagPattern == null) {
            // idempotent
            tagPattern = Pattern.compile(tagRegex);
        }
        return tagPattern;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        context.withPrefilter(new SCMHeadPrefilter() {
            @Override
            public boolean isExcluded(@NonNull SCMSource source, @NonNull SCMHead head) {
                if (head instanceof ChangeRequestSCMHead) {
                    head = ((ChangeRequestSCMHead)head).getTarget();
                }

                if (head instanceof TagSCMHead) {
                    return !getTagPattern().matcher(head.getName()).matches();
                } else {
                    return !getPattern().matcher(head.getName()).matches();
                }
            }
        });
    }

    /**
     * Our descriptor.
     */
    @Symbol("headRegexFilterWithPR")
    @Extension
    @Selection
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.RegexSCMHeadFilterTrait_DisplayName();
        }

        /**
         * Form validation for the regular expression.
         *
         * @param value the regular expression.
         * @return the validation results.
         */
        @Restricted(NoExternalUse.class) // stapler
        public FormValidation doCheckRegex(@QueryParameter String value) {
            try {
                Pattern.compile(value);
                return FormValidation.ok();
            } catch (PatternSyntaxException e) {
                return FormValidation.error(e.getMessage());
            }
        }
    }
}
