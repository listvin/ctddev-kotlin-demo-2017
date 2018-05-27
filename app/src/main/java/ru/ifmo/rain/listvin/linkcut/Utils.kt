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

fun Random.nextIntEx(bound: Int, vararg ex: Int): Int{
    var result: Int
    do {
        result = nextInt(bound)
    } while (result in ex)
    return result
}

fun AppCompatActivity.toast(text: String){
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun MyActivity.rundebug(){
//    perform("link",111, 139)
//    perform("conn",111, 139)
//    perform("size",139)
//    perform("link",111, 141)
}