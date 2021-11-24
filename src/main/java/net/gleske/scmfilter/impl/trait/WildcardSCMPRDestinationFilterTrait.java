/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
 * Copyright (c) 2017-2018, Sam Gleske - https://github.com/samrocketman
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
import java.util.regex.Pattern;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import jenkins.scm.api.mixin.TagSCMHead;
import jenkins.scm.api.trait.SCMHeadPrefilter;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.trait.Selection;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Decorates a {@link SCMSource} with a {@link SCMHeadPrefilter} that filters
 * {@link SCMHead} instances based on matching wildcard include/exclude rules.
 *
 * @since 0.1
 */
public class WildcardSCMPRDestinationFilterTrait extends SCMSourceTrait {

    /**
     * The PR include rules based on destination branch.
     */
    private final String prDestinationIncludes;

    /**
     * The PR exclude rules based on destination branch.
     */
    private final String prDestinationExcludes;

    /**
     * Stapler constructor.
     *
     * @param prDestinationIncludes the pull request include rules based on
     *                              destination branch
     * @param prDestinationExcludes the pull request exclude rules based on
     *                              destination branch
     */
    @DataBoundConstructor
    public WildcardSCMPRDestinationFilterTrait(String prDestinationIncludes, String prDestinationExcludes) {
        this.prDestinationIncludes = StringUtils.defaultIfBlank(prDestinationIncludes, "");
        this.prDestinationExcludes = StringUtils.defaultIfBlank(prDestinationExcludes, "");
    }

    /**
     * Returns the pull request exclude rules based on destination branch.
     *
     * @return the pull request exclude rules.
     */
    public String getPrDestinationIncludes() {
        return prDestinationIncludes;
    }

    /**
     * Returns the pull request exclude rules based on destination branch.
     *
     * @return the pull request exclude rules.
     */
    public String getPrDestinationExcludes() {
        return prDestinationExcludes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        context.withPrefilter(new SCMHeadPrefilter() {
            @Override
            public boolean isExcluded(SCMSource request, SCMHead head) {
                if (head instanceof ChangeRequestSCMHead) {
                    // change request destined to targetName branches
                    String targetName = ((ChangeRequestSCMHead) head).getTarget().getName();
                    return !Pattern.matches(getPattern(getPrDestinationIncludes()), targetName)
                            || Pattern.matches(getPattern(getPrDestinationExcludes()), targetName);
                }

                return false;
            }
        });
    }

    /**
     * Returns the pattern corresponding to the branches containing wildcards.
     *
     * @param branches the names of branches to create a pattern for
     * @return pattern corresponding to the branches containing wildcards
     */
    private String getPattern(String branches) {
        StringBuilder quotedBranches = new StringBuilder();
        for (String wildcard : branches.split(" ")) {
            StringBuilder quotedBranch = new StringBuilder();
            for (String branch : wildcard.split("(?=[*])|(?<=[*])")) {
                if (branch.equals("*")) {
                    quotedBranch.append(".*");
                } else if (!branch.isEmpty()) {
                    quotedBranch.append(Pattern.quote(branch));
                }
            }
            if (quotedBranches.length() > 0) {
                quotedBranches.append("|");
            }
            quotedBranches.append(quotedBranch);
        }
        return quotedBranches.toString();
    }

    /**
     * Our descriptor.
     */
    @Symbol("WildcardSCMPRDestinationFilter")
    @Extension
    @Selection
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.WildcardSCMPRDestinationFilter_DisplayName();
        }
    }
}
