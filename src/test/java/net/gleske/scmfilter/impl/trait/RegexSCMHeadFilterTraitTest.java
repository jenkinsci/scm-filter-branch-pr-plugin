package net.gleske.scmfilter.impl.trait;

import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@WithJenkins
public class RegexSCMHeadFilterTraitTest  {

    @Test
    void shouldCreateTrait(JenkinsRule jenkinsRule) {
        RegexSCMHeadFilterTrait trait = new RegexSCMHeadFilterTrait(".*", ".*");
        assertThat(trait.getRegex(), is(".*"));
        assertThat(trait.getTagRegex(), is(".*"));
    }
    
}
