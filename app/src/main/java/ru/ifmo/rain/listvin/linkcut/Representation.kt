package ru.ifmo.rain.listvin.linkcut

import android.webkit.WebView
import kotlin.math.max
import kotlin.math.min

typealias PInt = Pair<Int, Int>

class Representation (val webView: WebView) {
    val threshold = 200

    private lateinit var tree: LCTree
    var representable: Boolean = true
        private set(value) {
            if (field != value) {
                wvexec(if (value) "hidePlug()" else "showPlug()")
            }
            field = value
        }

    private var request: PInt? = null
        get() = field.also { field = null }
    private var requestType: Status? = null

    private val undirectedEdges = mutableSetOf<Int>()
    private fun request(a: Int, b: Int, type: Status) {
        request = Pair(a,b)
        requestType = type
        if (type == Status.CREATED) {
            undirectedEdges.add(edgeId(a,b,true))
        } else {
            undirectedEdges.remove(edgeId(a,b,true))
        }
    }

    fun link(a: Int, b: Int){
        try {
            tree.link(a, b)
        } catch (ise: IllegalStateException) {
            update()
            throw ise
        }
        request(a, b, Status.CREATED)
        update()
    }
    /**
     * returns false if there is no such edge
     */
    fun cut(a: Int, b: Int): Boolean {
        if (!undirectedEdges.contains(edgeId(a,b,true)))
            return false
        tree.cut(a, b)
        request(a, b, Status.PRESENTED)
        update()
        return true
    }
    fun connected(a: Int, b: Int): Boolean = tree.connected(a, b).also { update() }
    fun size(a: Int): Int = tree.size(a).also { update() }
    val size: Int get() = tree.size
    val edgesCnt: Int get() = edges.size
    fun getEdge(num: Int): List<Int>? {
        if (edges.size == 0) return null
        var i = num%edges.size
        for (edge in edges.values) {
            if (i == 0) return listOf(edge.from, edge.to)
            --i
        }
        return null
    }

    private val edges: MutableMap<Int, Edge> = mutableMapOf()
    /**
     * Used to clear representation and populate new graph
     * @return false in case graph is too big and won't be rendered
     */
    fun new(size: Int): Boolean {
        tree = LCTree(size)
        undirectedEdges.clear()
        edges.clear()
        request = null
        wvexec("clear()")
        wvexec("clearUndirectedEdges()")
        representable = size <= threshold
        if (representable) {
            for (i in 0 until size) wvexec("addNode($i)")
        } else {
            //TODO plug
        }
        return representable
    }

    fun update(){
        if (!representable) return

        for (node in tree.filament) {
            val linkCandidate = node.value as Splay.Node<*>?
            if (linkCandidate != null) {
                forceEdge(linkCandidate.id, node.id).propose(VivaDirected.LINK)
            }
            if (node.parent != null) {
                forceEdge(node.parent!!.id, node.id).propose(VivaDirected.SPLAY)
            }
        }

        /*request?.apply {
            when (requestType) {
                Status.PRESENTED -> (getUndirectedEdge(first, second) ?: throw IllegalStateException()).undirected = false
                Status.CREATED -> forceUndirectedEdge(first, second).undirected = true
                else -> IllegalStateException()
            }
        }*/
        request?.apply {
            when(requestType) {
                Status.PRESENTED -> wvexec("removeUndirectedEdge($first, $second)")
                Status.CREATED -> wvexec("addUndirectedEdge($first, $second)")
                else -> IllegalStateException()
            }
        }

        edges.values.filter { it.status == Status.PRESENTED }.forEach(Edge::remove)
        edges.values.forEach(Edge::flush)
    }

    private fun getEdge(from: Int, to: Int): Edge? = edges[edgeId(from, to)]
    private fun forceEdge(from: Int, to: Int): Edge = getEdge(from, to) ?: Edge(from, to)
    private fun getUndirectedEdge(a: Int, b: Int): Edge? = getEdge(a, b) ?: getEdge(b, a)
    private fun forceUndirectedEdge(a: Int, b: Int): Edge = getUndirectedEdge(a, b) ?: Edge(a,b)

    private enum class Status { PRESENTED, CONFIRMED, MODIFIED, CREATED}
    private enum class VivaDirected(val c: Char){ SPLAY('s'), LINK('l') }

    private inner class Edge(val from: Int, val to: Int) {
        init {
            edges[edgeId(from, to)] = this
        }
        private var type: VivaDirected? = null
        var status: Status = Status.CREATED
            private set
        fun propose(newType: VivaDirected){
            status = when (type) {
                null -> Status.CREATED
                newType -> Status.CONFIRMED
                else -> Status.MODIFIED
            }
            type = newType
        }
        var undirected: Boolean = false
            set(value) {
                //tree edge deleted no connections possible, because nodes are going to different subtrees
                if (field && !value) assert(status == Status.PRESENTED) {"nothing to do at all, status should stay PRESENTED to let this edge be deleted" }
                //new tree edge, there was no any connections before this iteration
                if (!field && value) assert(status == Status.CREATED) {"only fresh repr edge can receive new tree edge duty"}
                //confirming non-existent edge??
                if (!field && !value) throw IllegalStateException()
                //confirming existing tree edge...
                if (field && value) {
                    assert(status != Status.CREATED)
                    //reverse edge could have been created, then new repr-edge will carry this tree edge
                    if (status == Status.PRESENTED) {
                        if (getEdge(to, from) != null) return
                        status = Status.CONFIRMED
                    }
                }
                field = value
            }

        private val vivaString: String get() = "'${type?.c ?: '_'}${if (undirected) 't' else '_'}'"

        override fun toString(): String {
            return "Edge@${edgeId(from, to)}(status=${status.name}, type=${type?.name}, undirected=$undirected) <->" + vivaString
        }

        fun remove() {
            assert(status == Status.PRESENTED)
            if (undirected) {
                val candidate = getEdge(to, from)
                if (candidate == null) {
                    status = if (type == null) Status.CONFIRMED else Status.MODIFIED
                    type = null
                    return
                }
                candidate.undirected = true
            }
            assert(status == Status.PRESENTED)
            wvexec("removeEdge($from, $to)")
            edges.remove(edgeId(from, to))
        }

        fun flush(){
            when (status) {
                Status.PRESENTED -> throw IllegalStateException()
                Status.CREATED -> wvexec("addEdge($from, $to, $vivaString)")
                Status.MODIFIED -> wvexec("modifyEdge($from, $to, $vivaString)")
                Status.CONFIRMED -> {}
            }
            status = Status.PRESENTED
        }
    }

    private fun edgeId(from: Int, to: Int, undirected: Boolean = false): Int{
        if (undirected) return if (from < to) edgeId(from, to) else edgeId(to, from)
        return from*1_000_000_000 + to
    }

    private fun wvexec(com: String) = webView.loadUrl("javascript:graph.$com")
}