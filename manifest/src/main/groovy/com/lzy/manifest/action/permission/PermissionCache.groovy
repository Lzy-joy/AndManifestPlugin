package com.lzy.manifest.action.permission

import com.lzy.manifest.utils.FileUtil
import com.lzy.manifest.utils.Logger
import org.gradle.api.Project

class PermissionCache {
    private static final Map<String, HashSet<String>> permissionMap = new HashMap<>()

    static void add(permissionName, pkgName) {
        if (permissionMap.containsKey(permissionName)) {
            permissionMap.get(permissionName).add(pkgName)
        } else {
            def pkgSet = new HashSet<String>()
            pkgSet.add(pkgName)
            permissionMap.put(permissionName, pkgSet)
        }
    }

    static void clear() {
        permissionMap.clear()
    }

    static void dump(Project project) {
        if (permissionMap.size() == 0) {
            return
        }

        StringBuffer buffer = new StringBuffer()
        buffer.append("written time:${new Date().format("yyyy-MM-dd HH:mm:ss")}\n")
        permissionMap.each { permissionEntry ->
            buffer.append("${permissionEntry.key}\n")
            permissionEntry.value.each { pkgName ->
                buffer.append("    ")
                buffer.append("${pkgName}\n")
            }
        }
        dumpPermission(project, buffer.toString())
    }

    private static def dumpPermission(Project project, String content) {
        Logger.log(content)
        def dir = FileUtil.createDir(project)
        if (dir.empty) {
            Logger.log('权限结果写入文件失败:文件夹创建失败')
            return
        }
        def txtFilePath = FileUtil.createFile(dir, "queryPermissionResult")
        if (txtFilePath.empty) {
            Logger.log('权限结果写入文件失败:权限文件创建失败')
            return
        }
        new File(txtFilePath).withWriter("utf-8") { bw ->
            bw.write(content)
            bw.flush()
        }
    }
}