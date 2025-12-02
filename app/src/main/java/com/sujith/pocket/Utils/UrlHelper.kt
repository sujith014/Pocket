package com.sujith.pocket.utils

import com.sujith.pocket.utils.Constants.FREEDIUM_BASE_URL
import com.sujith.pocket.utils.Constants.FREEDIUM_TAG
import com.sujith.pocket.utils.Constants.MEDIUM_TAG

object UrlHelper {

    /*
    * Checking for Medium URL
    * & !url.contains(FREEDIUM_BASE_URL,ignoreCase = true) if the URL is already in Freedium
    * */
    fun isMediumUrl(url: String): Boolean {
        return url.contains(MEDIUM_TAG, ignoreCase = true) && !url.contains(
            FREEDIUM_TAG,
            ignoreCase = true
        )
    }

    fun convertToFreediumUrl(mediumUrl: String): String {
        return "$FREEDIUM_BASE_URL$mediumUrl"
    }
}

