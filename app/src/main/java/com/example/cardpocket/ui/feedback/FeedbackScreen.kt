package com.example.cardpocket.ui.feedback

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cardpocket.ui.components.CircularIconButton
import com.example.cardpocket.ui.theme.Bg
import com.example.cardpocket.ui.theme.DividerColor
import com.example.cardpocket.ui.theme.Terracotta
import com.example.cardpocket.ui.theme.TextMuted
import com.example.cardpocket.ui.theme.TextPrimary

/** 兔小巢产品 ID：802549，反馈页即 Web App。 */
private const val TUCAO_PRODUCT_ID = "802549"
private const val TUCAO_URL = "https://support.qq.com/products/$TUCAO_PRODUCT_ID"

/**
 * 反馈与建议：内嵌 WebView 加载兔小巢反馈页。
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FeedbackScreen(onBack: () -> Unit) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var failed by remember { mutableStateOf(false) }

    BackHandler {
        val wv = webViewRef
        if (wv != null && wv.canGoBack()) wv.goBack() else onBack()
    }

    DisposableEffect(Unit) {
        onDispose { webViewRef?.destroy() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularIconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "反馈与建议",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            // 与左侧返回按钮等宽，保证标题居中
            Spacer(Modifier.width(40.dp))
        }
        HorizontalDivider(color = DividerColor)

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.setSupportZoom(false)
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                failed = false
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true) {
                                    failed = true
                                }
                            }
                        }
                        loadUrl(TUCAO_URL)
                    }.also { webViewRef = it }
                }
            )

            if (failed) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "反馈页面加载失败，请检查网络",
                        fontSize = 14.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = {
                        failed = false
                        webViewRef?.reload()
                    }) {
                        Text("重新加载", color = Terracotta)
                    }
                }
            }
        }
    }
}
