# AndManifestPlugin
The Plugin of Android Manifest.xml ,permission query,exported tag auto completion,install and run gradle script.
#### AGP Version Requested
&ensp;&ensp;&ensp;&ensp;Requires AGP minimum version 7.0.2
#### Query Permission 
&ensp;&ensp;&ensp;&ensp;Add the following extended configuration to build.gradle in the main project directory of the Android project:
```groovy
manifestExt {
    log_enable = true
    
    queryPermission {
        enable = true
        permissions = ["android.permission.INTERNET",
                       "android.permission.READ_PHONE_STATE",
                       "android.permission.READ_EXTERNAL_STORAGE"
        ]
    }
}
```
&ensp;&ensp;&ensp;&ensp;The permission query results will be output to "./AndManifest/queryPermissionResult" in the project root directory.
#### exported tag auto-completion
&ensp;&ensp;&ensp;&ensp;In the Android project, the AGP version is 7.0 or higher and the targetSDk version is 30 or higher, and the exported tag needs to be processed in the Manifest.xml activity, service, and receiver component tags:
>①Only applicable to activity, service, receiver component tags.
②The target SDK version is above 30.
③The <intent-filter> filter tag is declared in the activity, service, and receiver tags. When the application project target is 31 or above, all AndroidManifest.xml in the application project, library project, and third-party SDK will be detected according to the above rules.     

&ensp;&ensp;&ensp;&ensp; Adding the following extension will be able to automatically complete the missing exported tags:
```groovy
manifestExt {
    log_enable = true

    exported {
        enable = true
        excludeClass = [""]
        excludePackage =["com.lzy.xxx","...","..."]
    }
}
```
excludeClass:No need to process the full path of activity, service, receiver.
excludePackage:The package name of the module that does not need to be processed.
#### Run or install the Apk
first config the extensions:
```groovy
manifestExt {
    log_enable = true

    installRun {
        taskGroup = "sample"
    }
}
```  
by default,it do not close the task after config the extensions.execute the install Apk:
```groovy
./gradlew assembleDebugInstall
./gradlew assembleReleaseInstall
or your custom variant type
./gradlew assembleCustomInstall
```   
Similarly, install and run the apk
```groovy
./gradlew assembleDebugRun
./gradlew assembleReleaseRun
or your custom variant type
./gradlew assembleCustomRun
```