package com.dhy.qigsawbundle.plugin


import com.dhy.openusage.OpenUsage
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

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
        createPublishApkTask()
    }

    private void createBundleApkTask(boolean bundle) {
        project.extensions.android.applicationVariants.all { baseVariant ->
            QigsawBundleOption option = project.qigsawBundleOption
            def name = bundle ? "bundle" : "gen"
            BundleApkTask task = project.tasks.create("$name${baseVariant.name.capitalize()}Apks", BundleApkTask)
            task.aabFolderPath = new File(project.buildDir, option.aabFolder.call(baseVariant)).absolutePath
            task.apks = new File(task.aabFolderPath, "${project.name}.apks")
            task.baseApks = new File(task.aabFolderPath, "base.apks")
            task.bundleOption = project.extensions.qigsawBundleOption
            task.isDebug = baseVariant.name.contains("debug")
            task.publish = bundle
            if (project.hasProperty('BUNDLE_TOOL_PATH')) {
                if (task.isDebug) task.dependsOn('bundleDebug')
                else task.dependsOn('bundleRelease')
            }
            task.setGroup(QIGSAW)
        }
    }

    private void createPublishApkTask() {
        project.extensions.android.applicationVariants.all { baseVariant ->
            String variantName = baseVariant.name
            Task task = project.tasks.create("publish${variantName.capitalize()}Apks").doLast {
                QigsawBundleOption option = project.extensions.qigsawBundleOption
                option.type = variantName.contains("debug") ? option.debugType : option.releaseType
                def aabFolder = new File(project.buildDir, option.aabFolder.call(baseVariant))
                def splits = new File(aabFolder, "splits")
                BundleApksUtil.publishSplits(option, splits)
            }
            task.group = QIGSAW
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