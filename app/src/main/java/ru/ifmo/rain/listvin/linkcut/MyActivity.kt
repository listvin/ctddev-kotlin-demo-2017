package ru.ifmo.rain.listvin.linkcut

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.example.demo.R
import kotlinx.android.synthetic.main.activity_my.*
import android.webkit.*

class MyActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener {
    lateinit var adapter: ArrayAdapter<CharSequence>
    lateinit var selectedOperation: String
    var lastedit = 0
    var argumentsExpected = 2
    val handler = Handler(Looper.getMainLooper(), {
        synchronized(argumentsExpected) {
            when (it.what) {
                0 -> {
                    editTextA.setText(it.obj.toString())
                    if (argumentsExpected == 2) editTextB.requestFocus()
                }
                1 -> {
                    editTextB.setText(it.obj.toString())
                    editTextA.requestFocus()
                    Unit
                }
            }
        }
        true
    })
    lateinit var tree: LCTree;

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my)

        webView.getSettings().setJavaScriptEnabled(true)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                perform("new", 12)
            }
        }
        webView.loadUrl("file:///android_asset/main.html")
        webView.addJavascriptInterface(JSInterface(), "Feedback")

        adapter = ArrayAdapter.createFromResource(this,
                R.array.operations_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        selectedOperation = adapter.getItem(0).toString()
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        button.setOnClickListener(this)

        editTextA.onFocusChangeListener = View.OnFocusChangeListener { _:View, haveFocus:Boolean ->
            if (haveFocus) synchronized(argumentsExpected) {
                lastedit = 0
            }
        }
        editTextB.onFocusChangeListener = View.OnFocusChangeListener { _:View, haveFocus:Boolean ->
            if (haveFocus) synchronized(argumentsExpected) {
                lastedit = 1
            }
        }
    }

    inner class JSInterface {
        @JavascriptInterface
        fun touched(id: Int) {
            synchronized(argumentsExpected) {
                if (argumentsExpected == 0) return
                lastedit = (1 - lastedit) % argumentsExpected
                Message.obtain(handler, lastedit, id).sendToTarget()
            }
        }

        @JavascriptInterface
        fun alert(text: String){
            Toast.makeText(this@MyActivity, text, Toast.LENGTH_SHORT).show()
        }
    }

    private fun wvexec(com: String) = webView.loadUrl("javascript:graph.$com")

    private fun perform(op: String, arg1: Int, arg2: Int = 0) {
        var explanation = "this feature is not implemented yet"
        var customMessage: String? = null
        if (try {
                    when (op) {
                        "new" -> {
                            tree = LCTree(arg1)
                            wvexec("clear()")
                            for (i in 0 until arg1) wvexec("addNode($i)")
                            true
                        }
                        "link" -> {
                            tree.link(arg1, arg2)
                            wvexec("addLink($arg1, $arg2, '_t')") //TODO move to representation
                            true
                        }
                        "cut" -> {
                            if (tree.connected(arg1, arg2) /*&& TODO edge presence check*/) {
                                tree.cut(arg1, arg2)
                                wvexec("removeEdge($arg1, $arg2, '_t')") //TODO move to representation
                                true
                            } else {
                                explanation = "can cut only existing and specific edges"
                                false
                            }
                        }
                        "connected" -> {
                            if (arg1 == arg2) {
                                explanation = "nodes are expected to be different."
                                false
                            } else {
                                customMessage = "Nodes $arg1 & $arg2 are" +
                                        (if (tree.connected(arg1, arg2)) "" else " NOT") +
                                        " connected."
                                true
                            }
                        }
                        "size" -> {
                            customMessage = "Size of node number $arg1 tree is ${tree.size(arg1)}"
                            true
                        }
                        else -> {
                            false
                        }
                    }
                } catch (e: IndexOutOfBoundsException) {
                    explanation = "node numbers must be in [0;${tree.size})"
                    false
                } catch (e: IllegalStateException) {
                    explanation = "these nodes are already connected"
                    false
                }) {
            @SuppressLint
            textView.text = customMessage ?: "" +
                    "'$selectedOperation $arg1" +
                    (if (argumentsExpected == 2) " $arg2" else "") +
                            "' performed successfully"
        } else {
            @SuppressLint
            textView.text = "$selectedOperation failed, because $explanation"
        }
    }

    override fun onClick(v: View?) {
        val arg1 = editTextA.text.toString().toIntSafe() ?: 0
        val arg2 = editTextB.text.toString().toIntSafe() ?: 0
        perform(selectedOperation, arg1, arg2)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        throw IllegalArgumentException()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedOperation = parent!!.getItemAtPosition(position) as String
        synchronized(argumentsExpected) {
            argumentsExpected = when (selectedOperation) {
                "size" -> 1
                "new", "gen" -> 0
                else -> 2
            }
        }
        editTextB.visibility = if (argumentsExpected <= 1) View.INVISIBLE else View.VISIBLE
    }
}