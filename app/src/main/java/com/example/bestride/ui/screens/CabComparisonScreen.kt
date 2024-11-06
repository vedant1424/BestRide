package com.example.bestride.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.os.Message
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import java.net.URLEncoder

data class LocationDetails(
    val lat: Double,
    val lng: Double,
    val name: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CabComparisonScreen(
    navController: NavHostController,
    sourceLat: Double?,
    sourceLng: Double?,
    destLat: Double?,
    destLng: Double?
) {
    val sourceLocation = LocationDetails(
        lat = sourceLat ?: 0.0,
        lng = sourceLng ?: 0.0,
        name = "Source Location"
    )

    val destLocation = LocationDetails(
        lat = destLat ?: 0.0,
        lng = destLng ?: 0.0,
        name = "Destination Location"
    )

    val tabs = listOf("Ola", "Uber", "Rapido")
    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding() // Add system bars padding
        ) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(text = title) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                    ) {
                        when (page) {
                            0 -> WebViewComponent(
                                url = buildOlaUrl(sourceLocation, destLocation)
                            )
                            1 -> WebViewComponent(
                                url = buildUberUrl(sourceLocation, destLocation)
                            )
                            2 -> WebViewComponent(
                                url = buildRapidoUrl(sourceLocation, destLocation)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WebViewComponent(url: String) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    // Add state to track if it's Uber
    val isUberUrl = remember(url) {
        url.contains("uber", ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DisposableEffect(url) {
            onDispose {
                webView?.stopLoading()
                webView?.clearCache(true)
                webView?.clearHistory()
                webView = null
            }
        }

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webView = this
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString = "Mozilla/5.0 (Linux; Android 10; SM-A505F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Mobile Safari/537.36"
                        setGeolocationEnabled(true)
                        javaScriptCanOpenWindowsAutomatically = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        allowContentAccess = true
                        allowFileAccess = true
                    }

                    WebView.setWebContentsDebuggingEnabled(true)

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                            Log.d("WebView", "Page started loading: $url")
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            Log.d("WebView", "Page finished loading: $url")

                            // Only inject for Uber
                            if (isUberUrl) {
                                val javascript = """
                                    (function() {
                                        function injectPhoneNumber() {
                                            console.log('Attempting to find phone input...');
                                            
                                            // More specific selectors based on the HTML structure
                                            const phoneInput = document.querySelector('input[aria-label="Enter phone number or email"]') || 
                                                             document.querySelector('div[class*="css"] input') ||
                                                             document.querySelector('input[placeholder*="phone"]');
                                            
                                            if (phoneInput) {
                                                console.log('Found phone input field');
                                                // Set the value
                                                phoneInput.value = '9999999999';
                                                
                                                // Create and dispatch events to trigger Uber's validation
                                                const inputEvent = new InputEvent('input', {
                                                    bubbles: true,
                                                    cancelable: true,
                                                });
                                                phoneInput.dispatchEvent(inputEvent);
                                                
                                                // Also dispatch change event
                                                const changeEvent = new Event('change', {
                                                    bubbles: true,
                                                    cancelable: true
                                                });
                                                phoneInput.dispatchEvent(changeEvent);
                                                
                                                console.log('Phone number injected and events dispatched');
                                                
                                                // Look for the continue button with various selectors
                                                const continueButton = 
                                                    document.querySelector('button[data-testid="next-button"]') ||
                                                    document.querySelector('button[type="submit"]') ||
                                                    Array.from(document.getElementsByTagName('button'))
                                                        .find(button => 
                                                            button.textContent.toLowerCase().includes('continue') || 
                                                            button.innerText.toLowerCase().includes('continue')
                                                        );
                                                
                                                if (continueButton) {
                                                    console.log('Found continue button');
                                                    setTimeout(() => {
                                                        // Create and dispatch click event
                                                        const clickEvent = new MouseEvent('click', {
                                                            bubbles: true,
                                                            cancelable: true,
                                                            view: window
                                                        });
                                                        continueButton.dispatchEvent(clickEvent);
                                                        console.log('Clicked continue button');
                                                    }, 1000);
                                                } else {
                                                    console.log('Continue button not found');
                                                }
                                            } else {
                                                console.log('Phone input not found, retrying...');
                                                // Retry after a delay
                                                setTimeout(injectPhoneNumber, 1000);
                                            }
                                        }
                                        
                                        // Add a mutation observer to handle dynamic content loading
                                        const observer = new MutationObserver((mutations) => {
                                            console.log('DOM changed, attempting injection...');
                                            injectPhoneNumber();
                                        });
                                        
                                        // Start observing
                                        observer.observe(document.body, {
                                            childList: true,
                                            subtree: true
                                        });
                                        
                                        // Initial attempt
                                        injectPhoneNumber();
                                    })()
                                """.trimIndent()

                                view?.evaluateJavascript(javascript) { result ->
                                    Log.d("WebView", "JavaScript result: $result")
                                }
                            }
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean = false

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            Log.e("WebView", "Error loading ${request?.url}: ${error?.description}")
                            super.onReceivedError(view, request, error)
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onGeolocationPermissionsShowPrompt(
                            origin: String?,
                            callback: GeolocationPermissions.Callback?
                        ) {
                            callback?.invoke(origin, true, false)
                        }

                        override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                            Log.d("WebView Console", "${message.message()} -- From line ${message.lineNumber()} of ${message.sourceId()}")
                            return true
                        }
                    }

                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun buildOlaUrl(source: LocationDetails, dest: LocationDetails): String {
    return buildString {
        append("https://book.olacabs.com/?")
        append("serviceType=p2p")
        append("&utm_source=widget_on_olacabs")
        append("&lat=${source.lat}")
        append("&lng=${source.lng}")
        append("&pickup_name=${URLEncoder.encode(source.name, "UTF-8")}")
        append("&drop_lat=${dest.lat}")
        append("&drop_lng=${dest.lng}")
        append("&drop_name=${URLEncoder.encode(dest.name, "UTF-8")}")
    }
}

private fun buildUberUrl(source: LocationDetails, dest: LocationDetails): String {
    return buildString {
        append("https://m.uber.com/looking?")
        append("pickup_lat=${source.lat}")
        append("&pickup_lng=${source.lng}")
        append("&dropoff_lat=${dest.lat}")
        append("&dropoff_lng=${dest.lng}")
    }
}

private fun buildRapidoUrl(source: LocationDetails, dest: LocationDetails): String {
    return "https://app.rapido.bike"
}