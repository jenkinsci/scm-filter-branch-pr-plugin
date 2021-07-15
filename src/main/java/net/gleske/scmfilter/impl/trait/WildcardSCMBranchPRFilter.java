package net.gleske.scmfilter.impl.trait;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import java.util.regex.Pattern;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import jenkins.scm.api.mixin.ChangeRequestSCMHead2;
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
 * Decorates a {@link SCMSource} with a {@link SCMHeadPrefilter} that filters {@link SCMHead} instances based on
 * matching wildcard include/exclude rules.
 *
 * @since 0.1
 */
public class WildcardSCMBranchPRFilter extends SCMSourceTrait {

    /**
     * The branch include rules.
     */
    @NonNull
    private final String includes;

    /**
     * The branch exclude rules.
     */
    @NonNull
    private final String excludes;

    /**
     * The tag include rules.
     */
    @NonNull
    private final String tagIncludes;

    /**
     * The tag exclude rules.
     */
    @NonNull
    private final String tagExcludes;

    /**
     * The PR destination branch rules.
     */
    @NonNull
    private final String prDestination;

    /**
     * Stapler constructor.
     *
     * @param includes the branch include rules.
     * @param excludes the branch exclude rules.
     * @param tagIncludes the tag include rules.
     * @param tagExcludes the tag exclude rules.
     * @param prDestination the tag exclude rules.
     */
    @DataBoundConstructor
    public WildcardSCMBranchPRFilter(@CheckForNull String includes, String excludes, String tagIncludes, String tagExcludes, String prDestination) {
        this.includes = StringUtils.defaultIfBlank(includes, "*");
        this.excludes = StringUtils.defaultIfBlank(excludes, "");
        this.tagIncludes = StringUtils.defaultIfBlank(tagIncludes, "");
        this.tagExcludes = StringUtils.defaultIfBlank(tagExcludes, "");
        this.prDestination = StringUtils.defaultIfBlank(prDestination, "");
    }

    /**
     * Deprecated constructor kept around for compatibility and migration.
     *
     * @param includes the include rules.
     * @param excludes the exclude rules.
     */
    @Deprecated
    public WildcardSCMBranchPRFilter(@CheckForNull String includes, String excludes) {
        this.includes = StringUtils.defaultIfBlank(includes, "*");
        this.excludes = StringUtils.defaultIfBlank(excludes, "");
        this.tagIncludes = "";
        this.tagExcludes = "*";
        this.prDestination = "development";
    }

    /**
     * Returns the branch include rules.
     *
     * @return the branch include rules.
     */
    public String getIncludes() {
        return includes;
    }

    /**
     * Returns the branch exclude rules.
     *
     * @return the branch exclude rules.
     */
    public String getExcludes() {
        return excludes;
    }

    /**
     * Returns the tag include rules.
     *
     * @return the tag include rules.
     */
    public String getTagIncludes() {
        return tagIncludes;
    }

    /**
     * Returns the tag exclude rules.
     *
     * @return the tag exclude rules.
     */
    public String getTagExcludes() {
        return tagExcludes;
    }

    /**
     * Returns the pr destination rules
     * 
     * @return the pr destination rules
     */
    public String getPrDestination() {
        return prDestination;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        context.withPrefilter(new SCMHeadPrefilter() {
            @Override
            public boolean isExcluded(@NonNull SCMSource request, @NonNull SCMHead head) {
                if (head instanceof ChangeRequestSCMHead) {
                    head = ((ChangeRequestSCMHead)head).getTarget();
                    return !Pattern.matches(getPattern(getPrDestination()), head.getName());
                }
                else if(head instanceof TagSCMHead) {
                    return !Pattern.matches(getPattern(getTagIncludes()), head.getName())
                         || Pattern.matches(getPattern(getTagExcludes()), head.getName());
                } else {
                    return !Pattern.matches(getPattern(getIncludes()), head.getName())
                         || Pattern.matches(getPattern(getExcludes()), head.getName());
                }
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
    @Symbol("headWildcardFilterWithPR")
    @Extension
    @Selection
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.WildcardSCMBranchPRFilter_DisplayName();
        }
    }
}
