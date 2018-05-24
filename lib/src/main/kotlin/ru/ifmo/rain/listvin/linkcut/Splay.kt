package ru.ifmo.rain.listvin.linkcut

class Splay<T>(
        filament: MutableList<Node<T>>? = null,
        rootValue: T? = null
) {
    /** initialized with globally unique ordinal num, not used internally in any way */
    var isomorphicOne: Splay<Unit>? = null

    class Factory<T> {
        private lateinit var result: Splay<T>

        fun acquireResult(): Splay<T> {
            result = Splay(dfs())
            list.clear()
            return result
        }

        private var list = mutableListOf<T>()

        fun supply(value: T) {
            list.add(value)
        }

        private fun dfs(l: Int = 0, r: Int = list.size): Node<T>? {
            if (l == r) return null
            val m = (l + r) / 2
            assert(m in list.indices)
            val cur = Node(list[m])
            cur.child[0] = dfs(l, m)
            cur.child[1] = dfs(m + 1, r)
            cur.child.updateStats()
            return cur
        }

        fun fromList(list: List<T>): Splay<T> {
            val swap = this.list
            this.list = list.toMutableList()
            val tree = acquireResult()
            this.list = swap
            return tree
        }
    }

    private constructor(root: Node<T>?) : this() {
        root?.hang(this)
    }

    class Node<T>(var value: T?, val id: Int = nodeId) {
        companion object {
            //            @JvmStatic
            var nodeId = 0
                get() = field++
                private set
        }

        internal var _numValue = 0
            private set
        val numValue: Int
            get() {
                assert(parent == null, { "forgot to splay" })
                child.push()
                return _numValue
            }

        internal fun resetNumValue(newValue: Int) {
            _numValue = newValue
            child.pendingInc = 0
            child.pendingPathRev = 0
        }

        inner class Children(private val child: Array<Node<T>?>) {
            operator fun set(rang: Int, node: Node<T>?) {
                push()
                child[rang] = node
                node?.rang = rang
                node?.parent = this@Node
            }

            operator fun get(rang: Int): Node<T>? {
                push()
                return child[rang]
            }

            private var pendingReverse = false
            internal fun orderReverse() {
                pendingReverse = !pendingReverse
            }
            private fun pushReverse() {
                assert(pendingReverse)
                val temp = child[0]; child[0] = child[1]; child[1] = temp
                child.forEach {
                    it?.child?.orderReverse()
                    it?.rang = 1 - it!!.rang
                }
                pendingReverse = false
            }

            internal var pendingInc = 0
            internal fun orderInc(by: Int) {
                if (pendingPathRev != 0) push()
                pendingInc += by
            }
            private fun pushInc() {
                assert(pendingInc != 0)
                _numValue += pendingInc
                child.forEach {
                    it?.child?.orderInc(pendingInc)
                }
                pendingInc = 0
            }

            internal var pendingPathRev = 0
            internal fun orderPathRev(with: Int){
                if (pendingInc != 0) push()
                if (pendingPathRev == 0) {
                    pendingPathRev = with
                } else {
                    val tmp = pendingPathRev
                    pendingPathRev = 0
                    orderInc(with - tmp)
                }
            }
            private fun pushPathRev(){
                assert(pendingPathRev != 0 && pendingInc == 0)
                _numValue = pendingPathRev - _numValue
                child.forEach { it?.child?.orderPathRev(pendingPathRev) }
                pendingPathRev = 0
            }

            internal fun push() {
                if (pendingReverse) pushReverse()
                if (pendingInc != 0) pushInc()
                if (pendingPathRev != 0) pushPathRev()
            }

            internal fun updateStats() {
                cnt = 1
                child.forEach { cnt += it?.cnt ?: 0 }
            }

            internal fun getChildSubStringRO(rang: Int): String {
                return if (child[rang] != null) {
                    "<${child[rang]!!.toSubString()}>"
                } else {
                    ""
                }
            }
        }

        fun reverseNode() = child.orderReverse()
        fun incNode(by: Int) = child.orderInc(by)
        fun pathReverseNode(with: Int) = child.orderPathRev(with)

        var parent: Node<T>? = null
            get() {
                field?.child?.push()
                return field
            }
        var rang = 0
        val child = Children(arrayOf<Node<T>?>(null, null))

        private fun rise() {
            assert(parent!!.child[rang] == this, { "$value" })
            if (parent!!.parent == null) hang(parent!!._tree, true)
            val oldParent = parent!!
            oldParent.child[rang] = child[1 - rang] //middle subtree
            val rangCache = rang
            if (oldParent.parent != null) {
                oldParent.parent!!.child[oldParent.rang] = this //hanging raised node to parent of old subroot
            } else {
                parent = null
            }
            child[1 - rangCache] = oldParent //hanging old subroot as opposite child
            oldParent.child.updateStats()
            child.updateStats()
        }

        fun asList(ls: MutableList<T?>) {
            child[0]?.asList(ls)
            ls.add(value)
            child[1]?.asList(ls)
        }

        override fun toString(): String { //TODO remove debug
            if (value is Node<*>?) {
                return "$id.${(value as Node<*>?)?.id ?: "$"}"
            } else {
                return (if (child.pendingPathRev != 0) "${child.pendingPathRev}?" else "") + "sz=$_numValue" +
                    if (child.pendingInc != 0) "+${child.pendingInc}" else ""
            }

//            return "$id.${(value as Splay.Node<*>?)?.id ?: "$"}|sz=$_numValue" +
//                    if (child.pendingInc != 0) "+${child.pendingInc}" else ""
//
//            return if (_numValue == 0 && child.pendingInc == 0)
//                "$id.${(value as Splay.Node<*>?)?.id ?: "$"}"
//            else
//                "sz=$_numValue" + if (child.pendingInc != 0) "+${child.pendingInc}" else ""
//
//    value +/
//                    id.toString(36) +
        }

        internal fun toSubString(): String {
            return child.getChildSubStringRO(0) +
                    toString() +
                    child.getChildSubStringRO(1)
        }

        fun check(isRoot: Boolean = true): Int {
            if (isRoot) {
                assert(parent == null)
            } else {
                assert(parent!!.child[rang] == this)
            }
            var ccnt = 1
            for (i in 0..1) child[i]?.let {
                ccnt += it.check(false)
            }
            assert(ccnt == cnt, { "at $value" })
            return ccnt
        }

        var cnt = 1

        internal fun splay(): Node<T> {
            while (parent != null) {
                if (parent!!.parent != null)
                    parent!!.rise()
                rise()
            }
            return this
        }

        private lateinit var _tree: Splay<T>
        val tree: Splay<T>
            get() {
                splay()
                return _tree
            }

        internal fun hang(owner: Splay<T>, unsafe: Boolean = false) {
            assert(unsafe || parent == null)
            _tree = owner
            owner.root = this
        }

        fun ordinal(): Int {
            splay()
            return child[0]?.cnt ?: 0
        }

        @Override
        operator fun compareTo(b: Node<T>): Int {
            if (this === b) return 0
            val t1 = b.tree
            assert(t1.root === b)
            val r1 = b.ordinal()
            val t0 = tree
            assert(t0.root === this)
            val r0 = ordinal()
            if (t1 !== t0)
                throw IllegalArgumentException()
            return r0 - r1
        }
    }

    /** supported correct by Node.splay() (.hang from .rise to be exact) and may be also changed by split/merge*/
    var root =
            if (filament == null) null
            else {
                val n = Node(rootValue, filament.size)
                n.hang(this)
                filament.add(n)
                n
            }
        private set(value) {
            //if (value != null) assert(Throwable().stackTrace[2].className.endsWith("Node")/*, {throw Throwable()}*/)
            field = value
        }

    fun reverse() = root?.reverseNode()
    fun inc(by: Int) = root?.incNode(by)
    fun pathReverse() {
        val l = ultra(0)!!
        val newRoot = splitAfter(l).root
        if (newRoot != null) {
            newRoot.hang(this)
        } else {
            root = null
        }
        val with = l.numValue
        l.resetNumValue(0)
        this += Splay(l)
        root!!.pathReverseNode(with)
        reverse()
    }

    fun ultra(rang: Int): Node<T>? {
        if (root == null) return null
        var result = root!!
        while (result.child[rang] != null)
            result = result.child[rang]!!
        return result.splay()
    }

    operator fun plusAssign(b: Splay<T>?) {
        assert(b !== this)
        if (b?.root == null) return
        if (root == null) {
            b!!.root!!.hang(this)
        } else {
            ultra(1)!!.child[1] = b!!.root
        }
        root!!.child.updateStats()
        b.root = null
    }

    operator fun get(index: Int): Node<T>? {
        if (root == null) return null
        var k1th = index + 1
        if (k1th !in 1..root!!.cnt) return null
        var cur = root!!
        while (true) {
            val lCnt = 1 + (cur.child[0]?.cnt ?: 0)
            if (lCnt == k1th)
                break
            cur = if (k1th < lCnt) {
                cur.child[0]!!
            } else {
                k1th -= lCnt
                cur.child[1]!!
            }
        }
        return cur.splay()
    }

    fun splitBefore(by: Node<T>): Splay<T> {
        assert(by.tree === this)
        val rtree = Splay(by.splay())
        if (root!!.child[0] == null) {
            root = null
        } else {
            root!!.child[0]!!.parent = null
            root!!.child[0]!!.hang(this)
            rtree.root!!.child[0] = null
        }
        root?.child?.updateStats()
        rtree.root!!.child.updateStats()
        return rtree
    }

    fun splitAfter(by: Node<T>): Splay<T> {
        assert(by.tree === this)
        by.splay()
        by.child[1]?.parent = null
        val rtree = Splay(by.child[1])
        root!!.child[1] = null
        root!!.child.updateStats()
        rtree.root?.child?.updateStats()
        return rtree
    }

    fun splitBefore(by: Int): Splay<T> {
        assert(by >= 0)
        if (by >= size) return Splay()
        return splitBefore(this[by]!!)
    }

    fun splitAfter(by: Int): Splay<T> {
        assert(by >= 0)
        if (by >= size - 1) return Splay()
        return splitAfter(this[by]!!)
    }

    fun asList(): List<T?> {
        val ls = mutableListOf<T?>()
        root?.asList(ls)
        return ls
    }

//    override fun toString() = "Splay'${isomorphicOne.toString().padStart(2, '_')}(${root?.toSubString()})"
    override fun toString() = "Splay(${root?.toSubString()})"

    val size: Int get() = root?.cnt ?: 0

    internal fun integrityCheck(): Boolean {
        root?.check()
        return true
    }

//    fun magicPathReverse() {
//        var prv: Node<T>? = null
//        var lastResult = 0
//
//        fun updateSizes(cur: Node<T>? = null) {
//            if (prv != null) {
//                lastResult += prv!!._numValue - (cur?._numValue ?: 0)
//                prv!!.resetNumValue(lastResult)
//            }
//            prv = cur
//        }
//
//        fun magicDfs(cur: Node<T>? = root) {
//            if (cur == null) return
//            magicDfs(cur.child[0])
//            updateSizes(cur)
//            magicDfs(cur.child[1])
//        }
//
//        magicDfs()
//        updateSizes()
//        reverse()
//    }
}

//            return (child[0]?.toString() ?: "") + "$value " + (child[1]?.toString() ?: "")
//    return if (value is Any)
//        "<${child[0]}> ${id.toString(36)} <${child[1]}>"
//    else
//        "<${child[0]}> $value <${child[1]}>"
//}
