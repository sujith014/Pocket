package com.sujith.pocket.ui.screens.webview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.PaddingValues
import com.sujith.pocket.ui.components.CustomTopBar
import com.sujith.pocket.ui.components.LoadingOverlay
import com.sujith.pocket.ui.extensions.SnackbarAction
import com.sujith.pocket.ui.extensions.SnackbarController
import com.sujith.pocket.ui.extensions.SnackbarEvent
import com.sujith.pocket.ui.extensions.showToast
import com.sujith.pocket.utils.Constants
import com.sujith.pocket.utils.UrlHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * WebView Screen
 */
@Composable
fun WebScreenView(
    modifier: Modifier = Modifier,
    url: String? = "https://www.google.com/",
    uiState: WebViewScreenUIState,
    onProgressChange: (Int) -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    onPageTitleChange: (String) -> Unit,
    onCurrentUrlChange: (String) -> Unit,
    onNavigationStateChange: (Boolean, Boolean) -> Unit,
    onUrlInputChange: (String) -> Unit,
    onToggleUrlInput: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onShowDownloadDialog: () -> Unit,
    onDismissDownloadDialog: () -> Unit,
    onUpdateBottomBarVisibility: (Boolean) -> Unit,
    onClearBookmarkDuplicate: () -> Unit,
    onSaveToHistory: suspend (String, String) -> Boolean,
    onBookmarkClick: suspend (String, String) -> Boolean,
    onShowReaderMode: () -> Unit,
    onDismissReaderMode: () -> Unit,
    onReaderContentExtracted: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // WebView reference
    var webView by remember { mutableStateOf<WebView?>(null) }
    
    // Local UI state (not app state)
    var lastScrollTime by remember { mutableStateOf(0L) }
    var scrollVelocity by remember { mutableStateOf(0f) }
    var lastClickTime by remember { mutableStateOf(0L) }
    var hasRedirectedToFreedium by remember { mutableStateOf(false) }
    var mediumUrl by remember { mutableStateOf<String?>(null) }

    // Debounced click handler
    val debouncedClick = remember {
        { action: () -> Unit ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > Constants.CLICK_DEBOUNCE_DELAY) {
            lastClickTime = currentTime
            action()
        }
    }
    }


    // Auto-hide bottom bar on scroll
    LaunchedEffect(scrollVelocity) {
        if (scrollVelocity != 0f) {
            onUpdateBottomBarVisibility(false)
            lastScrollTime = System.currentTimeMillis()
        }
    }

    // Show bottom bar after scroll stops
    LaunchedEffect(lastScrollTime) {
        if (lastScrollTime > 0) {
            delay(Constants.BOTTOM_BAR_AUTO_HIDE_DELAY)
            if (System.currentTimeMillis() - lastScrollTime >= Constants.BOTTOM_BAR_AUTO_HIDE_DELAY) {
                onUpdateBottomBarVisibility(true)
                scrollVelocity = 0f
            }
        }
    }

    // Update WebView settings when desktop mode changes
    LaunchedEffect(uiState.isDesktopMode) {
        val view = webView ?: return@LaunchedEffect

        view.settings.apply {
            userAgentString = if (uiState.isDesktopMode) {
                // Desktop mode
                Constants.DESKTOP_USER_AGENT
            } else {
                // Mobile mode
                Constants.MOBILE_USER_AGENT
            }
            useWideViewPort = uiState.isDesktopMode
            loadWithOverviewMode = uiState.isDesktopMode
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }

        // Clear only in‑memory cache to force re-layout with new UA
        view.clearCache(false)

        // Force a full reload with current URL
        val current = view.url
        if (current != null) {
            view.loadUrl(current)
        } else {
            view.reload()
        }
    }

    // Medium URL detection
    LaunchedEffect(uiState.currentUrl) {
        if (UrlHelper.isMediumUrl(uiState.currentUrl) && !hasRedirectedToFreedium) {
            mediumUrl = uiState.currentUrl
            scope.launch {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = Constants.MESSAGE_MEDIUM_DETECTED, action = SnackbarAction(
                            label = Constants.MESSAGE_OPEN_IN_FREEDIUM
                        ) {
                            val freediumUrl = UrlHelper.convertToFreediumUrl(mediumUrl!!)
                            webView?.loadUrl(freediumUrl)
                            hasRedirectedToFreedium = true
                        })
                )
            }
        } else if (!UrlHelper.isMediumUrl(uiState.currentUrl)) {
            hasRedirectedToFreedium = false
        }
    }

    // Handle status messages (toasts)
    LaunchedEffect(uiState.status, uiState.isBookmarkDuplicate) {
        when {
            uiState.isBookmarkDuplicate -> {
                context.showToast(Constants.MESSAGE_ALREADY_BOOKMARKED)
                onClearBookmarkDuplicate()
            }

            uiState.status is WebViewStatus.Success -> {
                val message = (uiState.status as WebViewStatus.Success).message
                when {
                    message.contains(
                        "Bookmarked",
                        ignoreCase = true
                    ) && !message.contains("Already", ignoreCase = true) -> {
                        context.showToast(Constants.MESSAGE_BOOKMARKED)
                    }
                }
            }
        }
    }

    // Column layout (no Scaffold - parent has it)
    Column(modifier = modifier.fillMaxSize()) {
        // Custom Top Bar
        CustomTopBar(
            title = uiState.pageTitle,
            url = uiState.currentUrl,
            showUrlInput = uiState.showUrlInput,
            urlInput = uiState.urlInput,
            onUrlInputChange = onUrlInputChange,
            onToggleUrlInput = onToggleUrlInput,
            canGoBack = uiState.canGoBack,
            canGoForward = uiState.canGoForward,
            isLoading = uiState.isLoading,
            progress = uiState.progress,
            onBackClick = { debouncedClick { webView?.goBack() } },
            onForwardClick = { debouncedClick { webView?.goForward() } },
            onRefreshClick = {
                debouncedClick {
                if (uiState.isLoading) webView?.stopLoading() else webView?.reload() 
                }
            },
            onUrlSubmit = { urlInput ->
                debouncedClick {
                    var finalUrl = urlInput.trim()
                    if (finalUrl.isNotEmpty()) {
                        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                            finalUrl = if (finalUrl.contains(".")) {
                                "https://$finalUrl"
                            } else {
                                "https://www.google.com/search?q=${Uri.encode(finalUrl)}"
                            }
                        }
                        webView?.loadUrl(finalUrl)
                        onToggleUrlInput()
                    }
                }
            })

        // WebView Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AndroidView(
                factory = { ctx ->
                    createWebView(
                        context = ctx,
                        onProgressChanged = onProgressChange,
                        onReceivedTitle = onPageTitleChange,
                        onUrlChanged = onCurrentUrlChange,
                        onNavigationStateChanged = onNavigationStateChange,
                        onPageFinished = { pageUrl, pageTitle ->
                            scope.launch {
                                onSaveToHistory(pageUrl, pageTitle)
                            }
                        },
                        onReaderContentExtracted = onReaderContentExtracted,
                        onScrollChanged = { delta ->
                            if (kotlin.math.abs(delta) > 10) {
                                scrollVelocity = delta.toFloat()
                                lastScrollTime = System.currentTimeMillis()
                            }
                        },
                        initialUrl = url
                    ).also { webView = it }
                }, modifier = Modifier.fillMaxSize()
            )

            LoadingOverlay(isLoading = uiState.isLoading)
        }

        // Bottom Action Bar
        val isBottomBarVisible = uiState.isBottomBarVisible
        AnimatedVisibility(
            visible = isBottomBarVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeOut()
        ) {
            WebViewBottomBar(isDesktopMode = uiState.isDesktopMode, onBookmarkClick = {
                debouncedClick {
                    if (uiState.currentUrl.isNotEmpty()) {
                        scope.launch {
                            onBookmarkClick(
                                uiState.currentUrl, uiState.pageTitle.ifEmpty { "Untitled" })
                        }
                    }
                }
            }, onDownloadClick = {
                debouncedClick { onShowDownloadDialog() }
            }, onViewModeToggle = {
                debouncedClick {
                    onToggleDesktopMode()
                }
            }, onReaderModeClick = {
                debouncedClick {
                    webView?.triggerReadingMode()
                    onShowReaderMode()
                }
            })
        }
    }

    // Download Dialog
    if (uiState.showDownloadDialog) {
        DownloadDialog(onDismiss = onDismissDownloadDialog, onSavePDF = {
            debouncedClick {
                if (!uiState.isLoading) {
                    webView?.let {
                        saveAsPDFFullPage(context, it, uiState.pageTitle)
                    }
                    onDismissDownloadDialog()
                } else {
                    context.showToast("Please wait for page to finish loading")
                }
            }
        }, onSaveImage = {
            debouncedClick {
                if (!uiState.isLoading) {
                    webView?.let {
                        saveAsImageFullPage(context, it, uiState.pageTitle)
                    }
                    onDismissDownloadDialog()
                } else {
                    context.showToast("Please wait for page to finish loading")
                }
            }
        })
    }

    // Reader Mode Dialog
    if (uiState.showReaderMode) {
        ReaderModeDialog(
            readerContent = uiState.readerContent,
            pageTitle = uiState.pageTitle,
            onDismiss = onDismissReaderMode
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                stopLoading()
                clearHistory()
                clearCache(true)
                destroy()
            }
            webView = null
        }
    }
}

