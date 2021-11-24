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

import hudson.Extension;
import hudson.util.FormValidation;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
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
 * Decorates a {@link SCMSource} with a {@link SCMHeadPrefilter} that excludes
 * {@link SCMHead} instances with names that do not match a user supplied
 * regular expression.
 *
 * @since 0.1
 */
public class RegexSCMPRDestinationFilterTrait extends SCMSourceTrait {

    /**
     * The pull request destination branch regular expression.
     */
    private final String prDestinationRegex;

    /**
     * The compiled pull request destination branch {@link Pattern}.
     */
    private transient Pattern prDestinationPattern;

    /**
     * Stapler constructor.
     *
     * @param prDestinationRegex The pull request destination branch regular
     *                           expression
     */
    @DataBoundConstructor
    public RegexSCMPRDestinationFilterTrait(String prDestinationRegex) {
        prDestinationPattern = Pattern.compile(prDestinationRegex);
        this.prDestinationRegex = prDestinationRegex;
    }

    /**
     * Gets the pull request destination branch regular expression.
     *
     * @return the pull request destination branch branch regular expression.
     */
    public String getPrDestinationRegex() {
        return prDestinationRegex;
    }

    /**
     * Gets the compiled pull request destination branch pattern.
     *
     * @return the compiled pull request destination branch pattern.
     */
    public Pattern getPrDestinationPattern() {
        if (prDestinationPattern == null) {
            // idempotent
            prDestinationPattern = Pattern.compile(prDestinationRegex);
        }
        return prDestinationPattern;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        context.withPrefilter(new SCMHeadPrefilter() {
            @Override
            public boolean isExcluded(SCMSource source, SCMHead head) {
                if (head instanceof ChangeRequestSCMHead) {
                    // Change request destined to targetName branches
                    String targetName = ((ChangeRequestSCMHead) head).getTarget().getName();
                    return !getPrDestinationPattern().matcher(targetName).matches();
                }

                return false;
            }
        });
    }

    /**
     * Our descriptor.
     */
    @Symbol("RegexSCMPRDestinationFilter")
    @Extension
    @Selection
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.RegexSCMPRDestinationFilterTrait_DisplayName();
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
