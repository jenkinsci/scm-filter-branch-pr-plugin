# SCM Filter Branch PR Plugin [![Build # Status][build-icon]][build-link]

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
plugin refer to the wiki article [SCM Filter Branch PR Plugin][wiki].

# Release Notes

For release notes, please refer to the [CHANGELOG.md](CHANGELOG.md).

# How to compile plugin

Developer environment:

    Ubuntu 18.04.3 LTS
    Linux 5.3.0-26-generic x86_64

    Apache Maven 3.6.0
    Maven home: /usr/share/maven
    Java version: 1.8.0_232, vendor: Private Build, runtime: /usr/lib/jvm/java-8-openjdk-amd64/jre
    Default locale: en_US, platform encoding: UTF-8
    OS name: "linux", version: "5.3.0-26-generic", arch: "amd64", family: "unix"

    openjdk version "1.8.0_232"
    OpenJDK Runtime Environment (build 1.8.0_232-8u232-b09-0ubuntu1~18.04.1-b09)
    OpenJDK 64-Bit Server VM (build 25.232-b09, mixed mode)

To compile the plugin run the following command.

    mvn clean package

Upload the compiled plugin to Jenkins located at
`target/scm-filter-branch-pr.hpi`.

# License

```
The MIT License

Copyright (c) 2017-2020, Sam Gleske - https://github.com/samrocketman
Copyright (c) 2017, CloudBees, Inc.
```

See [LICENSE.txt](LICENSE.txt)

[JENKINS-47091]: https://issues.jenkins-ci.org/browse/JENKINS-47091
[build-icon]: https://ci.jenkins.io/buildStatus/icon?job=Plugins/scm-filter-branch-pr-plugin/master
[build-link]: https://ci.jenkins.io/job/Plugins/job/scm-filter-branch-pr-plugin/job/master/
[jira]: https://issues.jenkins-ci.org/issues/?jql=project%20%3D%20JENKINS%20AND%20component%20%3D%20scm-filter-branch-pr-plugin
[pmb-plugin]: https://wiki.jenkins.io/display/JENKINS/Pipeline+Multibranch+Plugin
[sa-plugin]: https://wiki.jenkins.io/display/JENKINS/SCM+API+Plugin
[wiki]: https://wiki.jenkins.io/display/JENKINS/SCM+Filter+Branch+PR+Plugin
