package com.lzy.manifest.ex

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory

import javax.inject.Inject

abstract class ManifestExtension {
    public boolean log_enable
    public QueryPermission queryPermission
    public Exported exported
    public InstallRun installRun

    @Inject
    ManifestExtension(ObjectFactory objectFactory) {
        queryPermission = objectFactory.newInstance(QueryPermission)
        exported = objectFactory.newInstance(Exported)
        installRun = objectFactory.newInstance(InstallRun)
    }

    void queryPermission(Action<? super QueryPermission> action) {
        action.execute(queryPermission)
    }

    void exported(Action<? super Exported> action) {
        action.execute(exported)
    }

    void installRun(Action<? super InstallRun> action) {
        action.execute(installRun)
    }
}

abstract class QueryPermission implements Serializable {
    public boolean enable = false
    public List<String> permissions
}

abstract class Exported implements Serializable {
    public boolean enable = false
    public List<String> excludePackage
    public List<String> excludeClass
}

abstract class InstallRun implements Serializable {
    public boolean enable = true
    public boolean isOnlyInstall = false
    public String taskGroup
}