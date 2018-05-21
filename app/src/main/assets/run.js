var graph = Viva.Graph.graph();
var graphics = Viva.Graph.View.svgGraphics(),
    nodeSize = 10;

graph.addNode(0, "zero")
graph.addNode(1, "unit")
graph.addLink(0,1)

graphics.node(function(node) {
    var el = Viva.Graph.svg('text').attr('text-anchor',"middle")
    el.textContent = node.data == null ? node.id : node.data

    el.addEventListener("click", function() {
        if (typeof Feedback == "undefined") {
            console.log("no js interface, clicked on " + node.id)
            el.textContent += "-"
        } else {
            // console.log("GA")
            el.textContent += "+"
            Feedback.touched(node.id)
        }
        // highlightRelatedNodes(node.id, true);
    });

    return el
}).placeNode(function(nodeUI, pos) {
    nodeUI.attr('x', pos.x - nodeSize / 2).attr('y', pos.y - nodeSize / 2);
});

function strPair(pnt){
    return pnt.x+","+pnt.y+" "
}

graphics.link(function(link){
    // Notice the Triangle marker-end attribe:
    return Viva.Graph.svg('polyline')
               .attr('style', 'fill:limegreen');
}).placeLink(function(linkUI, fromPos, toPos) {
    // Here we should take care about
    //  "Links should start/stop at node's bounding box, not at the node center."
    // For rectangular nodes Viva.Graph.geom() provides efficient way to find
    // an intersection point between segment and rectangle
    var dx = toPos.x - fromPos.x
    var dy = toPos.y - fromPos.y
    var len = Math.sqrt(dx*dx + dy*dy)
    var normR = {}, normL = {}
    normR.x = fromPos.x + dy/len*nodeSize
    normR.y = fromPos.y - dx/len*nodeSize
    normL.x = fromPos.x - dy/len*nodeSize
    normL.y = fromPos.y + dx/len*nodeSize
    linkUI.attr("points", strPair(normR) + strPair(normL) + strPair(toPos));
});

var renderer = Viva.Graph.View.renderer(graph, {
        graphics : graphics
    });
renderer.run();
