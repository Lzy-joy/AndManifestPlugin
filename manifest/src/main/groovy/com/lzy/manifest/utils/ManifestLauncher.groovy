package com.lzy.manifest.utils

import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import groovy.util.slurpersupport.NodeChildren

/**
 * 查找 Launcher Activity包路径
 * note:将Launcher Activity放置在Application标签第一个节点位置，否则可能存在耗时情况
 * @param file merge后Manifest。xml文件
 * @return Launcher Activity包路径
 */
static String findLauncherActivity(File file) {
    try {
        Logger.log "Manifest.xml 路径:${file.path}"
        def manifestText = file.getText("UTF-8")
        GPathResult manifestGPathResult = new XmlSlurper().parseText(manifestText)
        manifestGPathResult.declareNamespace('android': 'http://schemas.android.com/apk/res/android')
        NodeChildren applicationNode = manifestGPathResult.application
        if (applicationNode == null || applicationNode.isEmpty()) {
            Logger.log  "applicationNode == null"
            return ""
        }

        GPathResult activityGPathResult = applicationNode.list().get(0).activity
        if (activityGPathResult == null || activityGPathResult.isEmpty()) {
            return ""
        }
        List launcherList = new ArrayList()
        activityGPathResult.each { NodeChild activityNode ->
            def activityName = isLauncherActivity(activityNode)
            if (!activityName.isEmpty()) {
                launcherList.add(activityName)
            }
        }

        if (launcherList.size() == 0) {
            return ""
        } else if (launcherList.size() == 1) {
            Logger.log  "Launcher Activitiy:${launcherList[0]}"
            return launcherList[0]
        } else {
            Logger.log  "The App has multiple Launcher Activities:"
            launcherList.each {
                println "${it}"
            }
            Logger.log  "by default,return the first LauncherActivity:${launcherList[0]}"
            return launcherList[0]
        }
    } catch (e) {
        Logger.log  "The launcherActivity is not found:${e.message}"
    }
    return ""
}

static String isLauncherActivity(NodeChild activityNode) {
    GPathResult childNode = activityNode.children()
    def iterator = childNode.iterator()
    while (iterator.hasNext()) {
        GPathResult itemNode = (NodeChild) iterator.next()
        if (itemNode.name() == "intent-filter") {
            GPathResult filterNode = itemNode.children()
            if (filterNode.size() < 2) {
                return ""
            }
            def isMain = isContainMainAction(filterNode)
            def isLauncher = isContainLauncherCategory(filterNode)
            if (isMain && isLauncher) {
                return activityNode."@android:name"
            }
        }
    }
    return ""
}

private static boolean isContainMainAction(GPathResult filterNode) {
    def iterator = filterNode.iterator()
    while (iterator.hasNext()) {
        GPathResult filterItemNode = (GPathResult) iterator.next()
        if (filterItemNode.name() == "action" && filterItemNode.@"android:name" == "android.intent.action.MAIN") {
            return true
        }
    }
    return false
}

private static boolean isContainLauncherCategory(GPathResult filterNode) {
    def iterator = filterNode.iterator()
    while (iterator.hasNext()) {
        GPathResult filterItemNode = (GPathResult) iterator.next()
        if (filterItemNode.name() == "category" && filterItemNode.@"android:name" == "android.intent.category.LAUNCHER") {
            return true
        }
    }
    return false
}