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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.mixin.TagSCMHead;
import jenkins.scm.api.trait.SCMHeadPrefilter;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.trait.Selection;

/**
 * Decorates a {@link SCMSource} with a {@link SCMHeadPrefilter} that excludes
 * {@link SCMHead} instances with names that do not match a user supplied
 * regular expression.
 *
 * @since 0.1
 */
public class RegexSCMTagFilterTrait extends SCMSourceTrait {

    /**
     * The tag regular expression.
     */
    private final String tagRegex;

    /**
     * The compiled tag {@link Pattern}.
     */
    private transient Pattern tagPattern;

    /**
     * Stapler constructor.
     * 
     * @param tagRegex The tag regular expression
     */
    @DataBoundConstructor
    public RegexSCMTagFilterTrait(String tagRegex) {
        this.tagRegex = tagRegex;
        this.tagPattern = Pattern.compile(tagRegex);
    }

    /**
     * Gets the tag regular expression.
     *
     * @return the tag regular expression.
     */
    public String getTagRegex() {
        return tagRegex;
    }

    /**
     * Gets the compiled tag pattern.
     *
     * @return the compiled tag pattern.
     */
    public Pattern getTagPattern() {
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
            public boolean isExcluded(SCMSource source, SCMHead head) {
                if (head instanceof TagSCMHead) {
                    // tag
                    return !getTagPattern().matcher(head.getName()).matches();
                }

                return false;
            }
        });
    }

    @Symbol("RegexSCMTagFilter")
    @Extension
    @Selection
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.RegexSCMTagFilterTrait_DisplayName();
        }

        /**
         * Form validation for the regular expression.
         *
         * @param value the regular expression.
         * @return the validation results.
         */
        @Restricted(NoExternalUse.class)
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
