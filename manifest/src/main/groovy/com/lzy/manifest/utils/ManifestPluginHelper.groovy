package com.lzy.manifest.utils

import com.lzy.manifest.ex.Exported
import com.lzy.manifest.ex.InstallRun
import com.lzy.manifest.ex.ManifestExtension
import com.lzy.manifest.ex.QueryPermission
import org.gradle.api.Project

static boolean isApplication(Project project) {
    return project.getPluginManager().hasPlugin("com.android.application")
}

static boolean isLibrary(Project project) {
    return project.getPluginManager().hasPlugin("com.android.library")
}

static boolean isIncludeQueryPermissionAction(ManifestExtension extension) {
    QueryPermission queryPermission = extension.queryPermission;
    if (queryPermission == null) {
        return false
    }
    try {
        return queryPermission.enable
    } catch (Exception ignored) {
        return false
    }
}

static boolean isIncludeExportedAction(ManifestExtension extension) {
    Exported exported = extension.exported
    if (exported == null) {
        return false
    }
    try {
        return exported.enable
    } catch (Exception ignored) {
        return false
    }
}

static boolean isIncludeInstallRunAction(ManifestExtension extension) {
    InstallRun installRun = extension.installRun
    if (installRun == null) {
        return false
    }
    try {
        return installRun.enable
    } catch (Exception ignored) {
        return false
    }
}

static ManifestExtension getExtension(Project project) {
    if (!project.hasProperty("manifestExt")) {
        return null
    }
    Object extObj = project.findProperty("manifestExt")
    if (extObj == null) {
        return null
    }
    if (extObj instanceof ManifestExtension) {
        return extObj as ManifestExtension
    }
    return null
}


