package ru.ifmo.rain.listvin.linkcut

//typealias ANode = Splay.Node<Any>

class LCTree (val size: Int){
    val filament = mutableListOf<Splay.Node<Any>>()
    val filamentSz = mutableListOf<Splay.Node<Unit>>()

    init {
        for (i in 0 until size) {
            Splay(filament).isomorphicOne = Splay(filamentSz, rootValue = Unit)
            filamentSz.last().resetNumValue(1)
        }
    }

    private fun Splay<Any>.mirror(): Splay<Unit> {
        return isomorphicOne!!
    }

    private fun cutout(n: Splay.Node<Any>){
        val szTree = n.tree.mirror().splitAfter(n.ordinal())
        val auxTree = n.tree.splitAfter(n)
        auxTree.isomorphicOne = szTree
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
            link.tree.mirror() += n.tree.mirror()
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
            n.tree.mirror().pathReverse()
            n.tree.reverse()
            assert(n.tree.ultra(0) === n)
        }
        expose(n)
    }

    private fun checkId(id: Int){
        if (id !in 0 until size) throw IndexOutOfBoundsException();
    }

    fun link(aId: Int, bId: Int){
        checkId(aId); checkId(bId)
        if (connected(aId, bId)) throw IllegalStateException()
        val a = filament[aId]
        val b = filament[bId]
        makeAuxRoot(b) //size of this tree is increment itself
        expose(a) //this path will be incremented. this needed only for sizes support

//        a.tree.mirror().inc(b.tree.mirror().ultra(0)!!.numValue)
        val mb = b.tree.mirror()
        val leftest = mb.ultra(0)!!
        val iv = leftest.numValue
        val ma = a.tree.mirror()
        ma.inc(iv)

        b.value = a
        expose(b)
    }

    fun cut(aId: Int, bId: Int){
        checkId(aId); checkId(bId)
        var a = filament[aId]
        var b = filament[bId]
        if (a.value === b || b.value !== a && b < a) {
            val tmp = a
            a = b
            b = tmp
        }
        //a should be parent of be here
        expose(a)
        a.tree.mirror().inc(-b.tree.mirror().ultra(0)!!.numValue)
        b.value = null
        expose(b)
    }

    fun connected(aId: Int, bId: Int) = findRoot(filament[aId]) === findRoot(filament[bId])

    fun size(aId: Int) = findRoot(filament[aId]).tree.mirror().ultra(0)!!.numValue
}