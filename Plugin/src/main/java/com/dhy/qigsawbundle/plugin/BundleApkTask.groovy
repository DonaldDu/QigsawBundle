package com.dhy.qigsawbundle.plugin


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class BundleApkTask extends DefaultTask {
    @Input
    String aabFolderPath
    @OutputFile
    File apks
    @OutputFile
    File baseApks
    @Input
    boolean isDebug
    @Input
    boolean publish
    @Input
    boolean log = true
    @Internal
    QigsawBundleOption bundleOption
    private String bundleTool

    @TaskAction
    void generate() throws IOException {
        bundleTool = project.findProperty('BUNDLE_TOOL_PATH')
        if (bundleTool == null) {
            throw new IllegalArgumentException('You must set BUNDLE_TOOL_PATH first, eg: project.ext.BUNDLE_TOOL_PATH')
        }
        if (bundleOption.apkFileHost == null) {
            throw new IllegalArgumentException('You should set QigsawBundleOption.apkFileHost for apk url')
        }
        def aab = findAAB()
        if (aab == null || !aab.exists()) {
            throw new IllegalArgumentException('aab file not found, please fix aabFolder')
        }
        println 'bundle to apks ...'
        genSplits(aab)
        genBaseApk(aab)
        bundleOption.type = isDebug ? bundleOption.debugType : bundleOption.releaseType
        bundleOption.publish = publish
        BundleApksUtil.INSTANCE.bundleApks(bundleOption, apks, baseApks)
    }

    private void genSplits(File aab) {
        def cmd = "java -jar $bundleTool build-apks --bundle=${aab.name} --output=${apks.name} --optimize-for=abi --overwrite "
        if (bundleOption.options != null && bundleOption.options.size() > 0) {
            cmd += bundleOption.options.join(' ')
        }
        println 'gen Split apks ...'
        if (log) println 'cmd: ' + cmd
        BundleApksUtil.INSTANCE.runCommand(cmd, aab.parentFile)
    }

    private void genBaseApk(File aab) {
        def cmd = "java -jar $bundleTool build-apks --bundle=${aab.name} --output=${baseApks.name} --mode=universal --modules=base --overwrite "
        if (bundleOption.options != null && bundleOption.options.size() > 0) {
            cmd += bundleOption.options.join(' ')
        }
        println 'gen base apk ...'
        if (log) println 'cmd: ' + cmd
        BundleApksUtil.INSTANCE.runCommand(cmd, aab.parentFile)
    }

    private File findAAB() {
        def aabs = new File(aabFolderPath).listFiles().findAll { it.name.endsWith('.aab') }
        if (aabs.size() > 1) throw new IllegalArgumentException("More than one aab files were found, please delete the errors then restart task")
        else if (aabs.size() == 1) return aabs.first()
        else return null
    }
}