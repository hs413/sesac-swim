package kr.sesacjava.swimtutor.health

import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HealthConnectJsInterface(private val healthConnectService: HealthConnectService): WebViewClient() {
//    @JavascriptInterface
//    fun getDataRecords(date: String) {
//        println("read")
//        CoroutineScope(Dispatchers.IO).launch {
//            val response = healthConnectService.getRecords(date)
//
//            val dataList = arrayListOf<Any>();
//
//            for (exerciseRecord in response.records) {
//                val results = awaitAll(
//                    async { healthConnectService.getDistanceData(exerciseRecord.startTime, exerciseRecord.endTime) },
//                    async { healthConnectService.getCaloriesData(exerciseRecord.startTime, exerciseRecord.endTime) },
//                    async { healthConnectService.getSpeedData(exerciseRecord.startTime, exerciseRecord.endTime) },
//                    async { healthConnectService.getHeartRateData(exerciseRecord.startTime, exerciseRecord.endTime) },
//                )
//                dataList.add(results.flatten())
//            }
//
//            val jsonString = Gson().toJson(dataList)
//
//            withContext(Dispatchers.Main) {
//                sendDataToJavaScript(jsonString)
//            }
//        }
//    }

//    private fun sendDataToJavaScript(data: String) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            webView.evaluateJavascript("javascript:receiveDataFromKotlin('$data')", null)
//        } else {
//            webView.loadUrl("javascript:receiveDataFromKotlin('$data')")
//        }
//    }


}