/**
 * Create and configure WebView
 */
private fun createWebView(
    context: Context,
    onProgressChanged: (Int) -> Unit,
    onReceivedTitle: (String) -> Unit,
    onUrlChanged: (String) -> Unit,
    onNavigationStateChanged: (Boolean, Boolean) -> Unit,
    onPageFinished: (String, String) -> Unit,
    onReaderContentExtracted: (String) -> Unit,
    onScrollChanged: (Int) -> Unit,
    initialUrl: String?
): WebView {
    return WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            loadsImagesAutomatically = true
                            javaScriptCanOpenWindowsAutomatically = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            allowFileAccess = true
                            allowContentAccess = true
                            userAgentString = Constants.MOBILE_USER_AGENT
                            cacheMode = WebSettings.LOAD_DEFAULT
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            useWideViewPort = false
                            loadWithOverviewMode = false
                            setSupportMultipleWindows(false)
                            safeBrowsingEnabled = true
                        }

        // Scroll listener
                        setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            onScrollChanged(scrollY - oldScrollY)
        }

        // JavaScript interface for reader mode
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun postMessage(text: String) {
                onReaderContentExtracted(text)
                            }
                        }, "ReadBridge")

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                onProgressChanged(newProgress)
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                onReceivedTitle(title ?: "")
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url?.toString() ?: return false

                if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("sms:") || url.startsWith(
                        "intent:"
                    )
                ) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    return true
                                }
                                return false
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                onUrlChanged(url ?: "")
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                onUrlChanged(url ?: "")
                onNavigationStateChanged(
                    view?.canGoBack() ?: false, view?.canGoForward() ?: false
                )

                                if (url != null && url.isNotEmpty()) {
                    val title = view?.title ?: "Untitled"
                    onPageFinished(url, title)
                                }
                            }

                            override fun shouldInterceptRequest(
                view: WebView?, request: WebResourceRequest?
                            ): WebResourceResponse? {
                                val host = request?.url?.host ?: return null
                                if (Constants.AD_HOSTS.any { host.contains(it, ignoreCase = true) }) {
                                    return WebResourceResponse("text/plain", "utf-8", null)
                                }
                                return super.shouldInterceptRequest(view, request)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: android.webkit.WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true) {
                    onProgressChanged(100)
                }
            }
        }

        initialUrl?.let { loadUrl(it) }
    }
}

