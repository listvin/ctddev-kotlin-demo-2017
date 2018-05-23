package ru.ifmo.rain.listvin.linkcut

fun String.toIntSafe(): Int? {
    return try {
        this.toInt();
    } catch (e: NumberFormatException) {
        null
    }
}