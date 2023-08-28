package com.lzy.manifest.action.permission

import com.android.build.gradle.AppExtension
import com.android.build.gradle.tasks.ProcessApplicationManifest
import com.lzy.manifest.action.core.IManifestAction
import com.lzy.manifest.ex.ManifestExtension
import com.lzy.manifest.utils.Logger
import org.codehaus.groovy.runtime.ResourceGroovyMethods
import org.gradle.api.Project
import org.gradle.api.file.FileCollection

class PermissionQueryAction implements IManifestAction {

    @Override
    void doAction(Project project, ManifestExtension extension) {
        project.extensions.findByType(AppExtension.class).applicationVariants.each { appVariant ->
            try {
                String variantTaskName = "process" + appVariant.getName().capitalize() + "MainManifest"
                ProcessApplicationManifest pamTask = project.getTasks().getByName(variantTaskName)
                pamTask.doFirst {
                    List<File> manifestList = new ArrayList<>()
                    manifestList.add(pamTask.mainManifest.get())
                    FileCollection fileCollection = pamTask.manifests
                    if (fileCollection != null) {
                        Set<File> fileSet = fileCollection.getFiles()
                        manifestList.addAll(fileSet)
                    }
                    if (manifestList.isEmpty()) {
                        return
                    }
                    List<String> permissions = extension.queryPermission.permissions
                    manifestList.each { manifestFile ->
                        String manifestText = ResourceGroovyMethods.getText(manifestFile, "UTF-8")
                        Node xmlNode = new XmlParser(false, false, false).parseText(manifestText)
                        String pkgName = (String) xmlNode.attribute("package");
                        (NodeList) xmlNode.get("uses-permission").each { Node permissionNode ->
                            permissionNode.attributes().each { String key, String value ->
                                String permissionFullName = "${key}=${value}"
                                if (permissions.contains(permissionFullName) || permissions.contains(value)) {
                                    PermissionCache.add(value, pkgName == null ? "unknown" : pkgName)
                                    Logger.log  "权限${permissionFullName},package name:$pkgName"
                                }
                            }
                        }
                    }
                }
                pamTask.doLast {
                    PermissionCache.dump(project)
                    PermissionCache.clear()
                }
            } catch (Exception e) {
                e.printStackTrace()
                Logger.log  "permission query failed:${e.getMessage()}"
            }
        }
    }


}