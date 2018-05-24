package ru.ifmo.rain.listvin.linkcut

import java.io.PrintStream
import java.util.*

typealias Pint = Pair<Int,Int>


class Generator (var opsToGen: Int = 100_000, val size: Int = opsToGen, val density: Double = 1.0, val seed: Int = 239) {
    val stubborness = 100
    init {
        assert(size < opsToGen);
    }
//    var opsToGen = 100_000
    val rand = Random(seed.toLong())
    val slowlc = SlowLinkCut(size)
    val targetEdgesCnt = ((size-1)*density).toInt()
//    val map = mutableMapOf<String, FunctionCall>()

    val sbRef = StringBuilder()
    val sbIn = StringBuilder()
    fun genOp(name: String): Pint? {
        var temP: Pint?
        var attempts = 0
        do {
            attempts++
            val a = rand.nextInt(slowlc.size)
            val b = rand.nextIntEx(slowlc.size, a)
            val eIdx = rand.nextInt(Int.MAX_VALUE)
            temP = Pair(a,b)
            when(name) {
                "link" -> {
                    if (slowlc.edgesCnt == slowlc.size-1) return null
                    slowlc.link(a, b)
                }
                "cut" -> {
                    if (slowlc.edgesCnt == 0) return null
                    temP = slowlc.disconnect(eIdx % slowlc.edgesCnt)
                }
                "size" -> sbRef.append(slowlc.size(a)).append('\n')
                else -> throw NotImplementedError(name)
            }
        } while (!slowlc.successful && attempts < stubborness)
        return if (slowlc.successful) temP else null
    }

    fun printOp(name: String, args: Pint?){
        if (args == null) return
        opsToGen--
        if (name == "size") {
            sbIn.append("$name ${args.first+1}")
        } else {
            sbIn.append("$name ${args.first+1} ${args.second+1}")
        }
    }

    fun main() {
        println("${slowlc.size} ${opsToGen}")
        while (opsToGen > 0 && slowlc.edgesCnt < targetEdgesCnt) {
            if (opsToGen % 10000 == 0 && opsToGen > 0) System.err.println("$opsToGen left; edges count is ${slowlc.edgesCnt}")
            val name = if (rand.nextInt(10) < 8) "link" else if (rand.nextInt(10) < 8) "size" else "cut"
            printOp(name, genOp(name))
        }
        while (opsToGen > 0) {
            val name = when (rand.nextInt(3)) {
                0 -> "link"
                1 -> "cut"
                2 -> "size"
                else -> "fail"
            }
            printOp(name, genOp(name))
            if (opsToGen % 10000 == 0 && opsToGen > 0) System.err.println("$opsToGen left")
//            println(slowlc.edgesCnt)
        }
    }
}

fun main(args: Array<String>) {
//    if (args.size == 1) {
//        System.setOut(PrintStream(args[0]))
//        reInitInput()
//    }
//    Generator().main()

    val N = 100_000
    val Q = 100_000
    val dens = 51
    val cnt = 100
    for (i in 1 until cnt) {
        System.err.println("\tstarted ${i}th..")
        val name = "tests/randomN${N}Q${Q}_${i.toString().padStart(3,'0')}"
        System.setOut(PrintStream("$name.in"))
        reInitInput()
        val g = Generator(Q, N, dens/100.0, Rand[i])
        g.main()
        PrintStream("$name.in.ref").print(g.sbRef.toString())
        print(g.sbIn.toString())
    }

}