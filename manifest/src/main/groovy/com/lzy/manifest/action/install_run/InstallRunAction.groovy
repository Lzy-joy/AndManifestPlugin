package com.lzy.manifest.action.install_run

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.tasks.ProcessMultiApkApplicationManifest
import com.lzy.manifest.action.core.IManifestAction
import com.lzy.manifest.action.permission.PermissionCache
import com.lzy.manifest.ex.ManifestExtension
import com.lzy.manifest.utils.FileUtil
import com.lzy.manifest.utils.Logger
import com.lzy.manifest.utils.ManifestLauncher
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.StopExecutionException

class InstallRunAction implements IManifestAction {
    private static final String DEFAULT_GROUP = "AndManifest"

    @Override
    void doAction(Project project, ManifestExtension extension) {
        project.extensions.findByType(AppExtension.class).applicationVariants.each { variant ->
            def apkOuts = variant.outputs.matching {
                it.outputFile != null && it.outputFile.name.endsWith('.apk')
            }
            if (apkOuts.isEmpty()) {
                Logger.log("Apk file not find")
                return
            }
            Task assembleVariantTask = project.tasks.findByName("assemble${variant.name.capitalize()}")

            def apkFile = apkOuts.first().outputFile
            def installTaskName = assembleVariantTask.name + "Install"
            def installTaskRunName = assembleVariantTask.name + "Run"
            def installTask = buildInstallTask(project, installTaskName, apkFile, extension, assembleVariantTask)
            def runTask = buildRunTask(project, installTaskRunName, variant, extension)
            runTask.dependsOn(installTask)
        }
    }


    private static def buildRunTask(Project project, String taskName, ApplicationVariant variant, ManifestExtension extension) {
        return project.task(taskName) {
            group(extension.installRun.taskGroup == null ? DEFAULT_GROUP : extension.installRun.taskGroup)
            enabled(extension.installRun.isOnlyInstall)
            doLast {
                def launcherActivityName = ""
                variant.outputs.all { BaseVariantOutput output ->
                    def processManifestTask = output.processManifestProvider.get() as ProcessMultiApkApplicationManifest
                    def mergedManifestParentPath = processManifestTask.multiApkManifestOutputDirectory.get().getAsFile()
                    File mergedManifestFile = new File(mergedManifestParentPath, "AndroidManifest.xml")
                    if (mergedManifestFile != null && mergedManifestFile.exists()) {
                        launcherActivityName = ManifestLauncher.findLauncherActivity(mergedManifestFile)
                    }
                }
                if (launcherActivityName.isEmpty()) {
                    throw new StopExecutionException("Apk installed successfully,but not find launcherActivity")
                }
                def launcherResultCode = "adb shell am start -n ${variant.applicationId}/${launcherActivityName}".execute().waitFor()
                if (launcherResultCode == 0) {
                    Logger.log "App started successfully!"
                } else {
                    Logger.log "App started failed!"
                    throw new StopExecutionException("Apk installed successfully,but failed to start!")
                }
            }
        }
    }

    private static def buildInstallTask(Project project, String taskName, File apkFile, ManifestExtension extension, Task assembleVariantTask) {
        def installTask = project.task(taskName) {
            group(extension.installRun.taskGroup == null ? DEFAULT_GROUP : extension.installRun.taskGroup)
            enabled(true)
            doFirst {
                println("Apk file path:${apkFile.absolutePath}")
                def installCode = "adb install ${apkFile.absolutePath}".execute().onExit().get().exitValue()
                if (installCode == 0) {
                    println "App installed successfully!"
                    project.copy { CopySpec copySpec ->
                        copySpec.from("${apkFile.absolutePath}")
                        copySpec.into(getApkOutputs(project))
                    }
                } else {
                    throw new StopExecutionException("App installation failed:$installCode")
                }
            }
        }
        installTask.dependsOn assembleVariantTask.taskDependencies.getDependencies(assembleVariantTask)
        return installTask
    }

    private static def getApkOutputs(Project project) {
        def dir = FileUtil.createDir(project)
        if (dir.empty) {
            return "${project.rootProject.getRootDir().absolutePath}/outputs/"
        } else {
            return "${dir}/outputs/"
        }
    }
}