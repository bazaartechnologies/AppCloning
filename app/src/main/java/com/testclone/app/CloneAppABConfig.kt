package com.testclone.app

class CloneAppABConfig {

    fun isCloningRestricted(): Boolean {
        return true
    }

    fun isUnknownServiceCheckEnabled(): Boolean {
        return true
    }


    fun isPackageNameRestrictionEnabled(): Boolean {
        return true
    }

    fun isApplicationIdRestrictionEnabled(): Boolean {
        return true
    }

    fun isManifestVerificationEnabled(): Boolean {
        return true
    }

    fun getPackageName(): String {
        return "com.testclone.app"
    }

    fun getManifestSHAsValue(): String {
        return  "2D:2B:AA:56:26:F8:F7:4A:50:3A:24:41:F8:5A:B1:56:95:BF:3B:67"
    }

    fun isSecondSpaceCheckEnabled(): Boolean {
        return true
    }

    fun isAllOtherProfileCheckEnabled(): Boolean {
        return true
    }

    fun isVirtualEnvCheckEnabled(): Boolean {
        return true
    }

    fun isInstallerVerificationEnabled(): Boolean {
        return true
    }

    fun getKnownServices(): List<String> {
        return emptyList()
    }

    fun getApplicationId(): String {
        return "com.testclone.app"
    }

}