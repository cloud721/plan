package com.example.simplewebview.attribution

class UploadTypeRegistry {
    fun resolve(
        sdk: LinkSdkType,
        fromInstallRef: Boolean = false,
        fromIntent: Boolean = false,
    ): Int {
        return when (sdk) {
            LinkSdkType.FACEBOOK -> {
                if (fromInstallRef) UPLOAD_SDK_FACEBOOK_INSTALL
                else UPLOAD_SDK_FACEBOOK_DEEP_LINK
            }
            LinkSdkType.FIREBASE -> UPLOAD_SDK_FIREBASE
            LinkSdkType.APPSFLYER -> {
                if (fromIntent) UPLOAD_SDK_AF_DEEP_LINK
                else if (fromInstallRef) UPLOAD_SDK_AF_INSTALL
                else UPLOAD_SDK_AF_DEEP_LINK
            }
            LinkSdkType.HUAWEI -> UPLOAD_SDK_HUAWEI
            LinkSdkType.KOCHAVA -> UPLOAD_SDK_KOCHAVA
            LinkSdkType.ADJUST -> UPLOAD_SDK_ADJUST
            LinkSdkType.GOOGLE -> UPLOAD_SDK_GOOGLE
            LinkSdkType.CLIPBOARD -> UPLOAD_SDK_CLIPBOARD
            LinkSdkType.UNIVERSAL -> UPLOAD_SDK_UNIVERSAL_LINK
            LinkSdkType.GOOGLE_ENGAGE -> UPLOAD_SDK_GOOGLE_ENGAGE
            LinkSdkType.SERVER_DEFAULT -> UPLOAD_SDK_CHANGDU_DEFAULT
            LinkSdkType.UAC -> UPLOAD_SDK_UAC
            LinkSdkType.KOC -> UPLOAD_SDK_KOC
            LinkSdkType.NONE -> UPLOAD_SDK_NONE
        }
    }

    companion object {
        const val UPLOAD_SDK_FACEBOOK_DEEP_LINK = 0
        const val UPLOAD_SDK_FIREBASE = 1
        const val UPLOAD_SDK_AF_INSTALL = 2
        const val UPLOAD_SDK_HUAWEI = 5
        const val UPLOAD_SDK_KOCHAVA = 7
        const val UPLOAD_SDK_ADJUST = 8
        const val UPLOAD_SDK_GOOGLE = 12
        const val UPLOAD_SDK_CLIPBOARD = 14
        const val UPLOAD_SDK_CHANGDU_DEFAULT = 17
        const val UPLOAD_SDK_UAC = 18
        const val UPLOAD_SDK_KOC = 19
        const val UPLOAD_SDK_UNIVERSAL_LINK = 20
        const val UPLOAD_SDK_FACEBOOK_INSTALL = 22
        const val UPLOAD_SDK_AF_DEEP_LINK = 23
        const val UPLOAD_SDK_GOOGLE_ENGAGE = 24
        const val UPLOAD_SDK_NONE = -1
    }
}
