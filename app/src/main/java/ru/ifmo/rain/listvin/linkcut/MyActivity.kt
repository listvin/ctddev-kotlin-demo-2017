package ru.ifmo.rain.listvin.linkcut

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.example.demo.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_my.*
import android.R.attr.fragment
import android.webkit.*
import android.widget.Toast

class MyActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener {
    lateinit var adapter: ArrayAdapter<CharSequence>
    var selectedOperation: String? = null
    var lastedit = 0
    var argumentsExpected = 2
    var argument = arrayOfNulls<Int>(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my)

        val spinner = findViewById<View>(R.id.spinner) as Spinner

        adapter = ArrayAdapter.createFromResource(this,
                R.array.operations_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        button7.setOnClickListener(this)

        editTextA.onFocusChangeListener = View.OnFocusChangeListener({v: View?, hasFocus: Boolean ->
            synchronized(argumentsExpected) {
                if (!hasFocus && argument[0] != null) {
                    (v as EditText).setText(argument[0]!!.toString())
                    argument[0] = null
                }
            }
        })
        editTextB.onFocusChangeListener = View.OnFocusChangeListener({v: View?, hasFocus: Boolean ->
            synchronized(argumentsExpected) {
                if (!hasFocus && argument[1] != null) {
                    (v as EditText).setText(argument[1]!!.toString())
                    argument[1] = null
                }
            }
        }) //TODO fix copypaste

        val webView = findViewById<View>(R.id.graph) as WebView
        webView.getSettings().setJavaScriptEnabled(true)
        webView.setWebViewClient(object : WebViewClient(){})
        webView.loadUrl("file:///android_asset/main.html")
        webView.addJavascriptInterface(JSInterface(), "Feedback")
    }

    inner class JSInterface {
        @JavascriptInterface
        fun touched(id: String){
            synchronized(argumentsExpected) {
                if (argumentsExpected == 0) return
                lastedit = if (lastedit % argumentsExpected == 0) {
                    editTextA.dispatchWindowFocusChanged(false)
                    1
                } else {
                    editTextB.dispatchWindowFocusChanged(false)
                    0
                } % argumentsExpected
                argument[lastedit] = id.toInt()
            }
        }
        @JavascriptInterface
        fun getNum(): Int {
//            return if (editTextA?.text != "") 0
//            else editTextA.text.toString().toInt()
            return 0 //TODO
        }
    }

    override fun onClick(v: View?) {
        (findViewById<View>(R.id.textView) as TextView).text = "${selectedOperation} ${editTextA.text} ${editTextB.text}"
        display()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        synchronized(argumentsExpected) {
            argumentsExpected = 2
        }
        selectedOperation = null
    }
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedOperation = parent!!.getItemAtPosition(position) as String
        synchronized(argumentsExpected) {
            argumentsExpected = when(selectedOperation) {
                "size" -> 1
                "new","gen" -> 0
                else -> 2
            }
        }
    }

    var cnt = 2
    fun display() {
        for (i in 0..cnt)
            (this.findViewById<View>(R.id.graph) as WebView).loadUrl("javascript:graph.addLink($i,$cnt)")
        ++cnt
    }
}