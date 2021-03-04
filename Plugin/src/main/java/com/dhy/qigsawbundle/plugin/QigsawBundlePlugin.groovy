package com.dhy.qigsawbundle.plugin

import com.dhy.openusage.Honor
import com.dhy.openusage.OpenUsage
import com.dhy.openusage.UsingApp
import org.gradle.api.Plugin
import org.gradle.api.Project

class QigsawBundlePlugin implements Plugin<Project> {
    private def QIGSAW = "qigsaw-bundle"
    private Project project
    private AutoPluginDelegate autoPlugin = new AutoPluginDelegate()

    @Override
    void apply(Project project) {
        this.project = project
        autoPlugin.apply(project)
        project.extensions.create("qigsawBundleOption", QigsawBundleOption)
        createTask()
        initOpenUsage()
    }

    private void createTask() {
        createBundleApkTask(true)
        createBundleApkTask(false)
    }

    private void createBundleApkTask(boolean bundle) {
        project.extensions.android.applicationVariants.all { baseVariant ->
            QigsawBundleOption option = project.qigsawBundleOption
            def name = bundle ? "bundle" : "gen"
            BundleApkTask task = project.tasks.create("$name${baseVariant.name.capitalize()}Apks", BundleApkTask)
            task.aabFolder = new File(project.buildDir, option.aabFolder.call(baseVariant))
            task.apks = new File(task.aabFolder, "${project.name}.apks")
            task.baseApks = new File(task.aabFolder, "base.apks")
            task.bundleOption = project.extensions.qigsawBundleOption
            if (bundle && project.hasProperty('BUNDLE_TOOL_PATH')) task.dependsOn('bundle')
            task.setGroup(QIGSAW)
        }
    }

    private void initOpenUsage() {
        project.afterEvaluate {
            def name = 'QigsawBundle'
            def url = 'https://gitee.com/DonaldDu/QigsawBundle'
            OpenUsage.report(project, name, url)
        }
    }
}