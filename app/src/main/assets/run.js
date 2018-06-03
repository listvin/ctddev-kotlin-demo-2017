var nodeSize = 12;
var nodeGuardSize = 9;
var dashPeriod = 9;
var nodeHitBoxSize = 14;

Array.prototype.expand = function(newSize, fun){
    for (var i = length; i < newSize; ++i) this.push(fun())
};
Array.prototype.remove = function(what){
    this.splice(this.indexOf(what), 1);
};

var graph = Viva.Graph.graph();
var graphics = Viva.Graph.View.svgGraphics();
var subscription = [];

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

graph.clearUndirectedEdges = function() {
    var list = document.getElementsByClassName("tree_edge");
    for (var i = 0; i < list.length; ++i) {
        console.log(list[i]);
        SVG_rootg.removeChild(list[i]);
    }
};

function redrawUEdge(uEdgeUI){
    var fromPos = getNodeUI(uEdgeUI.__from);
    var toPos = getNodeUI(uEdgeUI.__to);

    var dx = toPos.x - fromPos.x;
    var dy = toPos.y - fromPos.y;
    var len = Math.sqrt(dx * dx + dy * dy);
    var shift = (len%dashPeriod)/4.0;
    dx /= len;
    dy /= len;

    var fromPosC = {}, toPosC = {};

    fromPosC.x = fromPos.x + dx * (nodeGuardSize + shift);
    fromPosC.y = fromPos.y + dy * (nodeGuardSize + shift);
    toPosC.x = toPos.x - dx * nodeGuardSize;
    toPosC.y = toPos.y - dy * nodeGuardSize;

    // var a = getNodeUI(uEdgeUI.__from);
    // var b = getNodeUI(uEdgeUI.__to);
    // var A = {}, B = {};
    // A.x = a.getAttribute("cx");
    // A.y = a.getAttribute("cy");
    // B.x = b.getAttribute("cx");
    // B.y = b.getAttribute("cy");
    uEdgeUI.attr("d", "M " + strPair(fromPosC) + "L " + strPair(toPosC));
}

graph.addUndirectedEdge = function (a, b) {
    var uEdgeUI = Viva.Graph.svg('path').attr("class", "tree_edge");
    uEdgeUI.id = "uedge" + Math.min(a,b) + "_" + Math.max(a,b);
    uEdgeUI.__from = a;
    uEdgeUI.__to = b;
    subscription[a].push(uEdgeUI);
    subscription[b].push(uEdgeUI);
    redrawUEdge(uEdgeUI);
    SVG_rootg.append(uEdgeUI);
};

graph.removeUndirectedEdge = function (a, b) {
    SVG_rootg.removeChild(document.getElementById("uedge" + Math.min(a,b) + "_" + Math.max(a,b)));
    subscription[a].remove(b);
    subscription[b].remove(a);
};

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
    g.__id = node.id;
    if (node.id >= subscription.length) subscription.expand(node.id+1, function () { return [] });

    g.append(Viva.Graph.svg('circle').attr('r', nodeSize).attr('class','node_body').attr('id','nodeNumber'+node.id));
    g.append(caption);
    g.append(Viva.Graph.svg('circle').attr('r', nodeHitBoxSize).attr('fill','transparent'));
    g.addEventListener("touchend", function() {
        Feedback.touched(node.id)
    });
    g.addEventListener("touchstart", function() {
        setTouch(node.id)
    });
    return g
}).placeNode(function(nodeUI, pos) {
    nodeUI.children[0].attr('cx', pos.x).attr('cy', pos.y);
    nodeUI.children[0].x = pos.x; //because svg seems to spoil attributes time to time
    nodeUI.children[0].y = pos.y;
    nodeUI.children[1].attr('x', pos.x).attr('y', pos.y);
    nodeUI.children[2].attr('cx', pos.x).attr('cy', pos.y);
    subscription[nodeUI.__id].forEach(function (artiPath) {
        redrawUEdge(artiPath)
    })
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
    dx /= len;
    dy /= len;
    var normR = {}, normL = {};
    normR.x = fromPos.x + dy * nodeSize;
    normR.y = fromPos.y - dx * nodeSize;
    normL.x = fromPos.x - dy * nodeSize;
    normL.y = fromPos.y + dx * nodeSize;
    linkUI.children[0].attr("points", strPair(normR) + strPair(normL) + strPair(toPos));

    linkUI.children[1].attr("d", 'M' + fromPos.x + ',' + fromPos.y + 'L' + toPos.x + ',' + toPos.y);
});

var layout = Viva.Graph.Layout.forceDirected(graph, {
    springLength : nodeSize*5,
    springCoeff : 5e-5,
    dragCoeff : 3e-2,
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

SVG_rootg = document.getElementsByTagName("svg")[0].children[0];

if (typeof Feedback === "undefined") {
    graph.addNode(0);
    graph.addNode(1);
    // graph.addLink(0, 1, "_t");
    graph.addLink(1, 2, "s_");
    graph.addLink(2, 3, "l_");

    // setTimeout(500, function () {
        graph.addUndirectedEdge(0,1);
    // });
}