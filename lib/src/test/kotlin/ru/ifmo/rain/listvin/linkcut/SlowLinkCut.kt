package ru.ifmo.rain.listvin.linkcut


class SlowLinkCut(val size: Int){
    private val g = Array<MutableList<Int>>(size, { mutableListOf() })
    private val u = IntArray(size)
    private val edges = mutableListOf<Pair<Int, Int>>()
    private var T = 0
    private var lastLap = 0
    var successful = false
        private set
    val edgesCnt: Int get() = edges.size

    private fun dfs(cur: Int, target: Int? = null, init: Boolean = true): Boolean {
        if (init) {
            ++T
            lastLap = 0
        }
        if (cur == target) return true
        ++lastLap
        u[cur] = T
        for (to in g[cur]) if (u[to] != T) if (dfs(to, target, false)) return true
        return false
    }

    fun connected(a: Int, b: Int): Boolean {
        assert(a != b)
        successful = true
        return dfs(a, b)
    }

    fun link(a: Int, b: Int){
        assert(a != b)
        if (dfs(a,b)){
            successful = false
            return
        }
        g[a].add(b)
        g[b].add(a)
        edges.add(if (a < b) Pair(a,b) else Pair(b,a))
        successful = true
    }

    fun disconnect(i: Int): Pair<Int, Int>? {
        if (i !in edges.indices) {
            successful = false
            return null
        }
        val (a,b) = edges.removeAt(i)
        g[a].remove(b)
        g[b].remove(a)
        successful = true
        return if ((a+b) and 1 == 1) Pair(a,b) else Pair(b,a)
    }

    fun cut(a: Int, b: Int) {
        assert(a != b)
        val i = edges.indexOf(if (a < b) Pair(a,b) else Pair(b,a))
        successful = when(i){
            -1 -> false
            else -> {
                disconnect(i)
                true
            }
        }
    }

    fun size(a: Int): Int {
        if (a !in 0 until size) {
            successful = false
            return -1
        }
        successful = true
        dfs(a)
        return lastLap
    }
}