/**
 * Bottom Action Bar Component
 */
@Composable
fun WebViewBottomBar(
    isDesktopMode: Boolean,
    onBookmarkClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onViewModeToggle: () -> Unit,
    onReaderModeClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
            BottomActionButton(
                icon = Icons.Default.BookmarkAdd, label = "Bookmark", onClick = onBookmarkClick
            )

            BottomActionButton(
                icon = Icons.Default.Download, label = "Download", onClick = onDownloadClick
            )

            BottomActionButton(
                icon = if (isDesktopMode) Icons.Default.Computer else Icons.Default.PhoneAndroid,
                label = if (isDesktopMode) "Desktop" else "Mobile",
                onClick = onViewModeToggle
            )

            BottomActionButton(
                icon = Icons.Default.MenuBook, label = "Reader", onClick = onReaderModeClick
            )
        }
    }
}

@Composable
fun BottomActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(72.dp)
            .height(56.dp)
    ) {
        IconButton(
            onClick = onClick, enabled = enabled, modifier = Modifier.size(40.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/**
 * Download Options Dialog - Responsive for all screen sizes
 */
@Composable
fun DownloadDialog(
    onDismiss: () -> Unit, onSavePDF: () -> Unit, onSaveImage: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title
                Text(
                    text = "Download Options",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Description
                Text(
                    text = "Choose format to save the current page:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // PDF Button
                    Button(
                        onClick = onSavePDF,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Save as PDF")
                    }
                    
                    // Image Button
                    Button(
                        onClick = onSaveImage,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Save as Image")
                    }
                    
                    // Cancel Button
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

/**
 * Reader Mode Dialog - Shows extracted content
 */
@Composable
fun ReaderModeDialog(
    readerContent: String, pageTitle: String, onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reader Mode",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    if (pageTitle.isNotEmpty()) {
                        Text(
                            text = pageTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    if (readerContent.isNotEmpty()) {
                        Text(
                            text = readerContent,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight.times(1.5f)
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Extracting content...")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Extension function to trigger reader mode
 */
fun WebView.triggerReadingMode() {
    val js = """
        (function(){
            try {
                // Try multiple selectors for article content
                var el = document.querySelector('article') || 
                         document.querySelector('[role="main"]') || 
                         document.querySelector('main') ||
                         document.querySelector('.article-content') ||
                         document.querySelector('.post-content') ||
                         document.querySelector('.entry-content') ||
                         document.body;
                
                // Get text content
                var txt = el.innerText || el.textContent || ''; 
                
                // Clean up excessive whitespace
                txt = txt.replace(/\s+/g, ' ').trim();
                
                // Send to Android
                if(window.ReadBridge && window.ReadBridge.postMessage){
                    window.ReadBridge.postMessage(txt);
                }
            } catch(e) {
                if(window.ReadBridge && window.ReadBridge.postMessage){ 
                    window.ReadBridge.postMessage('Error extracting content: ' + e.message); 
                }
            }
        })();
    """.trimIndent()

    evaluateJavascript(js, null)
}

/**
 * Save webpage as PDF - FIXED for full page capture
 */
fun saveAsPDFFullPage(context: Context, webView: WebView, title: String) {
    try {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val printAdapter = webView.createPrintDocumentAdapter(
            "${title.take(50)}_${System.currentTimeMillis()}"
        )

        val attributes = PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()

        val jobName = "${title.take(50)}_${System.currentTimeMillis()}"
        printManager.print(jobName, printAdapter, attributes)

        context.showToast("PDF print dialog opened")
    } catch (e: Exception) {
        context.showToast("Failed to create PDF: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Save webpage as Image - FIXED for full page capture
 */
fun saveAsImageFullPage(context: Context, webView: WebView, title: String) {
    try {
        // Get full page dimensions via JavaScript
        webView.evaluateJavascript(
            """
            (function() {
                var body = document.body;
                var html = document.documentElement;
                return JSON.stringify({
                    width: Math.max(
                        body.scrollWidth, body.offsetWidth,
                        html.clientWidth, html.scrollWidth, html.offsetWidth
                    ),
                    height: Math.max(
                        body.scrollHeight, body.offsetHeight,
                        html.clientHeight, html.scrollHeight, html.offsetHeight
                    )
                });
            })();
            """.trimIndent()
        ) { result ->
            try {
                // Parse JSON response
                val json = result.replace("\\", "").trim('"')
                val dimensions = org.json.JSONObject(json)
                val fullWidth = dimensions.getInt("width")
                val fullHeight = dimensions.getInt("height")

                // Limit height to prevent memory issues (max 20000px)
                val maxHeight = 20000
                val actualHeight = if (fullHeight > maxHeight) maxHeight else fullHeight

                // Create bitmap for full page
                val bitmap = Bitmap.createBitmap(
                    fullWidth, actualHeight, Bitmap.Config.ARGB_8888
                )

                // Draw WebView content
            val canvas = Canvas(bitmap)

                // Save current scroll position
                val originalScrollY = webView.scrollY

                // Scroll to top and capture
                webView.scrollTo(0, 0)
            webView.draw(canvas)

                // Restore scroll position
                webView.scrollTo(0, originalScrollY)

                // Save to file
            val fileName = "${title.take(50)}_${System.currentTimeMillis()}.png"
            val file = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName
            )

            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }

                bitmap.recycle()

            context.showToast("Image saved: ${file.name}")
            } catch (e: Exception) {
                context.showToast("Failed to save image: ${e.message}")
                e.printStackTrace()
            }
        }
    } catch (e: Exception) {
        context.showToast("Failed to capture page: ${e.message}")
        e.printStackTrace()
    }
}