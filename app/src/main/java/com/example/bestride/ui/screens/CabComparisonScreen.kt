
package com.example.bestride.ui.screens

import android.graphics.Bitmap
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bestride.presentation.state.UberAuthState
import com.example.bestride.presentation.viewmodel.CabComparisonViewModel
import com.example.bestride.presentation.viewmodel.UberAuthViewModel
import com.example.bestride.ui.dialogs.UberAuthDialog
import kotlinx.coroutines.launch
import java.net.URLEncoder

data class LocationDetails(
    val lat: Double,
    val lng: Double,
    val name: String
)

enum class CabType {
    OLA, UBER, RAPIDO
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CabComparisonScreen(
    navController: NavHostController,
    viewModel: CabComparisonViewModel = hiltViewModel()
) {
    val sourceLocation = remember {
        LocationDetails(
            lat = viewModel.sourceLat.toDouble(),
            lng = viewModel.sourceLng.toDouble(),
            name = viewModel.sourceAddress
        )
    }

    val destLocation = remember {
        LocationDetails(
            lat = viewModel.destLat.toDouble(),
            lng = viewModel.destLng.toDouble(),
            name = viewModel.destAddress
        )
    }

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
                .systemBarsPadding()
        ) {
            // Location Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pickup: ${sourceLocation.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Drop: ${destLocation.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Tab Row
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

            // Content Area
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
                            0 -> CabWebView(
                                url = buildOlaUrl(sourceLocation, destLocation),
                                cabType = CabType.OLA
                            )
                            1 -> CabWebView(
                                url = buildUberUrl(sourceLocation, destLocation),
                                cabType = CabType.UBER
                            )
                            2 -> CabWebView(
                                url = buildRapidoUrl(sourceLocation, destLocation),
                                cabType = CabType.RAPIDO
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CabWebView(
    url: String,
    cabType: CabType,
    viewModel: UberAuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var injectionInProgress by remember { mutableStateOf(false) }
    val isUberCab = cabType == CabType.UBER
    val authState by viewModel.authState.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                stopLoading()
                clearCache(true)
                clearHistory()
                removeAllViews()
                destroy()
            }
            viewModel.clearWebView()
            webView = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                        userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Mobile Safari/537.36"
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
                            if (isUberCab && !showAuthDialog) {
                                checkAuthenticationRequired(view) { required ->
                                    if (required) {
                                        viewModel.updateAuthState(UberAuthState.PhoneNumberInput)
                                        showAuthDialog = true
                                    }
                                }
                            }
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            Log.e("WebView", "Error: ${error?.description}")
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

                        override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                            Log.d(
                                "WebView Console",
                                "${message.message()} -- From line ${message.lineNumber()} of ${message.sourceId()}"
                            )
                            return true
                        }
                    }
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (showAuthDialog && isUberCab) {
            UberAuthDialog(
                viewModel = viewModel,
                onDismiss = {
                    showAuthDialog = false
                    viewModel.updateAuthState(UberAuthState.Initial)
                },
                onPhoneSubmit = { phoneNumber ->
                    if (!injectionInProgress) {
                        injectionInProgress = true
                        webView?.evaluateJavascript("""
            (function() {
                try {
                    function simulateKeyPress(element, key) {
                        const keydownEvent = new KeyboardEvent('keydown', {
                            key: key,
                            code: 'Digit' + key,
                            keyCode: key.charCodeAt(0),
                            which: key.charCodeAt(0),
                            bubbles: true,
                            cancelable: true
                        });
                        element.dispatchEvent(keydownEvent);

                        // Update value character by character
                        element.value = element.value + key;
                        element.dispatchEvent(new Event('input', { bubbles: true }));
                        element.dispatchEvent(new Event('change', { bubbles: true }));

                        element.dispatchEvent(new KeyboardEvent('keyup', {
                            key: key,
                            code: 'Digit' + key,
                            keyCode: key.charCodeAt(0),
                            which: key.charCodeAt(0),
                            bubbles: true,
                            cancelable: true
                        }));
                    }

                    async function injectFullNumber(number) {
                        const phoneInput = document.querySelector('#PHONE_NUMBER_or_EMAIL_ADDRESS');
                        if (!phoneInput) return false;

                        // Clear any existing value
                        phoneInput.value = '';
                        phoneInput.dispatchEvent(new Event('input', { bubbles: true }));
                        
                        // Focus the input
                        phoneInput.focus();
                        await new Promise(resolve => setTimeout(resolve, 1000));

                        // Type +
                        console.log('Typing +');
                        simulateKeyPress(phoneInput, '+');
                        await new Promise(resolve => setTimeout(resolve, 1500));

                        // Type 91
                        console.log('Typing 9');
                        simulateKeyPress(phoneInput, '9');
                        await new Promise(resolve => setTimeout(resolve, 1500));
                        
                        console.log('Typing 1');
                        simulateKeyPress(phoneInput, '1');
                        await new Promise(resolve => setTimeout(resolve, 1500));

                        // Type the actual phone number very slowly
                        for(let i = 0; i < number.length; i++) {
                            console.log('Typing digit:', number[i]);
                            simulateKeyPress(phoneInput, number[i]);
                            await new Promise(resolve => setTimeout(resolve, 1500));
                        }

                        // Final wait before clicking continue
                        await new Promise(resolve => setTimeout(resolve, 2000));
                        
                        const continueButton = Array.from(document.getElementsByTagName('button'))
                            .find(button => 
                                button.textContent.toLowerCase().includes('continue') &&
                                !button.textContent.toLowerCase().includes('google') &&
                                !button.textContent.toLowerCase().includes('apple')
                            );
                            
                        if (continueButton && !continueButton.disabled) {
                            console.log('Clicking continue button');
                            continueButton.click();
                        } else {
                            console.log('Continue button not found or disabled');
                        }
                    }

                    // Start the injection process
                    console.log('Starting full number injection');
                    injectFullNumber('${phoneNumber}');
                    return true;
                } catch(e) {
                    console.error('Error:', e);
                    return false;
                }
            })();
        """.trimIndent()) { result ->
                            injectionInProgress = false
                            Log.d("WebView", "Phone injection result: $result")
                        }
                    }
                },




                onOTPSubmit = { otp ->
                    if (!injectionInProgress) {
                        injectionInProgress = true
                        webView?.evaluateJavascript("""
                            (function() {
                                try {
                                    const otpInput = document.querySelector('input[type="tel"]') ||
                                                   document.querySelector('input[aria-label*="digit code"]') ||
                                                   document.querySelector('input[aria-label="Enter an OTP Code"]');
                                    
                                    if (!otpInput) {
                                        console.error('OTP input not found');
                                        return false;
                                    }
                                    
                                    otpInput.value = '${otp}';
                                    otpInput.dispatchEvent(new Event('input', { bubbles: true }));
                                    otpInput.dispatchEvent(new Event('change', { bubbles: true }));
                                    
                                    const verifyButton = Array.from(document.getElementsByTagName('button'))
                                        .find(button => 
                                            button.textContent.toLowerCase().includes('verify') ||
                                            button.textContent.toLowerCase().includes('continue')
                                        );
                                    
                                    if (!verifyButton) {
                                        console.error('Verify button not found');
                                        return false;
                                    }
                                    
                                    verifyButton.click();
                                    return true;
                                } catch(e) {
                                    console.error('Error:', e);
                                    return false;
                                }
                            })();
                        """.trimIndent()) { result ->
                            injectionInProgress = false
                            Log.d("WebView", "OTP injection result: $result")
                        }
                    }
                }
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading...")
            }
        }
    }
}

private fun checkAuthenticationRequired(webView: WebView?, callback: (Boolean) -> Unit) {
    val script = """
        (function() {
            const phoneInput = document.querySelector('input[placeholder*="phone"]') || 
                             document.querySelector('input[aria-label="Enter phone number or email"]') ||
                             document.querySelector('input[type="tel"]');
            return phoneInput ? true : false;
        })();
    """.trimIndent()

    webView?.evaluateJavascript(script) { result ->
        callback(result == "true")
    }
}

private fun buildOlaUrl(source: LocationDetails, dest: LocationDetails): String =
    buildString {
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

private fun buildUberUrl(source: LocationDetails, dest: LocationDetails): String =
    buildString {
        append("https://m.uber.com/looking?")
        append("pickup_lat=${source.lat}")
        append("&pickup_lng=${source.lng}")
        append("&dropoff_lat=${dest.lat}")
        append("&dropoff_lng=${dest.lng}")
    }

private fun buildRapidoUrl(source: LocationDetails, dest: LocationDetails): String =
    "https://app.rapido.bike"
