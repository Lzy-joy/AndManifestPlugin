package com.lzy.manifest.action.core

import com.lzy.manifest.action.export.ExportedTagAction
import com.lzy.manifest.action.install_run.InstallRunAction
import com.lzy.manifest.action.permission.PermissionQueryAction
import com.lzy.manifest.ex.ManifestExtension
import com.lzy.manifest.utils.Logger
import com.lzy.manifest.utils.ManifestPluginHelper
import org.gradle.api.Action
import org.gradle.api.Project

class BaseManifestAction implements Action<Project> {

    @Override
    void execute(Project project) {
        ManifestExtension extension = ManifestPluginHelper.getExtension(project)
        if (extension == null) {
            println "The manifestExt tag is not configured"
            return
        }
        Logger.isEnable = extension.log_enable
        if (ManifestPluginHelper.isIncludeQueryPermissionAction(extension)) {
            Logger.log("----------QueryPermissionAction-----------")
            new PermissionQueryAction().doAction(project, extension)
        }
        if (ManifestPluginHelper.isIncludeExportedAction(extension)) {
            Logger.log("----------ExportedTagAction-----------")
            new ExportedTagAction().doAction(project, extension)
        }
        if (ManifestPluginHelper.isIncludeInstallRunAction(extension)) {
            Logger.log("----------InstallRunAction-----------")
            new InstallRunAction().doAction(project, extension)
        }
    }
}