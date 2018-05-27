package ru.ifmo.rain.listvin.linkcut

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.*
import com.example.demo.R
import android.webkit.*
import ru.ifmo.rain.listvin.dsl.*
import kotlin.math.max
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import android.content.res.TypedArray
import java.util.*
import kotlin.math.absoluteValue


class MyActivity : AppCompatActivity(){
    var random = Random()
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

    lateinit var fireAction: MenuItem
    lateinit var helpAction: MenuItem
    lateinit var diceAction: MenuItem
    lateinit var editTextA: EditText
    lateinit var editTextB: EditText
    lateinit var statusView: TextView
    lateinit var historyView: TextView
    lateinit var historyButton: ImageButton

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //FIXME
        lateinit var dice: List<Drawable>
        resources.apply { dice = (1..6).map{ getDrawable(getIdentifier("ic_dice_$it", "drawable", packageName),theme).apply { setTint(Color.WHITE) } } }
        val styledAttributes = theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        val verPad = styledAttributes.getDimension(0, 0f).toInt()
        styledAttributes.recycle()

        menu!!.items {
            helpAction = menuItem("help") {
                imageView(verPad,this@MyActivity, resources.getDrawable(R.drawable.ic_help_outline_black_24dp, theme).apply { setTint(Color.WHITE) }).onClick {
                    toast("help not implemented yet")
                }
            }

            diceAction = menuItem("dice") {
                imageView(verPad,this@MyActivity, dice[0]).onClick {
                    startAnimation(RotateAnimation(
                            0f, 360f,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f).apply {
                        duration = 250
                        setAnimationListener(object : Animation.AnimationListener{
                            override fun onAnimationRepeat(animation: Animation?) {}
                            override fun onAnimationEnd(animation: Animation?) {
                                setImageDrawable(dice[random.nextInt(6)])
                            }
                            override fun onAnimationStart(animation: Animation?) {}
                        })
                    })

                    fun setEdits(a: Int, b: Int = DONT_CHANGE){
                        editTextA.setText(a.toString())
                        if (b != DONT_CHANGE) editTextB.setText(b.toString())
                    }
                    when(selectedOperation) {
                        "new" -> setEdits(random.nextInt(representation.threshold-2) +2)
                        "size" -> setEdits(random.nextInt(representation.size))
                        "link" -> if (representation.edgesCnt < representation.size-1) {
                            var a: Int
                            var b: Int
                            //do {
                                a = random.nextInt(representation.size)
                                b = random.nextIntEx(representation.size, a)
                            //} while (representation.connected(a,b)) //this changes state of LCTree, but don't want extra copy:(
                            setEdits(a,b)
                        }
                        "connected","conn" -> {
                            val a = random.nextInt(representation.size)
                            val b = random.nextIntEx(representation.size, a)
                            setEdits(a,b)
                        }
                        "cut" -> if (representation.edgesCnt > 0) {
                            val (a, b) = representation.getEdge(random.nextInt().absoluteValue)!!
                            setEdits(a,b)
                        }
                    }
                }
            }

            fireAction = menuItem("fire") {
                imageView(verPad,this@MyActivity, resources.getDrawable(R.drawable.ic_fire, theme).apply { setTint(Color.WHITE) }).onClick {
                    perform(selectedOperation,
                            editTextA.text.toString().toIntSafe() ?: 0,
                            editTextB.text.toString().toIntSafe() ?: 0)
                }
            }
        }

