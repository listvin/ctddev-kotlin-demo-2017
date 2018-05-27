package ru.ifmo.rain.listvin.linkcut

//typealias ANode = Splay.Node<Any>

class LCTree (val size: Int){
    val filament = mutableListOf<Splay.Node<Any>>()
    val filamentSz = mutableListOf<Splay.Node<Unit>>()

    init {
        for (i in 0 until size) {
            Splay(filament).isomorphic = Splay(filamentSz, rootValue = Unit)
            filamentSz.last().resetNumValue(1)
        }
    }

//    private fun Splay<Any>.mirror(): Splay<Unit> {
//        return isomorphic!!
//    }

    private fun cutout(n: Splay.Node<Any>){
        val szTree = n.tree.isomorphic!!.splitAfter(n.ordinal())
        val auxTree = n.tree.splitAfter(n)
        auxTree.isomorphic = szTree
        auxTree.ultra(0)?.value = n
    }

    private fun expose(n: Splay.Node<Any>){
        cutout(n)
        @Suppress("NAME_SHADOWING")
        var n = n.tree.ultra(0)!!
        while (n.value != null) {
            @Suppress("UNCHECKED_CAST")
            val link = n.value as Splay.Node<Any>
            n.value = null
            cutout(link)
            link.tree.isomorphic!! += n.tree.isomorphic!!
            link.tree += n.tree
            n = n.tree.ultra(0)!!
        }
    }

    /** also calls expose(n), returned node is root of aux tree*/
    private fun findRoot(n: Splay.Node<Any>): Splay.Node<Any> {
        expose(n)
        return n.tree.ultra(0)!!
    }

    /** also calls expose(n)*/
    private fun makeAuxRoot(n: Splay.Node<Any>) {
        val oldRoot = findRoot(n) //expose made here
        if (oldRoot !== n) {
            n.tree.isomorphic!!.pathReverse()
            n.tree.reverse()
            assert(n.tree.ultra(0) === n)
        }
        expose(n)
    }

    fun link(aId: Int, bId: Int){
        if (connected(aId, bId)) throw IllegalStateException()
        val a = filament[aId]
        val b = filament[bId]
        makeAuxRoot(b) //size of this tree is increment itself
        expose(a) //this path will be incremented. this needed only for sizes support

//        a.tree.isomorphic!!.inc(b.tree.isomorphic!!.ultra(0)!!.numValue)
        val mb = b.tree.isomorphic!!
        val leftest = mb.ultra(0)!!
        val iv = leftest.numValue
        val ma = a.tree.isomorphic!!
        ma.inc(iv)

        b.value = a
        expose(b)
    }

    fun cut(aId: Int, bId: Int){
        var a = filament[aId]
        var b = filament[bId]
        if (a.value === b || b.value !== a && b < a) {
            val tmp = a
            a = b
            b = tmp
        }
        //a should be parent of be here
        expose(a)
        a.tree.isomorphic!!.inc(-b.tree.isomorphic!!.ultra(0)!!.numValue)
        b.value = null
        expose(b)
    }

    fun connected(aId: Int, bId: Int) = findRoot(filament[aId]) === findRoot(filament[bId])

    fun size(aId: Int) = findRoot(filament[aId]).tree.isomorphic!!.ultra(0)!!.numValue
}
