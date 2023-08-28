package com.lzy.manifest.action.core

import com.lzy.manifest.ex.ManifestExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.internal.impldep.org.eclipse.jgit.annotations.NonNull

interface IManifestAction {
    void doAction(@NonNull Project project, @NonNull ManifestExtension extension)
}