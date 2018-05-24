package ru.ifmo.rain.listvin.linkcut

import java.io.*
import java.util.*

internal var sin = StreamTokenizer(BufferedReader(InputStreamReader(System.`in`)))
internal var sout = PrintWriter(System.out)

internal fun reInitInput(){
    sin = StreamTokenizer(BufferedReader(InputStreamReader(System.`in`)))
    sout = PrintWriter(System.out)
}

@Throws(IOException::class)
internal fun StreamTokenizer.nextI(): Int {
    nextToken()
    return nval.toInt()
}

@Throws(IOException::class)
internal fun StreamTokenizer.nextS(): String {
    nextToken()
    return sval
}

internal fun pause(){
    System.err.print("Press Enter...")
    System.`in`.read()
    System.err.println(" OK")
}

fun genRandomList(len: Int) =
        Random(len.toLong())
                .ints(len.toLong(), 100, 1000)
                .toArray().toMutableList()

fun genUniqueRandomList(len: Int) : MutableList<Int> {
    val result = (1..len).toMutableList()
    Collections.shuffle(result, Random(len.toLong()))
    return result
}

fun genCharList(len: Int): MutableList<Char> {
    val sample = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    assert(len <= sample.size, { "$len is too long for char list. max ${sample.size}" })
    return sample.subList(0, len).toMutableList()
}

object Rand {
    private val random = Random()
    operator fun get(i: Int): Int {
        random.setSeed(i.toLong())
        return Math.abs(random.nextInt())
    }
}

fun Random.nextIntEx(bound: Int, vararg ex: Int): Int{
    var result: Int
    do {
        result = nextInt(bound)
    } while (result in ex)
    return result
}