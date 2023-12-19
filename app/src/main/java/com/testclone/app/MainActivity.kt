package com.testclone.app

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val notepad = findViewById<TextView>(R.id.notepad)
        val version = 6

        val context = this.baseContext
        val context2 = this.createPackageContext("com.testclone.app", CONTEXT_IGNORE_SECURITY)

        val filePath = context.filesDir.path
        val dataPath = context.dataDir.path
        val cachePath = context.cacheDir.path
        val file2 = context2.externalCacheDir

//        val pkgM2 = packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA))


//          context.checkse
        notepad.append("\nversion: $version")

        notepad.append("\nfile path: $filePath")
        notepad.append("\ndataPath: $dataPath")
        notepad.append("\ncachePath: $cachePath")
        notepad.append("\nfile 2: $file2")

        notepad.append("\n abc : ${Build.VERSION.SDK_INT}")

        notepad.append("\npackage name: ${context.packageName}")

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        activityManager.appTasks.get(0).taskInfo

        val sdkVersion = Build.VERSION.SDK_INT

        if (sdkVersion >= Build.VERSION_CODES.O) {
            val services = activityManager.getRunningServices(Int.MAX_VALUE)
            val processes = activityManager.runningAppProcesses


            notepad.append("\n 1 Services ${services}")
            notepad.append("\n Process list ${processes.size}\n ")

            for(process in processes){
                notepad.append("\n 2 processes ${process.processName}")
            }


            for (service in services) {
                notepad.append("\n servicePackageName ${service.service.packageName}")
//                process.pkgList.forEach {
//                    notepad.append("\n pkglist $it")
//                }
            }

            val apptasks = activityManager.appTasks


            notepad.append("\n services App tasks ${apptasks}")
            val remoteConfig = CloneAppABConfig()
            val environmentChecker = EnvironmentCheckerImpl(context,remoteConfig)
            notepad.append("\n ${environmentChecker.getCloningStatus()}\n")

            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);

            notepad.append("\nApp data directory ${appInfo.dataDir}")
            notepad.append("\nPackage Name ${appInfo.packageName}")
            notepad.append("\nProcess name ${appInfo.processName}")
            notepad.append("\nStorage UUID ${appInfo.storageUuid.toString()}")
            notepad.append("\nAPP UUID ${appInfo.uid}")
            notepad.append("\nAPP Metadata ${appInfo.metaData}")
            notepad.append("\nAPP Name ${appInfo.name}")





            notepad.append("\nAndroid count ${countAndroidDirectories()}")




            apptasks.forEach {
//                notepad.append("\n taskInfo ${it.taskInfo}")
//                notepad.append("\n serviceClassName ${it.service.className}")
//                notepad.append("\n serviceClientPackage ${it.clientPackage}")
            }
        }
    }


    fun countAndroidDirectories(): Int {
        val externalStorage = Environment.getExternalStorageDirectory()
        val androidDirectories = findAndroidDirectories(externalStorage)
        return androidDirectories.size
    }

    fun findAndroidDirectories(directory: File): List<File> {
        val androidDirectories = mutableListOf<File>()

        // List all files and directories in the given directory
        val files = directory.listFiles()

        // Iterate through the files and directories
        files?.forEach { file ->
            if (file.isDirectory) {
                // Check if the directory name is "Android"
                if (file.name.equals("Android", ignoreCase = true)) {
                    androidDirectories.add(file)
                } else {
                    // Recursively search for Android directories in subdirectories
                    androidDirectories.addAll(findAndroidDirectories(file))
                }
            }
        }

        return androidDirectories
    }
}