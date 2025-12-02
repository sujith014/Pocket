package com.sujith.pocket.ui.extensions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object UrlUtils {
    
    // User agent to mimic a real browser
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"
    private const val TIMEOUT_MS = 10000 // 10 seconds
    private const val CONNECT_TIMEOUT_MS = 5000 // 5 seconds

    fun extractDomain(url: String): String {
        return try {
            val uri = java.net.URI(url)
            uri.host ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun fetchPageTitle(url: String): String {
        return try {
            val doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get()
            extractTitle(doc)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Extract title from document with multiple fallback strategies
     */
    private fun extractTitle(doc: Document): String {
        // Try multiple selectors in order of preference
        return doc.select("meta[property=og:title]").attr("content").takeIf { it.isNotEmpty() }
            ?: doc.select("meta[name=twitter:title]").attr("content").takeIf { it.isNotEmpty() }
            ?: doc.select("title").text().takeIf { it.isNotEmpty() }
            ?: doc.select("h1").firstOrNull()?.text()?.takeIf { it.isNotEmpty() }
            ?: ""
    }

    /**
     * Fetch web info with improved error handling and fallbacks
     */
    suspend fun fetchWebInfo(url: String): WebInfo = withContext(Dispatchers.IO) {
        val domain = extractDomain(url)
        var title = ""
        var text = ""
        var images = emptyList<String>()
        var links = emptyList<String>()

        try {
            val doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .ignoreHttpErrors(false)
                .ignoreContentType(false)
                .get()

            // Extract title with fallbacks
            title = extractTitle(doc)
            
            // Extract text content
            text = try {
                doc.body()?.text() ?: ""
            } catch (e: Exception) {
                ""
            }

            // Extract images
            images = try {
                doc.select("img[src]").mapNotNull { img ->
                    img.absUrl("src").takeIf { it.isNotEmpty() }
                }
            } catch (e: Exception) {
                emptyList()
            }

            // Extract links
            links = try {
                doc.select("a[href]").mapNotNull { link ->
                    link.absUrl("href").takeIf { it.isNotEmpty() }
                }
            } catch (e: Exception) {
                emptyList()
            }

        } catch (e: java.net.SocketTimeoutException) {
            // Timeout - use fallback values
            title = domain.ifEmpty { "Untitled" }
        } catch (e: java.net.UnknownHostException) {
            // Network error - use fallback values
            title = domain.ifEmpty { "Untitled" }
        } catch (e: org.jsoup.HttpStatusException) {
            // HTTP error (404, 403, etc.) - try to extract what we can
            title = domain.ifEmpty { "Untitled" }
        } catch (e: Exception) {
            // Any other error - use fallback values
            title = domain.ifEmpty { "Untitled" }
        }

        // Final fallback: if title is still empty, use domain or "Untitled"
        if (title.isEmpty()) {
            title = domain.ifEmpty { "Untitled" }
        }

        WebInfo(
            title = title,
            domain = domain,
            text = text,
            images = images,
            links = links
        )
    }
}


data class WebInfo(
    val title: String,
    val domain: String,
    val text: String,
    val images: List<String>,
    val links: List<String>
)


