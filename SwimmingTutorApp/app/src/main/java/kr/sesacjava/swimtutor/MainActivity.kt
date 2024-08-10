package kr.sesacjava.swimtutor

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sesacjava.swimtutor.health.HealthConnectManager
import kr.sesacjava.swimtutor.health.HealthConnectService

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var healthConnectService: HealthConnectService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val healthConnectManager = HealthConnectManager(applicationContext)

        if (!healthConnectManager.getSdkStatus()) return

        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let { view?.loadUrl(it) }
                return true
            }
        }
        webView.settings.apply {
            useWideViewPort = true
            loadWithOverviewMode = true
        }
        webView.setInitialScale(100)

        val connectClient = healthConnectManager.getConnectClient()
        healthConnectService = HealthConnectService(applicationContext, connectClient)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                Log.e("WebView Error", "Error: ${error?.errorCode} ${error?.description}")
                super.onReceivedError(view, request, error)
            }
        }
        webView.addJavascriptInterface(this, "healthConnectJsInterface")
        webView.loadUrl("https://ssam.store")
//        webView.loadUrl("http://172.30.1.72:3000")
    }

    @JavascriptInterface
    fun getDataRecords(date: String) {
        println("read")
        CoroutineScope(Dispatchers.IO).launch {
            val response = healthConnectService.getRecords(date)

            val dataList = arrayListOf<Any>();

            for (exerciseRecord in response.records) {
                val results = awaitAll(
                    async { healthConnectService.getDistanceData(exerciseRecord.startTime, exerciseRecord.endTime) },
                    async { healthConnectService.getSpeedData(exerciseRecord.startTime, exerciseRecord.endTime) },
                    async { healthConnectService.getHeartRateData(exerciseRecord.startTime, exerciseRecord.endTime) },
                )
                dataList.add(results.flatten())
            }

            val jsonString = Gson().toJson(dataList)

            withContext(Dispatchers.Main) {
                sendDataToJavaScript(jsonString)
            }
        }
    }

    private fun sendDataToJavaScript(data: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript("javascript:receiveDataFromKotlin('$data')", null)
        } else {
            webView.loadUrl("javascript:receiveDataFromKotlin('$data')")
        }
    }
}
