package ru.ifmo.rain.listvin.linkcut

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import java.util.*
import kotlin.math.absoluteValue

fun String.toIntSafe(): Int? {
    return try {
        this.toInt();
    } catch (e: NumberFormatException) {
        null
    }
}

val random = Random()
fun rand() = random.nextInt().absoluteValue

fun AppCompatActivity.toast(text: String){
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun MyActivity.rundebug(){
//    perform("link",111, 139)
//    perform("conn",111, 139)
//    perform("size",139)
//    perform("link",111, 141)
}