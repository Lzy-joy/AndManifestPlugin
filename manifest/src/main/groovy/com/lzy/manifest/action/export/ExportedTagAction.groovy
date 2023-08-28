package com.lzy.manifest.action.export

import com.android.build.gradle.AppExtension
import com.android.build.gradle.tasks.ProcessApplicationManifest
import com.lzy.manifest.action.core.IManifestAction
import com.lzy.manifest.ex.ManifestExtension
import com.lzy.manifest.utils.Logger
import groovy.xml.XmlUtil
import org.gradle.api.Project
import org.gradle.api.file.FileCollection

class ExportedTagAction implements IManifestAction {
    static def EXPORTED_NS = "{http://schemas.android.com/apk/res/android}exported"
    static def exportedComponentList = ["activity", "service", "receiver"]
    public static List<String> defaultExcludePkg = [
            "android.",
            "androidx.",
            "com.google.android",
            "com.didichuxing.doraemonkit"
    ]

    @Override
    void doAction(Project project, ManifestExtension extension) {
        project.extensions.findByType(AppExtension.class).applicationVariants.each { appVariant ->
            try {
                String variantTaskName = "process" + appVariant.getName().capitalize() + "MainManifest"
                ProcessApplicationManifest pamTask = project.getTasks().getByName(variantTaskName)
                println("export task name:${pamTask.name}")
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
                    manifestList.each { manifestFile ->
                        if (!manifestFile.exists()) {
                            return
                        }
                        def manifestText = manifestFile.getText("UTF-8")
                        def xmlNode = new XmlParser(false, false, false).parseText(manifestText)
                        def pkgName = xmlNode.attribute("package")
                        isHandlePkgExported(pkgName, extension) { isHandleExported ->
                            if (!isHandleExported) {
                                return
                            }
                            NodeList applicationNode = xmlNode.get("application")
                            if (applicationNode == null || applicationNode.size() == 0) {
                                return
                            }
                            def isRevised = false
                            exportedComponentList.each { componentName ->
                                NodeList componentNodeList = applicationNode[0]."${componentName}"
                                if (componentNodeList == null || componentNodeList.size() == 0) {
                                    return
                                }
                                componentNodeList.each { Node componentNode ->
                                    isHandleClassExported(componentNode, extension) { isNeedAdd, exportedValue ->
                                        if (!isNeedAdd) {
                                            return
                                        }
                                        isRevised = true
                                        componentNode.attributes().put("android:exported", exportedValue)
                                    }
                                }
                                if (isRevised) {
                                    //println "Manifest.xml文件:${manifestText}"
                                    String result = XmlUtil.serialize(xmlNode)
                                    //println "Manifest.xml 修改后:${result}"
                                    manifestFile.write(result, "utf-8")
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                Logger.log "Manifest.xml 修改失败:${e.message}"
            }
        }
    }
    /**
     * Manifest.xml package tag
     * package:xxx.xxx.xxx
     */
    static def isHandlePkgExported(String pkgName, ManifestExtension extension, Closure closure) {
        if (pkgName == null || pkgName.isEmpty()) {
            closure(false)
            return
        }

        boolean isDefaultExcludePkg = true
        defaultExcludePkg.each { pkgPrefix ->
            if (pkgName.startsWith(pkgPrefix)) {
                isDefaultExcludePkg = false
            }
        }
        if (!isDefaultExcludePkg) {
            closure(false)
            return
        }
        def excludePackageList = extension.exported.excludePackage
        if (excludePackageList == null) {
            closure(true)
            return
        }
        if (excludePackageList.contains(pkgName)) {
            closure(false)
            return
        }
        closure(true)
    }

    static def isHandleClassExported(Node node, ManifestExtension extension, Closure closure) {
        def isIntentFilterExist = false
        node.children().find {
            if ("intent-filter" == it.name()) {
                isIntentFilterExist = true
            }
        }
        // activity 、service, receiver exists intent-filter tag
        if (isIntentFilterExist) {
            def isExportedExist = false
            node.attributes().find {
                if ("android:exported" == it.getKey().toString()) {
                    isExportedExist = true
                }
            }
            if (isExportedExist) {//exported tag exist,do not handle
                closure(false, true)
            } else {
                closure(true, isExcludeClassList(node, extension))
            }
        } else {//仅仅适配Android12,这里可不做任何操作，这里给其添加默认exported值取之于是否配置在白名单中
            closure(true, isExcludeClassList(node, extension))
        }
    }

    static def isExcludeClassList(Node node, ManifestExtension extension) {
        if (node == null) {
            return false
        }
        def excludeClassList = extension.exported.excludeClass
        if (excludeClassList == null) {
            return false
        }
        def nameAttr = node.attribute("android:name")
        if (nameAttr == null) {
            return false
        }
        def name = nameAttr as String
        if (name == null || name.isEmpty()) {
            return false
        }
        def isInclude = excludeClassList.find {
            it.contains(name)
        }
        return isInclude != null && !isInclude.isEmpty()
    }
}