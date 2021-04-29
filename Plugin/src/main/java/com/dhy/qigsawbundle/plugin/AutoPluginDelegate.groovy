package com.dhy.qigsawbundle.plugin


import org.gradle.api.Project

class AutoPluginDelegate {

    void apply(Project baseProject) {
        baseProject.rootProject.subprojects { Project project ->
            if (project.hasProperty('android')) {
                if (project.plugins.hasPlugin("com.android.dynamic-feature")) insertModuleVersion(project)
            } else project.afterEvaluate {
                if (project.plugins.hasPlugin("com.android.dynamic-feature")) insertModuleVersion(project)
            }
        }

        baseProject.with {
            apply plugin: 'dynamic-provider-switch'
        }
    }

    private void insertModuleVersion(Project project) {
        project.with {
            android.defaultConfig {
                resValue "string", "module_version_${project.name}", "module_version_${versionName}@${versionCode}"
            }
        }
    }
}