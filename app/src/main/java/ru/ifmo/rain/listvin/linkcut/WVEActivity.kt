package ru.ifmo.rain.listvin.linkcut

import android.app.Activity
import android.os.Bundle
import com.example.demo.R
import kotlinx.android.synthetic.main.activity_wve.*

class WVEActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wve)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("file:///android_asset/sample.html")
        webView.addJavascriptInterface(WebAppInterface(this), "Android")
    }
}