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
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import jenkins.scm.api.mixin.ChangeRequestSCMHead2;
import jenkins.scm.api.mixin.TagSCMHead;
import jenkins.scm.api.trait.SCMHeadPrefilter;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.trait.Selection;

/**
 * Decorates a {@link SCMSource} with a {@link SCMHeadPrefilter} that excludes {@link SCMHead} instances with names that
 * do not match a user supplied regular expression.
 *
 * @since 0.1
 */
public class RegexSCMFilterTrait extends SCMSourceTrait {

    /**
     * The branch regular expression.
     */
    private final String branchRegex;

    /**
     * The pull request origin branch regular expression.
     */
    private final String prOriginRegex;

    /**
     * The pull request destination branch regular expression.
     */
    private final String prDestinationRegex;

    /**
     * The tag regular expression.
     */
    private final String tagRegex;

    /**
     * The compiled branch {@link Pattern}.
     */
    private transient Pattern branchPattern;

    /**
     * The compiled pull request origin branch {@link Pattern}.
     */
    private transient Pattern prOriginPattern;

    /**
     * The compiled pull request destination branch {@link Pattern}.
     */
    private transient Pattern prDestinationPattern;

    /**
     * The compiled tag {@link Pattern}.
     */
    private transient Pattern tagPattern;

    /**
     * Stapler constructor.
     * 
     * @param branchRegex The branch regular expression
     * @param prOriginRegex The pull request origin branch regular expression
     * @param prDestinationRegex The pull request destination branch regular expression
     * @param tagRegex The tag regular expression
     */
    @DataBoundConstructor
    public RegexSCMFilterTrait(String branchRegex, String prOriginRegex, String prDestinationRegex, String tagRegex) {
        this.branchRegex = branchRegex;
        this.branchPattern = Pattern.compile(branchRegex);
        this.prOriginRegex = prOriginRegex;
        this.prOriginPattern = Pattern.compile(prOriginRegex);
        this.prDestinationRegex = prDestinationRegex;
        this.prDestinationPattern = Pattern.compile(prDestinationRegex);
        this.tagRegex = tagRegex;
        this.tagPattern = Pattern.compile(tagRegex);
    }
    
    /**
     * Gets the branch regular expression.
     *
     * @return the branch regular expression.
     */
    public String getBranchRegex() {
        return branchRegex;
    }
    
    /**
     * Gets the pull request origin branch regular expression.
     *
     * @return the pull request origin branch regular expression.
     */
    public String getPrOriginRegex() {
        return prOriginRegex;
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
     * Gets the tag regular expression.
     *
     * @return the tag regular expression.
     */
    public String getTagRegex() {
        return tagRegex;
    }

    /**
     * Gets the compiled branch pattern.
     *
     * @return the compiled branch pattern.
     */
    public Pattern getBranchPattern() {
        if (branchPattern == null) {
            // idempotent
            branchPattern = Pattern.compile(branchRegex);
        }
        return branchPattern;
    }

    /**
     * Gets the compiled pull request origin branch pattern.
     *
     * @return the compiled pull request origin branch pattern.
     */
    public Pattern getPrOriginPattern() {
        if (prOriginPattern == null) {
            // idempotent
            prOriginPattern = Pattern.compile(prOriginRegex);
        }
        return prOriginPattern;
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
                if (head instanceof ChangeRequestSCMHead) {
                    // Change request destined to targetName branches
                    String targetName = ((ChangeRequestSCMHead) head).getTarget().getName();
                    return !getPrDestinationPattern().matcher(targetName).matches();
                }
                
                if (head instanceof ChangeRequestSCMHead2) {
                    // Change request originating from origin branches
                    String origin = ((ChangeRequestSCMHead2) head).getOriginName();
                    return !getPrOriginPattern().matcher(origin).matches();
                }

                if (head instanceof TagSCMHead) {
                    // tag
                    return !getTagPattern().matcher(head.getName()).matches();
                } else {
                    // branch
                    return !getBranchPattern().matcher(head.getName()).matches();
                }
            }
        });
    }

    @Symbol("RegexSCMFilter")
    @Extension
    @Selection
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.RegexSCMFilterTrait_DisplayName();
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
