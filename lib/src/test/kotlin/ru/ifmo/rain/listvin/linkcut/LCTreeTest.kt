package ru.ifmo.rain.listvin.linkcut

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class LCTreeTest {
    internal var lastLCTree: LCTree? = null
    internal var lastSlowLC: SlowLinkCut? = null

    @Test
    fun mixedTiny() = mixed(5, 10, 1_000_000)

    @Test
    fun mixedSmall() = mixed(30, 70, 100_000)

    @Test
    fun mixed() = mixed(5000, 1000, 1000)

    @Test
    fun mixedBig() = mixed(100_000, 100_000, 10)

    internal fun mixed(treeCnt: Int, seqOps: Int, cycles: Int, cycleNum: Int = -1, opList: List<Int>? = null) {
        assert(treeCnt >= 2)
        val stats = mutableMapOf<String, Int>()
        repeat(cycles, { cycle: Int ->
            if (cycleNum != -1 && cycleNum != cycle) return@repeat
//            println("cycle $cycle")
            val rand = Random(Rand[cycle].toLong())

            val slowlc = SlowLinkCut(treeCnt)
            val lctree = LCTree(treeCnt)
            var opCode = -1
            repeat(seqOps, { op: Int ->
                try {
                    if (cycleNum != -1) {
//                        drawNodes(lctree.filament, "aux")
//                        drawNodes(lctree.filamentSz, "sz")
                        print("operation $op")
                    }
                    val name: String
                    var a = rand.nextInt(treeCnt)
                    var b = rand.nextIntEx(treeCnt, a)
//                    selectedNodes.add(a)
//                    selectedNodes.add(b)
                    val eIdx = rand.nextInt(Int.MAX_VALUE)
                    opCode = if (opList == null) rand.nextInt(5) else opList[rand.nextInt(opList.size)]
                    name = when (opCode) {
                        0,4 -> {
                            slowlc.link(a, b)
                            if (slowlc.successful) {
                                lctree.link(a, b)
                                "link"
                            } else "skip"
                        }
                        1 -> {
                            if (slowlc.edgesCnt > 0) {
                                val p = slowlc.disconnect(eIdx % slowlc.edgesCnt)
                                a = p!!.first; b = p.second
                                lctree.cut(a, b)
                                "cut"
                            } else "skip"
                        }
                        2 -> {
                            assertEquals(slowlc.connected(a, b), lctree.connected(a, b))
                            "connected"
                        }
                        3 -> {
//                            selectedNodes.remove(b)
                            assertEquals(slowlc.size(a), lctree.size(a))
                            "size"
                        }
                        else -> "notImplemented"
                    }
                    if (cycleNum != -1) println("\t//$name $a $b")
                    stats[name] = (stats[name] ?: 0) + 1
                } catch (thr: Throwable) {
                    println("\nerror on op $op at cycle $cycle, opCode $opCode")
                    throw thr
                }
            })

            lastLCTree = lctree
            lastSlowLC = slowlc
        })
        if (cycles != 1) {
            assert(cycleNum == -1)
            print("\n${Throwable().stackTrace[2].methodName} tested:")
        }
        for (p in stats) {
            print(" ${p.key} x${p.value},")
        }
        println()
    }
}