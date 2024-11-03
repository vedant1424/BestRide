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

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean = false
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onGeolocationPermissionsShowPrompt(
                            origin: String?,
                            callback: GeolocationPermissions.Callback?
                        ) {
                            callback?.invoke(origin, true, false)
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