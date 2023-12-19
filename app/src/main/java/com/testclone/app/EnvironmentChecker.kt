package com.testclone.app

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import java.net.URL
import java.util.jar.Attributes
import java.util.jar.Manifest


interface EnvironmentChecker {
//    fun getEmulatorStatus(): EnvStatus
//    fun getRootStatus(): EnvStatus
//    fun getDebuggableStatus(): EnvStatus
    fun getCloningStatus(): EnvStatus
}

sealed class EnvStatus {
    data class Blocked(val errorCode: Int) : EnvStatus()
    object UnBlocked : EnvStatus()
}

val EnvStatus.isBlocked: Boolean
    get() = this is EnvStatus.Blocked

val EnvStatus.errorCode: Int
    get() = (this as EnvStatus.Blocked).errorCode

/**
 * This class checks the underlying environment i.e. Emulator and Rooted device, dual space container.
 * Restrict app to run in the preceding conditions
 */
class EnvironmentCheckerImpl(
    private val context: Context,
    val remoteConfig: CloneAppABConfig
) : EnvironmentChecker {

//    override fun getEmulatorStatus(): EnvStatus {
//        return if (BuildConfig.DEBUG.not() && RemoteConfigs.getBoolean(Keys.IS_EMULATOR_RESTRICTION_ENABLED)) {
//
//            if (isEmulator()) {
//                logNonfatalException(CX_2000)
//                EnvStatus.Blocked(CX_2000)
//            } else {
//                EnvStatus.UnBlocked
//            }
//        } else {
//            EnvStatus.UnBlocked
//        }
//    }

//    override fun getRootStatus(): EnvStatus {
//        return if (BuildConfig.DEBUG.not() && RemoteConfigs.getBoolean(Keys.IS_ROOTED_RESTRICTION_ENABLED)) {
//
//            if (CommonUtils.isRooted(context)) {
//                logNonfatalException(CX_2001)
//                EnvStatus.Blocked(CX_2001)
//            } else {
//                EnvStatus.UnBlocked
//            }
//        } else {
//            EnvStatus.UnBlocked
//        }
//    }

//    override fun getDebuggableStatus(): EnvStatus {
//        return if (RemoteConfigs.isDebugModeRestrictionEnable()) {
//            val condition =
//                BuildConfig.FLAVOR == FLAVOR_PROD && (0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)
//            if (condition) {
//                logNonfatalException(CX_2002)
//                EnvStatus.Blocked(CX_2002)
//            } else {
//                EnvStatus.UnBlocked
//            }
//        } else {
//            EnvStatus.UnBlocked
//        }
//    }

    private fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") || Build.DEVICE.startsWith("generic")
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator"))
    }

    override fun getCloningStatus(): EnvStatus {
        val envStatus = if (BuildConfig.DEBUG.not() && remoteConfig.isCloningRestricted()) {
            checkAppCloning(context)
        } else {
            EnvStatus.UnBlocked
        }

        return envStatus
    }

    private fun checkAppCloning(context: Context): EnvStatus {
        val path = context.filesDir.path
        val uri = Uri.parse(path)

        val userProfileId = if (path.contains("[0-9]".toRegex())) {
            uri.pathSegments[2].toInt()
        } else {
            DEFAULT_PROFILE_ID
        }

        return when {
//            remoteConfig.isUnknownServiceCheckEnabled() && isUnknownServiceRunning() -> {
//                logNonfatalException(CX_1010)
//                EnvStatus.Blocked(CX_1010)
//            }

//            remoteConfig.isSignatureVerificationEnbable && isAppSignatureChanged() -> {
//                logNonfatalException(CX_1009)
//                EnvStatus.Blocked(CX_1009)
//            }

            remoteConfig.isManifestVerificationEnabled() && isChangesInManifestDetected() -> {
                logNonfatalException(CX_1000)
                EnvStatus.Blocked(CX_1000)
            }
            remoteConfig.isPackageNameRestrictionEnabled() && isPackageNameTampered() -> {
                logNonfatalException(CX_1001)
                EnvStatus.Blocked(CX_1001)
            }
            remoteConfig.isApplicationIdRestrictionEnabled() && isApplicationIdTampered() -> {
                logNonfatalException(CX_1002)
                EnvStatus.Blocked(CX_1002)
            }
            remoteConfig.isInstallerVerificationEnabled() && isUnknownInstaller(context) -> {
                logNonfatalException(CX_1003)
                EnvStatus.Blocked(CX_1003)
            }
            isAppUUIDTampered(context) -> {
                EnvStatus.Blocked(CX_1002)
            }
//            remoteConfig.isVirtualEnvCheckEnabled() && isRunningOnVirtualEnv() -> {
//                logNonfatalException(CX_1004)
//                EnvStatus.Blocked(CX_1004)
//            }

            isPathContainDualAppId(path) -> {
                logNonfatalException(CX_1005)
                EnvStatus.Blocked(CX_1005)
            }
            remoteConfig.isSecondSpaceCheckEnabled() && isSecondSpaceProfileId(userProfileId) -> {
                logNonfatalException(CX_1006)
                EnvStatus.Blocked(CX_1006)
            }
            remoteConfig.isAllOtherProfileCheckEnabled() && isOtherProfileThanDefault(userProfileId) -> {
                logNonfatalException(CX_1007)
                EnvStatus.Blocked(CX_1007)
            }
            else -> {
                val hasMoreDots = getPackageDotsCount(path) > APP_PACKAGE_DOT_COUNT
                if (hasMoreDots) {
                    logNonfatalException(CX_1008)
                    EnvStatus.Blocked(CX_1008)
                } else {
                    EnvStatus.UnBlocked
                }
            }
        }
    }

    private fun isAppUUIDTampered(context: Context): Boolean {
            return false
    }

    private fun logNonfatalExceptionWithMessage(message: String) {
//        FirebaseCrashlytics.getInstance().log(message)
    }

    private fun logNonfatalException(errorCode: Int) {
//        FirebaseCrashlytics.getInstance()
//            .recordException(Exception("Error Code: $errorCode"))
    }

    private fun isUnknownServiceRunning(): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val knownServices = remoteConfig.getKnownServices()


            for (service in manager.getRunningServices(Int.MAX_VALUE)) {

                if (knownServices.contains(service.service.packageName).not()) {
                    logNonfatalExceptionWithMessage("Unknown service detected:: ${service.service.packageName}")
                    return true
                }
            }
        }
        return false
    }

    private fun isChangesInManifestDetected(): Boolean {
        var sha: String? = ""
        val resources = context.applicationContext.classLoader.getResources("META-INF/MANIFEST.MF")
        while (resources.hasMoreElements()) {
            try {
                val element: URL = resources.nextElement()
                val manifest = Manifest(element.openStream())
                val map: Map<String, Attributes> = manifest.entries

                val attributes: Attributes? = map["AndroidManifest.xml"]

                if (attributes != null) {
                    sha = attributes.getValue("SHA1-Digest")

                    if (sha == null) {
                        sha = attributes.getValue("SHA-256-Digest")
                    }

                    break
                }

            } catch (e: Exception) {
//                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }

        val expectedShas = remoteConfig.getManifestSHAsValue()

        return if (expectedShas.isNotEmpty()) {
            val shaList: List<String> = expectedShas.split(",")

            return sha.isNullOrEmpty().not() && shaList.contains(sha).not()
        } else {
            false
        }
    }

    private fun isUnknownInstaller(context: Context): Boolean {
        // A list with valid installers package name
        val validInstallers: List<String> =
            ArrayList(listOf("com.android.vending", "com.google.android.feedback"))

        val packageManager: PackageManager = context.packageManager

        var installerInfo: String? = null

        try {
            installerInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                packageManager.getInstallerPackageName(context.packageName)
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        // true if your app has been downloaded from Play Store
        return installerInfo != null && validInstallers.contains(installerInfo).not()
    }

//    private fun isRunningOnVirtualEnv(): Boolean {
//        val pm: PackageManager = context.packageManager
//        val packages: List<ApplicationInfo> =
//            pm.getInstalledApplications(PackageManager.GET_META_DATA)
//
//        var systemPackages = ""
//
//        for (packageInfo in packages) {
//            if (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
//                // its system application
//                systemPackages += packageInfo.packageName + " "
//            }
//        }
//
//        val packagesList =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                RemoteConfigs.getOs11MustHavePackages().split(",")
//            } else {
//                RemoteConfigs.getOs11BelowMustHavePackages().split(",")
//            }
//
//        for (packageName in packagesList) {
//            if (systemPackages.contains(packageName).not()) {
//                return true
//            }
//        }
//
//        return false
//    }

    private fun isPackageNameTampered(): Boolean {
        return context.packageName.equals(remoteConfig.getPackageName()).not()
    }

    private fun isApplicationIdTampered(): Boolean {
        return (BuildConfig.APPLICATION_ID != remoteConfig.getApplicationId())
    }

    private fun isOtherProfileThanDefault(userProfileId: Int) = userProfileId > DEFAULT_PROFILE_ID

    private fun isPathContainDualAppId(path: String) = path.contains(DUAL_APP_ID_999)

    private fun isSecondSpaceProfileId(userProfileId: Int) =
        userProfileId > PROFILE_IDS_ALLOWED_LIMIT

    private fun getPackageDotsCount(path: String): Int {
        var count = 0

        for (element in path) {
            if (count > APP_PACKAGE_DOT_COUNT) {
                break
            }
            if (element == DOT) {
                count++
            }
        }

        return count
    }

    companion object {
        private const val APP_PACKAGE_DOT_COUNT = 2 // number of dots present in package name
        private const val DUAL_APP_ID_999 = "999"
        private const val DOT = '.'
        private const val PROFILE_IDS_ALLOWED_LIMIT = 9
        private const val DEFAULT_PROFILE_ID = 0
        private const val FLAVOR_PROD = "prod"


        // ERROR CODE
        // Error codes for cloning
        const val CX_1000 = 1000  /// apk tampered
        const val CX_1001 = 1001  /// app package name tampered
        const val CX_1002 = 1002  // app application id changed
        const val CX_1003 = 1003  // installed from unknown source
        const val CX_1004 = 1004  // running on virtual environment
        const val CX_1005 = 1005  // directory path contian dual app id
        const val CX_1006 = 1006  // second space profile id
        const val CX_1007 = 1007  // no defualt profile id
        const val CX_1008 = 1008  // package name lenght changed
        const val CX_1009 = 1009
        const val CX_1010 = 1010


        /// Error code for root, emulator and debug modes
        const val CX_2000 = 2000 // emulaotr
        const val CX_2001 = 2001 // rooted phone
        const val CX_2002 = 2002 // debuggable mode
        const val CX_2003 = 2003
        const val CX_2004 = 2004
        const val CX_2005 = 2005
    }
}