        return true
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        verticalLayout {
            horizontalLayout {
                spinner {
                    val adapter = ArrayAdapter.createFromResource(this@MyActivity, R.array.operations_array, android.R.layout.simple_spinner_item)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    this.adapter = adapter
                }.onItemSelected {
                    synchronized(argumentsExpected) {
                        selectedOperation = it
                        argumentsExpected = when (selectedOperation) {
                            "size" -> 1
                            "new", "gen" -> 0
                            else -> 2
                        }
                        lastedit = max(argumentsExpected - 1, 0)
                    }
                    @SuppressLint
                    when (selectedOperation) {
                        "new" ->
                            editTextA.apply { if (hint != "size") { hint = "size"; text.clear() } }
                        "conn","connected","cut","link","size" ->
                            editTextA.apply { if (hint != "node") { hint = "node"; text.clear() } }
                        "gen" ->
                            {} //TODO gen hints
                    }
                    editTextB.visibility = if (argumentsExpected <= 1) View.INVISIBLE else View.VISIBLE
                }.lparams(
                        leftMargin = 4
                )


                editTextA = editNumber (3) {
                    hint = "node"
                }.onFocusChange {
                    synchronized(argumentsExpected) { lastedit = if (it) 1 % max(1, argumentsExpected) else 0 }
                }.lparams(
                        leftMargin = 4
                )


                editTextB = editNumber (3) {
                    hint = "node"
                }.lparams(
                        leftMargin = 8
                )

                space {}.lparams(
                        width = 0,
                        weight = 1f
                )

                historyButton = imageButton {
                    setImageResource(R.drawable.ic_history_black_24dp)
                }.onClick {
                    historyView.apply {
                        if (width == 0)
                            lparams(width = WRAP_CONTENT, leftMargin = 4)
                        else
                            lparams(width = 0)
                    }
                }
            }


            statusView = textView {
                text = "Hello, DSL!"
                setSingleLine(false)
            }.lparams(
                    width = MATCH_PARENT,
                    height = WRAP_CONTENT,
                    leftMargin = 16,
                    topMargin = 8,
                    rightMargin = 8,
                    bottomMargin = 8
            )


            horizontalLayout {
                webView {
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            perform("new", 12)
                            rundebug()
                            statusView.setText("last operation status will be displayed here")
                        }
                    }
                    settings.javaScriptEnabled = true
                    loadUrl("file:///android_asset/main.html")
                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun touched(id: Int) {
                            synchronized(argumentsExpected) {
                                if (argumentsExpected == 0) return
                                lastedit = (1 - lastedit) % argumentsExpected
                                Message.obtain(this@MyActivity.handler, lastedit, id).sendToTarget()
                            }
                        }

                        @JavascriptInterface
                        fun toast(text: String) = this@MyActivity.toast(text)
                    }, "Feedback")
                    representation = Representation(this)
                    //TODO set height
                    //Resources.getSystem().displayMetrics.heightPixels
                }.lparams(
                        width = 0,
                        height = MATCH_PARENT,
                        weight = 1f,
                        focusable = false
                )

                historyView = textView {//TODO possibly need scrollview with dummy here
                    setSingleLine(false)
                    scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
                    setTextIsSelectable(true)
                }.lparams(
                        width = 0,
                        height = MATCH_PARENT
                )
            }.lparams(
                    width = MATCH_PARENT,
                    height = 0,
                    weight = 1f
            )
        }
    }

    private lateinit var representation: Representation
    private val NO_ARG = -1577384758
    fun perform(op: String, a: Int, b: Int = NO_ARG) {
        var explanation = "this feature is not implemented yet"
        var customMessage: String? = null
        var reply = ""
        if (try {
                    if (a == b && argumentsExpected == 2) {
                        explanation = "nodes are expected to be distinct."
                        false
                    } else when (op) {
                        "new" -> {
                            if (!representation.new(a)) {
                                customMessage = "despite visualisation for $a nodes will be missing, processing will be complete as usual"
                            }
                            true
                        }
                        "link" -> {
                            representation.link(a, b)
                            true
                        }
                        "cut" -> {
                            if (representation.cut(a, b)) {
                                true
                            } else {
                                explanation = "can cut only existing specific edges"
                                false
                            }
                        }
                        "connected", "conn" -> {
                            val r = representation.connected(a, b)
                            customMessage = "Nodes $a & $b are" +
                                    (if (r) "" else " NOT") +
                                    " connected."
                            reply = if (r) " yes" else " no"
                            true
                        }
                        "size" -> {
                            val r = representation.size(a)
                            customMessage = "Size of node number $a tree is $r"
                            reply = " = $r"
                            true
                        }
                        else -> {
                            false
                        }
                    }
                } catch (e: IndexOutOfBoundsException) {
                    explanation = "node numbers must be in [0;${representation.size})"
                    false
                } catch (e: IllegalStateException) {
                    explanation = "these nodes are already connected"
                    false
                }) {
            @SuppressLint
            statusView.text = customMessage ?: "" +
                    "'$op $a" +
                    (if (argumentsExpected == 2) " $b" else "") +
                            "' performed successfully"
            if (op == "new") historyView.clearComposingText()
            @SuppressLint
            historyView.text = "${historyView.text}${if (op == "connected") "conn" else op} $a${if (argumentsExpected == 2 && b != NO_ARG) " $b" else ""}$reply\n"
        } else {
            @SuppressLint
            statusView.text = "$selectedOperation failed, because $explanation"
        }
    }
}