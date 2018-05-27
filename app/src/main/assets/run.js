var nodeSize = 12;

var graph = Viva.Graph.graph();
var graphics = Viva.Graph.View.svgGraphics();

graph.removeEdge = function (from, to) {
    graph.forEachLinkedNode(from, function (node, link) { if (node.id === to) graph.removeLink(link); } );
};

graph.modifyEdge = function (from, to, data) {
    graph.removeEdge(from, to);
    graph.addLink(from, to, data);
};

graph.addEdge = function (from, to, data) {
    graph.removeEdge(from, to);
    graph.addLink(from, to, data);
};

if (typeof Feedback === "undefined") {
    graph.addNode(0);
    graph.addNode(1);
    graph.addLink(0, 1, "_t");
    graph.addLink(1, 2, "st");
    graph.addLink(2, 3, "l_");
    graph.addLink(0, 1, "_t");
}

var prevTouch = -1;
function getNodeUI(id) {
    return document.getElementById("nodeNumber"+id)
}
function dropTouch(){
    if (prevTouch !== -1 && graph.getNode(prevTouch) !== undefined)
        getNodeUI(prevTouch).attr('class','node_body');
    prevTouch = -1;
}
function setTouch(id){
    dropTouch();
    getNodeUI(id).attr('class','node_selected');
    prevTouch = id;
}

graphics.node(function(node) {
    var caption = Viva.Graph.svg('text').attr('class','node_caption');
    caption.textContent = node.id.toString();

    var g = Viva.Graph.svg('g');
    g.append(Viva.Graph.svg('circle').attr('r', nodeSize).attr('class','node_body').attr('id','nodeNumber'+node.id));
    g.append(caption);
    g.addEventListener("touchend", function() {
        Feedback.touched(node.id)
    });
    g.addEventListener("touchstart", function() {
        setTouch(node.id)
    });
    return g
}).placeNode(function(nodeUI, pos) {
    nodeUI.children[0].attr('cx', pos.x).attr('cy', pos.y);
    nodeUI.children[1].attr('x', pos.x).attr('y', pos.y);
});

function strPair(pnt){
    return pnt.x+","+pnt.y+" "
}

graphics.link(function(link){
    //link.data is _t s_ l_ st lt
    var poly = Viva.Graph.svg('polyline');
    var path = Viva.Graph.svg('path');

    switch (link.data[0]){
        case 'l': poly.attr('class', 'link_edge'); break;
        case 's': poly.attr('class', 'splay_edge'); break;
        case '_': poly.attr('class', 'no_edge'); break;
        default: console.error("problem with interpreting link: " + link.data); break;
    }

    switch (link.data[1]){
        case 't': path.attr('class', 'tree_edge'); break;
        case '_': path.attr('class', 'no_edge'); break;
        default: console.error("problem with interpreting link: " + link.data); break;
    }

    var g = Viva.Graph.svg('g');
    g.append(poly);
    g.append(path);
    return g;
}).placeLink(function(linkUI, fromPos, toPos) {
    var dx = toPos.x - fromPos.x;
    var dy = toPos.y - fromPos.y;
    var len = Math.sqrt(dx * dx + dy * dy);
    var normR = {}, normL = {};
    normR.x = fromPos.x + dy / len * nodeSize;
    normR.y = fromPos.y - dx / len * nodeSize;
    normL.x = fromPos.x - dy / len * nodeSize;
    normL.y = fromPos.y + dx / len * nodeSize;
    linkUI.children[0].attr("points", strPair(normR) + strPair(normL) + strPair(toPos));

    linkUI.children[1].attr("d", 'M' + fromPos.x + ',' + fromPos.y + 'L' + toPos.x + ',' + toPos.y);
});

var layout = Viva.Graph.Layout.forceDirected(graph, {
    springLength : nodeSize*5,
    springCoeff : 5e-5,
    dragCoeff : 0.02,
    gravity : -1.5e-1
});

var renderer = Viva.Graph.View.renderer(graph, {
        graphics : graphics,
        layout: layout
    });
renderer.run();

document.getElementsByTagName("svg")[0].addEventListener("touchstart", function() {
    dropTouch()
});