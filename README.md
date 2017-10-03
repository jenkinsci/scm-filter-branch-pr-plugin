# SCM Branch PR Filter Plugin

This is a Jenkins plugin.

* Documentation in the [Jenkins wiki][wiki].
* File issues in [JIRA][jira].

This plugin will filter branches in the [Pipeline Multibranch
Plugin][pmb-plugin] similar to the [SCM API Plugin][sa-plugin].  However, pull
requests where the destination branch matches a filtered branch will also be
built.  No the need to define an additional filter for pull requests if using
this plugin.

This plugin was created to address [JENKINS-47091][JENKINS-47091] where
[Pipeline Multibranch Plugin][pmb-plugin] does not build GitHub pull requests
destined for a filtered branch.  For documentation on how to configure this
plugin refer to the wiki article [SCM Branch PR Filter Plugin][wiki].

# Release Notes

For release notes, please refer to the [CHANGELOG.md](CHANGELOG.md).

# License

```
The MIT License

Copyright (c) 2017, Sam Gleske - https://github.com/samrocketman
Copyright (c) 2017, CloudBees, Inc.
```

See [LICENSE.txt](LICENSE.txt)

[JENKINS-47091]: https://issues.jenkins-ci.org/browse/JENKINS-47091
[jira]: https://issues.jenkins-ci.org/issues/?jql=project%20%3D%20JENKINS%20AND%20component%20%3D%20scm-branch-pr-filter-plugin
[pmb-plugin]: https://wiki.jenkins.io/display/JENKINS/Pipeline+Multibranch+Plugin
[sa-plugin]: https://wiki.jenkins.io/display/JENKINS/SCM+API+Plugin
[wiki]: https://wiki.jenkins.io/display/JENKINS/SCM+Branch+PR+Filter+Plugin
