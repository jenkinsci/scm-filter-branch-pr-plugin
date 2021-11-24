/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
 * Copyright (c) 2017-2020, Sam Gleske - https://github.com/samrocketman
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
import jenkins.scm.api.mixin.ChangeRequestSCMHead2;
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
 * @since 0.5
 */
public class WildcardSCMPROriginFilterTrait extends SCMSourceTrait {

    /**
     * The PR include rules based on origin branch.
     */
    private final String prOriginIncludes;

    /**
     * The PR exclude rules based on origin branch.
     */
    private final String prOriginExcludes;

    /**
     * Stapler constructor.
     *
     * @param prOriginIncludes the pull request include rules based on origin branch
     * @param prOriginExcludes the pull request exclude rules based on origin branch
     */
    @DataBoundConstructor
    public WildcardSCMPROriginFilterTrait(String prOriginIncludes, String prOriginExcludes) {
        this.prOriginIncludes = StringUtils.defaultIfBlank(prOriginIncludes, "");
        this.prOriginExcludes = StringUtils.defaultIfBlank(prOriginExcludes, "");
    }

    /**
     * Returns the pull request include rules based on origin branch.
     *
     * @return the pull request include rules.
     */
    public String getPrOriginIncludes() {
        return prOriginIncludes;
    }

    /**
     * Returns the pull request exclude rules based on origin branch.
     *
     * @return the pull request exclude rules.
     */
    public String getPrOriginExcludes() {
        return prOriginExcludes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        context.withPrefilter(new SCMHeadPrefilter() {
            @Override
            public boolean isExcluded(SCMSource request, SCMHead head) {
                if (head instanceof ChangeRequestSCMHead2) {
                    // change request originating from originName branches
                    String originName = ((ChangeRequestSCMHead2) head).getOriginName();
                    return !Pattern.matches(getPattern(getPrOriginIncludes()), originName)
                            || Pattern.matches(getPattern(getPrOriginExcludes()), originName);
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
    @Symbol("WildcardSCMPROriginFilter")
    @Extension
    @Selection
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.WildcardSCMPROriginFilterTrait_DisplayName();
        }
    }
}
