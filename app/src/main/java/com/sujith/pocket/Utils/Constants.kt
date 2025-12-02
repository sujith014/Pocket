package com.sujith.pocket.utils

object Constants {
    // URLs
    const val FREEDIUM_BASE_URL = "https://freedium-mirror.cfd/"
    const val FREEDIUM_TAG = "freedium"
    const val MEDIUM_BASE_URL = "https://medium.com"
    const val MEDIUM_TAG = "medium"

    
    // WebView
    const val BOTTOM_BAR_AUTO_HIDE_DELAY = 1500L
    const val CLICK_DEBOUNCE_DELAY = 500L
    
    // User Agents
    const val DESKTOP_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"
    const val MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/537.36"
    
    // Ad blocking hosts
    val AD_HOSTS = listOf(
        "doubleclick.net",
        "googlesyndication.com",
        "adservice.google.com",
        "ads.twitter.com",
        "ads.yahoo.com",
        "adroll.com",
        "adsafeprotected.com",
        "adform.net",
        "googletagmanager.com",
        "googletagservices.com"
    )
    
    // Messages
    const val MESSAGE_BOOKMARKED = "Bookmarked!"
    const val MESSAGE_ALREADY_BOOKMARKED = "Already bookmarked"
    const val MESSAGE_SAVED_TO_HISTORY = "Saved to history"
    const val MESSAGE_ALREADY_IN_HISTORY = "Already in history"
    const val MESSAGE_MEDIUM_DETECTED = "Open this Medium article in Freedium for free access?"
    const val MESSAGE_OPEN_IN_FREEDIUM = "Open in Freedium"
}

