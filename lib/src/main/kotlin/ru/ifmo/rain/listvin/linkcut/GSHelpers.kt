package ru.ifmo.rain.listvin.linkcut
//
//import org.graphstream.graph.*
//import org.graphstream.graph.implementations.*
////import org.graphstream.ui.layout.HierarchicalLayout
//
//private fun newGraph(graphName: String): Graph {
////    val tmp = SingleGraph(graphName, false, false)
//    val tmp = SingleGraph(graphName, false, true)
//    /*tmp.setAttribute("ui.stylesheet", ""+//graph {\n" +
////            "    fill-color: black;\n" +
////            "}\n" +
////            "\n" +
//            "node {\n" +
//            "    size: 10px;\n" +
////            "\n" +
////            "    /*label*/\n" +
////            "    text-alignment: center;\n" +
////            "    text-size: 10;\n" +
////            "    text-style: bold;\n" +
////            "    text-background-mode: rounded-box;\n" +
////            "    text-background-color: #00000055;\n" +
////            "    text-color: white;\n" +
////            "    text-padding: 2px;\n" +
//            "}\n" +
//            "\n" +
//            "edge {\n" +
////            "    /*stroke-color: white;*/\n" +
////            "    shape: angle;\n" +
////            "    arrow-shape: none;\n" +
//            "    size: 5px;\n" +
//            "}")*/
////    tmp.display().enableAutoLayout(HierarchicalLayout())
//    return tmp
//}
//
//val forest = newGraph("forest")
//val isomorphic = newGraph("isomorphic")
//val sample = {
//    val g = newGraph("sample")
//    for (i in 0..9) g.addNode(i.toString())
////    g.addNode("a")
////    g.addNode("b")
////    g.addNode("c")
////    g.addNode("d")
////    g.addEdge("ab", "a", "b", true)
////    g.addEdge("bc", "b", "c", true)
////    g.addEdge("bd", "b", "d", true)
////    g.addEdge("cd", "c", "d")
//    g
//}()
//val selectedNodes = mutableSetOf<Int>()
//
//fun LCTree.redraw(){
//    drawNodes(filament, forest)
//    drawNodes(filamentSz, isomorphic)
//}
//
//private var prevSz = 0
//private fun <T> drawNodes(filament: List<Splay.Node<T>>, g: Graph){
//    if (filament.size != prevSz) {
//        g.removeAll { true }
//        prevSz = filament.size
//    }
//
//    filament.forEach{ n ->
//        g.addNode(n)
//
//        g.removeEdge(n.sid())
//        g.removeEdge("l"+n.sid())
//        if (n.parent != null) {
//            g.addEdge(n.parent!!, n)
//        } else if (n.value is Splay.Node<*>) {
//            @Suppress("UNCHECKED_CAST")
//            g.addEdge(n.value!! as Splay.Node<T>, n, true)
//        }
//    }
//
////    g.setAttribute("ui.screenshot", "/home/fdl/Desktop/${System.currentTimeMillis()}.png");
//}
//
//private fun <T> Splay.Node<T>.sid() = id.toString()
//
//private fun <T> Graph.addNode(n: Splay.Node<T>): Node{
//    val gn = this.addNode(n.sid())
////    gn.setAttribute("ui.label", " "+n.toString()+" ")
//
//    if (n.parent == null) {
//        val hook = this.addNode("r${n.sid()}")
//        hook.setAttribute("ui.class", "root")
//        hook.setAttribute("ui.label", n.tree.isomorphicOne)
//        this.addEdge("sr${n.sid()}", n.sid(), "r${n.sid()}").setAttribute("ui.class", "root")
//    } else {
//        this.removeNode("r${n.sid()}")
//    }
//
//    if (selectedNodes.contains(n.id)) {
//        gn.setAttribute("ui.class", "selected")
//        selectedNodes.remove(n.id)
//    } else {
//        gn.removeAttribute("ui.class")
//    }
//
//    gn.setAttribute("ui.label", n.toString())
//    return gn
//}
//
//private fun <T> Graph.addEdge(from: Splay.Node<T>, to: Splay.Node<T>, link: Boolean = false): Edge{
//    val e = this.addEdge((if (link) "l${to.sid()}" else to.sid()), from.sid(), to.sid(), true)
//    if (link) e.setAttribute("ui.class", "link")
//    return e
//}
//
//fun main(args: Array<String>) {
//    System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer")
//    val graph = SingleGraph("Tutorial 1")
//
//    graph.addNode("A")
//    graph.addNode("B")
//    val n = graph.addNode("C")
//    n.setAttribute("ui.label", "CA")
//    graph.addEdge("AB", "A", "B")
//    graph.addEdge("BC", "B", "C", true)
//    //        e.setAttribute();
//    graph.addEdge("CA", "C", "A")
//
//    graph.setAttribute("ui.stylesheet", "url(file:///home/fdl/Documents/Study/3rd_sem/ASD/link-cut/styles/sz.css)")
//    //        graph.setAttribute("ui.stylesheet", "url(file://styles/sz.css)");
//    graph.display()
//}