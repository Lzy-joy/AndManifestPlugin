package com.lzy.manifest

import com.lzy.manifest.action.core.BaseManifestAction
import com.lzy.manifest.ex.ManifestExtension
import com.lzy.manifest.utils.ManifestPluginHelper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.impldep.org.eclipse.jgit.annotations.NonNull

class AndManifestPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println "--------------------------------"
        println "--------AndManifestPlugin-------"
        println "--------------------------------"
        if (!ManifestPluginHelper.isApplication(project) && ManifestPluginHelper.isLibrary(project)) {
            println 'This plugin is only applicable to Android projects!'
            return
        }
        buildExt(project)
        project.afterEvaluate(new BaseManifestAction())
    }

    private static void buildExt(@NonNull Project project) {
        project.getExtensions().create("manifestExt", ManifestExtension)
    }
}