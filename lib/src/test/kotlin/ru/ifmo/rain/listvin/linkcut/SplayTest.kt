package ru.ifmo.rain.listvin.linkcut

import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SplayTest {
    private val factoryI = Splay.Factory<Int>()
    private val factoryC = Splay.Factory<Char>()
    private lateinit var random: Random

    @Before
    fun init() {
        random = Random(239)
    }

    @Test
    fun size0() {
        assertEquals(0, factoryI.fromList(emptyList()).size)
    }

    @Test
    fun factoryFromListSmall() {
        for (len in 0..62) {
            assertTrue(factoryC.fromList(genCharList(len)).integrityCheck(),
                    "failed with charList.size == $len")
        }
    }

    @Test
    fun size() {
        for (it in 0..1000) {
            val seq = genRandomList(1000 + it)
            val tree = factoryI.fromList(seq)
            assertTrue(tree.integrityCheck())
            assertEquals(seq.size, tree.size, "iteration $it failed")
        }
    }

    @Test
    fun asList() {
        for (idlen in 0..500) {
            val seq = genRandomList(idlen)
            val tree = factoryI.fromList(seq)
            assertEquals(tree.asList(), seq, "randomList idlen was $idlen")
            assertTrue(tree.integrityCheck())
        }
    }

    @Test
    fun factorySupply() {
        val ownFactory = Splay.Factory<Int>()
        for (idlen in 0..500) {
            val seq = genRandomList(500 + idlen)
            seq.forEach { ownFactory.supply(it) }
            val tree = ownFactory.acquireResult()
            assertTrue(tree.integrityCheck())
            assertEquals(seq, tree.asList())
        }
    }

//    @Test
//    fun toStringTest() {
//        for (idlen in 0..500) {
//            val seq = getRandomList(idlen)
//            val tree = factoryI.fromList(seq)
//            val str = tree.toString()
//            val list = str
//                    .filter { it in '0'..'9' || it == ' ' }
//                    .split(" ")
//                    .filter { it.isNotEmpty() }
//                    .map(String::toInt)
//            assertEquals(seq, list, "randomList idlen was $idlen")
//            assertTrue(tree.integrityCheck())
//        }
//    }

    @Test
    fun findSmall() {
        for (i in 0..400) {
//            println(i)
            val seq = genCharList(Rand[i] % 17 + 1)
            val tree = factoryC.fromList(seq)
            val by = Rand[i] % tree.size
            assertTrue(tree.integrityCheck())
            assertEquals(seq[by], tree[by]?.value, "iteration $i failed")
        }
    }

    @Test
    fun find() {
        for (i in 0..1000) {
//            println(i)
            val seq = genRandomList(1000 + i)
            val tree = factoryI.fromList(seq)
            assertTrue(tree.integrityCheck())
            val by = Rand[i] % tree.size
            assertTrue(tree.integrityCheck())
            assertEquals(seq[by], tree[by]?.value, "iteration $i failed")
        }
    }

    @Test
    fun seqFindSize() {
        for (i in 0..100) {
            val seq = genCharList(Rand[i] % 62 + 1)
            val tree = factoryC.fromList(seq)
            for (query in 0..50) {
                val by = Rand[query] % tree.size
                assertEquals(seq.size, tree.size, "iteration $i, sz-query $query failed")
                assertEquals(seq[by], tree[by]?.value, "iteration $i, []-query $query failed")
            }
        }
    }

    @Test
    fun splitBeforeSmall() {
        val seq = genCharList(17)
        for (by in 0..17) {
            val tree = factoryC.fromList(seq)
            val rtree = tree.splitBefore(by)
            assertTrue(tree.integrityCheck())
            assertEquals(by, tree.size)
            assertTrue(rtree.integrityCheck())
            assertEquals(seq.size - by, rtree.size)
            assertEquals(seq, tree.asList() + rtree.asList())
        }
    }

    @Test
    fun splitAfterSmall() {
        val seq = genCharList(17)
        for (by in 0..16) {
            val tree = factoryC.fromList(seq)
            val rtree = tree.splitAfter(tree[by]!!)
            assertTrue(tree.integrityCheck())
            assertEquals(by + 1, tree.size)
            assertTrue(rtree.integrityCheck())
            assertEquals(seq.size - by - 1, rtree.size)
            assertEquals(seq, tree.asList() + rtree.asList())
        }
    }

    @Test
    fun splitAfter() {
        for (idlen in 0..500) {
            val seq = genRandomList(500 + idlen)
            val tree = factoryI.fromList(seq)
            val by = Rand[idlen] % (tree.size)
            val rtree = tree.splitAfter(tree[by]!!)
            assertTrue(tree.integrityCheck())
            assertEquals(by + 1, tree.size)
            assertTrue(rtree.integrityCheck())
            assertEquals(seq.size - by - 1, rtree.size)
            assertEquals(seq, tree.asList() + rtree.asList())
        }
    }

    @Test
    fun splitBefore() {
        for (idlen in 0..1000) {
            val seq = genRandomList(1000 + idlen)
            val tree = factoryI.fromList(seq)
            val by = Rand[idlen] % (tree.size + 1)
            val rtree = tree.splitBefore(by)
            assertTrue(tree.integrityCheck())
            assertEquals(by, tree.size)
            assertTrue(rtree.integrityCheck())
            assertEquals(seq.size - by, rtree.size)
            assertEquals(seq, tree.asList() + rtree.asList())
        }
    }

    @Test
    fun mergeSmall() {
        for (len in 0..62) {
            val seq = genCharList(len)
            val by = Rand[len] % (seq.size + 1)
            val tree = factoryC.fromList(seq.subList(0, by))
            val rtree = factoryC.fromList(seq.subList(by, len))
            tree += rtree
            assertEquals(0, rtree.size)
            assertTrue(tree.integrityCheck())
            assertEquals(seq, tree.asList())
        }
    }

    @Test
    fun merge() {
        for (idlen in 0..200) {
            val seq = genRandomList(1000 + idlen)
            val by = Rand[idlen] % (seq.size + 1)
            val tree = factoryI.fromList(seq.subList(0, by))
            val rtree = factoryI.fromList(seq.subList(by, seq.size))
            tree += rtree
            assertEquals(0, rtree.size)
            assertTrue(tree.integrityCheck())
            assertEquals(seq, tree.asList())
        }
    }

    @Test
    fun tree() {
        val arr = Array<Splay<Int>>(200, { factoryI.fromList(genRandomList(1000 + it)) })
        arr.forEach {
            for (i in 0..(it.size - 1)) {
                assert(it === it[i]?.tree)
            }
        }
    }

    @Test
    fun reverseRoot() {
        val seq = genCharList(17)
        val tree = factoryC.fromList(seq)
        tree.reverse()
        seq.reverse()
        assertEquals(seq, tree.asList())
    }

    @Test
    fun reverseRootFind() {
        for (len in 1..17) {
            val seq = genCharList(len)
            for (i in seq.indices) {
                val tree = factoryC.fromList(seq)
                tree.reverse()
                seq.reverse()
                assertEquals(seq[i], tree[i]!!.value)
                seq.reverse()
            }
        }
    }

    @Test
    fun compareTo() {
        val seq = genCharList(62)
        val tree = factoryC.fromList(seq)
        for (i in seq.indices) {
            for (j in seq.indices) {
                assertEquals(i < j, tree[i]!! < tree[j]!!, "$i $j")
                assertEquals(i > j, tree[i]!! > tree[j]!!, "$i $j")
            }
        }
    }

    @Test
    fun sumSimple() {
        val seq = genCharList(62)
        val tree = factoryC.fromList(seq)
        var value = 0
        for (i in seq.indices) {
            tree.inc(Rand[i] % 10 + 1)
            value += Rand[i] % 10 + 1
            assertEquals(value, tree[i]!!.numValue, "${i}th call")
        }
    }

    fun magic(pathCntList: List<Int>): List<Int>{
        if (pathCntList.isEmpty()) return emptyList()
        data class Kappa(var _numValue: Int)
        val seq = pathCntList.map{ Kappa(it) }

        var prv: Kappa? = null
        var lastResult: Int = 0
        fun updateSizes(cur: Kappa? = null){
            if (prv != null) {
                lastResult += prv!!._numValue - (cur?._numValue ?: 0)
                prv!!._numValue = lastResult
            }
            prv = cur
        }

        seq.forEach { updateSizes(it) }
        updateSizes()

        assertEquals(pathCntList.first(), seq.last()._numValue)
        return seq.map{ it._numValue }.reversed()
    }

    @Test
    fun magicTestOfTest(){
        val t = { l: List<Int> -> Unit; assertEquals(l, magic(magic(l))) }
        t(listOf(15,7,5,1))
        t(listOf(3,1))
        t(listOf(10,5,3))
    }

//    @Test
//    fun magicPathReverse(){
//        for (idlen in 1..500) {
//            var psum = 0
//            val seq = genUniqueRandomList(idlen).map{ psum += it;psum }.reversed()
//            println(seq)
//            val tree = factoryI.fromList((1..seq.size).toList())
//            for (i in seq.indices) tree[i]!!.resetNumValue(seq[i])
//            tree.pathReverse()
//            assertEquals(magic(seq), (0 until seq.size).map { tree[it]!!.numValue })
//            assertEquals((1..seq.size).reversed().toList(), tree.asList())
//        }
//    }

    private fun <T> mixed(treeCnt: Int, seqOps: Int, cycles: Int, generator: (Int) -> MutableList<T>, cycleNum: Int = -1) {
        assert(treeCnt > 0)
        val stats = mutableMapOf<String, Int>()
        repeat(cycles, { cycle: Int ->
            if (cycleNum != -1 && cycleNum != cycle) return@repeat
            if (cycleNum != -1) println("cycle $cycle")
            val rand = Random(Rand[cycle].toLong())

            val fila: MutableList<Splay.Node<T>> = mutableListOf()
            val seq = generator(treeCnt)
            seq.forEach { Splay(fila, it) }
            val lol = seq.map { mutableListOf(it) }.toMutableList() //list of lists of node values
            val numValues = hashMapOf(*seq.map { Pair(it, 0) }.toTypedArray())

            fun search(value: T): Int {
                for (i in lol.indices)
                    if (lol[i].contains(value))
                        return i
                return -1
            }

            repeat(seqOps, { op: Int ->
                try {
                    if (cycleNum != -1) print("operation $op")
                    val name: String
                    val a = rand.nextInt(fila.size)
                    val b = rand.nextInt(fila.size)
                    val lla = search(seq[a])
                    val llb = search(seq[b])
                    val by = if (lol[lla].size > 0) rand.nextInt(lol[lla].size) else 0
                    val first = rand.nextBoolean()
                    val inc = rand.nextInt(treeCnt)
                    name = when (rand.nextInt(6)) {
                        0 -> {
                            if (lla != llb) {
                                lol[lla] = (lol[lla] + lol[llb]).toMutableList()
                                lol[llb] = mutableListOf()
                                fila[a].tree += fila[b].tree
                                "merge"
                            } else {
                                "skip"
                            }
                        }
                        1 -> {
                            if (by + 1 < lol[lla].size)
                                lol.add(lol[lla].subList(by + 1, lol[lla].size).toMutableList())
                            lol[lla] = lol[lla].subList(0, by + 1)
                            fila[a].tree.splitAfter(by)
                            "splitAfter"
                        }
                        2 -> {
                            assertEquals(
                                    lol[lla][by],
                                    fila[a].tree[by]!!.value
                            )
                            "find"
                        }
                        3 -> {
                            lol[lla].reverse()
                            fila[a].tree.reverse()
                            "reverse"
                        }
                        4 -> {
                            "ultra" +
                                    if (first) {
                                        assertEquals(lol[lla].first(), fila[a].tree.ultra(0)?.value)
                                        0
                                    } else {
                                        assertEquals(lol[lla].last(), fila[a].tree.ultra(1)?.value)
                                        1
                                    }
                        }
                        5 -> {
                            for (i in lol[lla].indices) numValues[lol[lla][i]] = numValues[lol[lla][i]]!! + inc
                            fila[a].tree.inc(inc)
                            "inc"
                        }
                        else -> "notImplemented"
                    }
                    if (cycleNum != -1) println("\t//$name")
                    assertEquals(
                            lol
                                    .filter { it.isNotEmpty() }
                                    .toSet(),
                            fila
                                    .filter {
                                        it.tree.integrityCheck()
                                        it.parent == null
                                    }
                                    .map { it.tree.asList() }
                                    .toSet(),
                            "cycle $cycle, operation #$op ($name)")
                    assertEquals(
                            numValues.values.sorted(),
                            fila.map{ it.splay().numValue }.sorted(),
                            "cycle $cycle, operation #$op ($name)")
                    stats[name] = (stats[name] ?: 0) + 1
                } catch (thr: Throwable) {
                    println("\nerror on op $op at cycle $cycle")
                    throw thr
                }
            })
        })
        assert(cycleNum == -1)
        print("\n${Throwable().stackTrace[2].methodName} tested:")
        for (p in stats) {
            print(" ${p.key} x${p.value},")
        }
        println()
    }

    @Test
    fun mixedShortSeqSmall() = mixed(5, 5, 1000, ::genCharList)

    @Test
    fun mixedSeqSmall() = mixed(5, 200, 200, ::genCharList)

    @Test
    fun mixedShortSeq() = mixed(500, 10, 50, ::genUniqueRandomList)

    @Test
    fun mixedSeq() = mixed(150, 150, 70, ::genUniqueRandomList)
}