package ru.ifmo.rain.listvin.dsl

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.*
import android.widget.LinearLayout.HORIZONTAL
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout.LayoutParams

@DslMarker
annotation class UIConstructor

@UIConstructor
fun AppCompatActivity.verticalLayout(init: LinearLayout.() -> Unit): LinearLayout {
    val layout = LinearLayout(this)
    layout.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    layout.orientation = LinearLayout.VERTICAL

    layout.init()

    this.setContentView(layout)
    return layout
}

//class ConstraintLayoutBuilder(context: Context): ConstraintLayout(context) {

private fun <T: View> LinearLayout.element(element: T, init: T.() -> Unit): T {
    this.addView(element, WRAP_CONTENT, WRAP_CONTENT)
    element.init()
    return element
}

val DONT_CHANGE = -1830472708

@UIConstructor
fun <T:View> T.lparams(
        width: Int = DONT_CHANGE,
        height: Int = DONT_CHANGE,
        leftMargin: Int = DONT_CHANGE,
        topMargin: Int = DONT_CHANGE,
        rightMargin: Int = DONT_CHANGE,
        bottomMargin: Int = DONT_CHANGE,
        gravity: Int = DONT_CHANGE,
        weight: Float = DONT_CHANGE.toFloat(),
        focusable: Boolean = isFocusableInTouchMode,
        visibility: Int = this.visibility
): T {
    val lp = LayoutParams(
            if (width == DONT_CHANGE) layoutParams.width else width,
            if (height == DONT_CHANGE) layoutParams.height else height
    )
    lp.setMargins(
            this.context.dp(if (leftMargin == DONT_CHANGE) lp.leftMargin else leftMargin),
            this.context.dp(if (topMargin == DONT_CHANGE) lp.leftMargin else topMargin),
            this.context.dp(if (rightMargin == DONT_CHANGE) lp.leftMargin else rightMargin),
            this.context.dp(if (bottomMargin == DONT_CHANGE) lp.leftMargin else bottomMargin))
    if (weight != DONT_CHANGE.toFloat()) lp.weight = weight
    if (gravity != DONT_CHANGE) lp.gravity = gravity
    layoutParams = lp
    isFocusableInTouchMode = focusable
    this.visibility = visibility
    return this
}

@UIConstructor
fun LinearLayout.button(init: Button.() -> Unit): Button = element(Button(this.context), init)

@UIConstructor
fun LinearLayout.imageButton(init: ImageButton.() -> Unit): ImageButton = element(ImageButton(this.context), init)

@UIConstructor
fun Button.onClick(listener: () -> Unit): Button {
    setOnClickListener { listener() }
    return this
}

@UIConstructor
fun LinearLayout.textView(init: TextView.() -> Unit): TextView = element(TextView(this.context), init)

@UIConstructor
fun LinearLayout.editNumber(ems: Int, init: EditText.() -> Unit): EditText{
    val editText = element(EditText(this.context), init)
    editText.inputType = InputType.TYPE_CLASS_NUMBER
    editText.setEms(ems)
    return editText
}

@UIConstructor
fun EditText.onFocusChange(listener: (Boolean) -> Unit): EditText {
    onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus -> listener(hasFocus) }
    return this
}

@UIConstructor
fun LinearLayout.spinner(init: Spinner.() -> Unit): Spinner = element(Spinner(this.context), init)

@UIConstructor
fun LinearLayout.space(init: Space.() -> Unit): Space = element(Space(this.context), init)

@UIConstructor
fun Spinner.onItemSelected(listener: (String) -> Unit): Spinner {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            listener(parent!!.getItemAtPosition(position) as String)
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {
            throw IllegalStateException()
        }
    }
    return this
}

@UIConstructor
fun LinearLayout.webView(init: WebView.() -> Unit): WebView = element(WebView(this.context), init)

@UIConstructor
fun LinearLayout.horizontalLayout(init: LinearLayout.() -> Unit): LinearLayout{
    val layout = element(LinearLayout(this.context), init)
    layout.orientation = HORIZONTAL
    layout.lparams(width = MATCH_PARENT)
    return layout
}

@UIConstructor
fun Menu.items(init: Menu.() -> Unit): Unit {
    this.init()
}

@UIConstructor
fun Menu.menuItem(title: String, init: MenuItem.() -> Unit): MenuItem {
    val item = this.add(title)
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    item.init()
    return item
}

@UIConstructor
fun MenuItem.imageView(verticalPad: Int, context: Context, drawable: Drawable): ImageView {
    val iv = ImageView(context)
    iv.setImageDrawable(drawable)
    val icon = context.dp(26)
    val horizontalPad = context.dp(8)
    iv.lparams(
            width = 2*horizontalPad + icon,
            height = 2*verticalPad + icon
    )
    iv.setPadding(horizontalPad, verticalPad, horizontalPad, verticalPad)
    val outValue = TypedValue()
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
    iv.setBackgroundResource(outValue.resourceId)
    actionView = iv
    return iv
}

fun <T:View> T.onClick(listener: T.() -> Unit): T {
    setOnClickListener { v ->
        @Suppress("UNCHECKED_CAST")
        (v as T).listener()
    }
    return this
}

fun Context.dp(dps: Int): Int {
    return (dps * resources.displayMetrics.density + 0.5f).toInt()
}

