import {select, scaleLinear, zoom, zoomIdentity, zoomTransform, event, line, curveStep, drag} from "d3";

import {Node} from './node';
import {Edge, edgeId} from './edge';
import { GraphObjectCache } from "./object-cache";

const SHADOW_DOM_TEMPLATE = `
<style>
</style>
`


export default class GraphEditor extends HTMLElement {

    private mutationObserver: MutationObserver;

    private initialized: boolean;
    private root: ShadowRoot;
    private xScale: scaleLinear;
    private yScale: scaleLinear;
    private zoom: zoom;
    private edgeGenerator;

    private contentMinHeight = 0;
    private contentMaxHeight = 1;
    private contentMinWidth = 0;
    private contentMaxWidth = 1;

    private hovered: Set<number|string> = new Set();

    private _nodes: Node[];
    private _edges: Edge[];
    private _mode: string = 'display'; // interaction mode ['display', 'layout', 'link', 'select']
    private allowZoom: boolean = true;

    private objectCache: GraphObjectCache;

    private interactionStateData: {
        source?: number|string,
        target?: number|string,
        selected?: Set<number|string>,
        fromMode?: string,
        [property: string]: any
    } = null;

    private get isInteractive(): boolean {
        return (this._mode !== 'display') && !(this._mode === 'select' && this.interactionStateData.fromMode === 'display');
    }

    constructor() {
        super();
        this._nodes = [];
        this._edges = [];
        this.objectCache = new GraphObjectCache();
        this.initialized = false;
        this.edgeGenerator = line().x((d) => d.x).y((d) => d.y).curve(curveStep);

        this.root = this.attachShadow({mode: 'open'});

        select(this.root).html(SHADOW_DOM_TEMPLATE);

        this.mutationObserver = new MutationObserver(() => {
            this.updateTemplates();
            this.completeRender(true);
            this.zoomToBoundingBox();
        });
    }

    get nodeList() {
        return this._nodes;
    }

    set nodeList(nodes: Node[]) {
        this._nodes = nodes;
        this.objectCache.updateNodeCache(nodes);
    }

    get edgeList() {
        return this._edges;
    }

    set edgeList(edges: Edge[]) {
        this._edges = edges;
        this.objectCache.updateEdgeCache(edges);
    }

    get mode() {
        return this._mode;
    }

    set mode(mode: string) {
        this.updateMode(mode.toLowerCase());
        this.setAttribute('mode', mode);
    }

    static get observedAttributes() { return ['nodes', 'edges', 'mode', 'zoom']; }

    attributeChangedCallback(name, oldValue, newValue: string) {
        if (name === 'nodes') {
            newValue = newValue.replace(/'/g, '"');
            console.log('Nodes ' + newValue);
            this.nodeList = JSON.parse(newValue);
        }
        if (name === 'edges') {
            newValue = newValue.replace(/'/g, '"');
            console.log('Edges ' + newValue);
            this.edgeList = JSON.parse(newValue);
        }
        if (name === 'zoom') {
            this.allowZoom = (newValue.toLowerCase() !== 'true')
            this.completeRender();
        }
        if (name === 'mode') {
            this.updateMode(newValue.toLowerCase());
        }
        this.initialize();
        this.completeRender();
        this.zoomToBoundingBox();
    }

    /**
     * Set the graph interaction mode and cleanup temp data from old interaction mode.
     *
     * @param mode interaction mode (one of ["display", "layout", "link", "select"])
     */
    updateMode(mode: string) {
        console.log(mode);
        if (mode === this._mode) {
            return;
        }
        if (mode === 'display') {
            if (this._mode != 'display') {
                this.interactionStateData = null;
                this._mode = 'display';
                this.completeRender();
            }
        } else if (mode === 'layout') {
            if (this._mode != 'layout') {
                this.interactionStateData = null;
                this._mode = 'layout';
                this.completeRender();
            }
        } else if (mode === 'link') {
            if (this._mode != 'link') {
                this.interactionStateData = {
                    source: null,
                    target: null,
                    allowedTargets: new Set(),
                }
                this._mode = 'link';
                this.completeRender();
            }
        } else if (mode === 'select') {
            if (this._mode != 'select') {
                this.interactionStateData = {
                    selected: new Set(),
                    fromMode: this._mode,
                }
                this._mode = 'select';
                this.completeRender();
            }
        } else {
            console.log(`Wrong mode "${mode}". Allowed are: ["display", "layout", "link", "select"]`)
        }
    }

    connectedCallback() {
        if (! this.isConnected) {
            return;
        }
        //this.initialize();
        this.completeRender();
        this.zoomToBoundingBox();

        this.mutationObserver.observe(this, {
            childList: true,
            characterData: true,
            subtree: true,
        });
    }

    /**
     * Initialize the shadow dom with a drawing svg.
     */
    initialize() {
        if (!this.initialized) {
            this.initialized = true;

            const svg = select(this.root).append('svg')
                .attr('class', 'graph-editor')
                .attr('width', '100%')
                .attr('height', '100%');

            this.xScale = scaleLinear()
                .domain([10, 0])
                .range([0, 10]);
            this.yScale = scaleLinear()
                .domain([10, 0])
                .range([0, 10]);

            const defs = svg.append('defs');

            // setup filters ///////////////////////////////////////////////////

            // for edges
            const edgeShadow = defs.append('filter')
                .attr('id', 'shadow-edge')
                .attr('height', '130%');

            edgeShadow.append('feGaussianBlur')
                .attr('in', 'SourceAlpha')
                .attr('stdDeviation', 5)
                .attr('result', 'blur');

            edgeShadow.append('feOffset')
                .attr('in', 'blur')
                .attr('dx', 3)
                .attr('dy', 3)
                .attr('result', 'offsetBlur');

            edgeShadow.append('feComponentTransfer')
              .append('feFuncA')
                .attr('type', 'linear')
                .attr('slope', '0.3');

            const mergeLinkShadow = edgeShadow.append('feMerge');

            mergeLinkShadow.append('feMergeNode');

            mergeLinkShadow.append('feMergeNode')
                .attr('in', 'SourceGraphic');

            // for normal nodes
            const smallShadow = defs.append('filter')
                .attr('id', 'shadow-small')
                .attr('height', '130%');

            smallShadow.append('feGaussianBlur')
                .attr('in', 'SourceAlpha')
                .attr('stdDeviation', 5)
                .attr('result', 'blur');

            smallShadow.append('feOffset')
                .attr('in', 'blur')
                .attr('dx', 3)
                .attr('dy', 3)
                .attr('result', 'offsetBlur');

            smallShadow.append('feComponentTransfer')
              .append('feFuncA')
                .attr('type', 'linear')
                .attr('slope', '0.3');

            const mergeSmallShadow = smallShadow.append('feMerge');

            mergeSmallShadow.append('feMergeNode');

            mergeSmallShadow.append('feMergeNode')
                .attr('in', 'SourceGraphic');

            // for highlighted nodes
            const largeShadow = defs.append('filter')
                .attr('id', 'shadow-large')
                .attr('y', '-20%')
                .attr('height', '150%');

            largeShadow.append('feGaussianBlur')
                .attr('in', 'SourceAlpha')
                .attr('stdDeviation', 8)
                .attr('result', 'blur');

            largeShadow.append('feOffset')
                .attr('in', 'blur')
                .attr('dx', 5)
                .attr('dy', 5)
                .attr('result', 'offsetBlur');

            largeShadow.append('feComponentTransfer')
              .append('feFuncA')
                .attr('type', 'linear')
                .attr('slope', '0.6');

            const mergeLargeShadow = largeShadow.append('feMerge');

            mergeLargeShadow.append('feMergeNode');

            mergeLargeShadow.append('feMergeNode')
                .attr('in', 'SourceGraphic');


            // for inactive nodes
            let inactive = defs.append('filter')
                .attr('id', 'inactive')
                .attr('height', '130%');

            inactive.append('feColorMatrix')
                .attr('in', 'SourceGraphic')
                .attr('type', 'matrix')
                .attr('values', '.33 .33 .33 0 0 \n .33 .33 .33 0 0 \n .33 .33 .33 0 0 \n .33 .33 .33 0 0')
                .attr('result', 'faded');

            inactive.append('feGaussianBlur')
                .attr('in', 'faded')
                .attr('stdDeviation', 5)
                .attr('result', 'blur');

            inactive.append('feOffset')
                .attr('in', 'blur')
                .attr('dx', 3)
                .attr('dy', 3)
                .attr('result', 'offsetBlur');

            inactive.append('feComponentTransfer')
              .append('feFuncA')
                .attr('type', 'linear')
                .attr('slope', '0.3');

            let mergeInactive = inactive.append('feMerge');

            mergeInactive.append('feMergeNode');

            mergeInactive.append('feMergeNode')
                .attr('in', 'faded');

            // setup graph groups //////////////////////////////////////////////

            const graph = svg.append('g')
                .attr('class', 'zoom-group');

            this.zoom = zoom().on('zoom', (d) => {
                graph.attr('transform', event.transform);
            });

            graph.append('g')
                .attr('class', 'edges')
            //    .attr('filter', 'url(#shadow-edge)') <- performance drain...
            //  .append('circle')
            //    .attr('cx', -100)
            //    .attr('cy', -100)
            //    .attr('r', 0.1)
            //    .attr('fill', '#FFFFFF'); // fix for shadow of first line

            graph.append('g')
                .attr('class', 'nodes');

            this.updateSize();
        }
    }

    private getSvg() {
        return select(this.root).select('svg.graph-editor');
    }

    /**
     * Calculate and store the size of the svg.
     *
     * @private
     */
    private updateSize() {
        const svg = this.getSvg();
        this.contentMaxHeight = parseInt(svg.style('height').replace('px', ''), 10);
        this.contentMaxWidth = parseInt(svg.style('width').replace('px', ''), 10);

        this.yScale.range([0, Math.max(this.contentMaxHeight, this.contentMinHeight)]);
        this.xScale.range([0, Math.max(this.contentMaxWidth, this.contentMinWidth)]);
    }

    /**
     * Zooms and pans the graph to get all content inside the visible area.
     */
    private zoomToBoundingBox() {
        if (! this.initialized || ! this.isConnected) {
            return;
        }
        const svg = this.getSvg();

        // reset zoom
        svg.call(this.zoom.transform, zoomIdentity);

        const box: SVGRect = svg.select('g.zoom-group').select('g.nodes').node().getBBox();
        const scale = 0.9 * Math.min(this.contentMaxWidth / box.width, this.contentMaxHeight / box.height);

        const xCorrection = (-box.x * scale) + ((this.contentMaxWidth - (box.width * scale)) / 2);
        const yCorrection = (-box.y * scale) + ((this.contentMaxHeight - (box.height * scale)) / 2);

        let newZoom = zoomTransform(svg.node())
            .translate(xCorrection, yCorrection)
            .scale(scale);

        if (isNaN(xCorrection) || isNaN(yCorrection)) {
            newZoom = zoomIdentity;
        }
        svg.call(this.zoom.transform, newZoom);
    }

    /**
     * Get templates in this dom-node and render them into defs node of svg or style tags.
     */
    updateTemplates() {
        const templates = select(this).selectAll('template');
        const styleTemplates = templates.filter(function() {
            return this.getAttribute('template-type') === 'style';
        });
        const stylehtml = [];
        styleTemplates.each(function() {
            // extract style attribute from template
            select(this.content).selectAll('style').each(function() {stylehtml.push(this)})
        });
        const styles = select(this.root).selectAll('style').data(stylehtml);
        styles.exit().remove();
        styles.enter().merge(styles).html((d) => d.innerHTML);

        const nodeTemplates = templates.filter(function() {
            return this.getAttribute('template-type') === 'node';
        });
        const nodehtml = [];
        nodeTemplates.each(function() {
            nodehtml.push(this);
        });
        const defs = this.getSvg().select('defs');
        const defTemplates = defs.selectAll('g.template').data(nodehtml, (d) => d.id);

        defTemplates.exit().remove();
        defTemplates.enter()
          .append('g')
          .merge(defTemplates)
            .attr('id', (d) => d.id)
            .html((d) => d.innerHTML);
    }

    /**
     * Render all changes of the data to the graph.
     */
    completeRender(updateTemplates: boolean=false) {
        if (! this.initialized || ! this.isConnected) {
            return;
        }
        const svg = this.getSvg();

        if (this.allowZoom) {
            svg.call(this.zoom);
        } else {
            svg.on('.zoom', null);
        }

        this.updateSize();

        const graph = svg.select("g.zoom-group");

        // update nodes ////////////////////////////////////////////////////////
        if (updateTemplates) {
            graph.select('.nodes').selectAll('g.node').remove();
        }

        let nodeSelection = graph.select('.nodes')
            .selectAll('g.node')
            .data(this._nodes, (d) => {return d.id;});

        nodeSelection.exit().remove();

        nodeSelection = nodeSelection.enter().append("g")
            .classed('node', true)
            .attr('id', (d) => d.id)
            .call(this.createNodes.bind(this))
          .merge(nodeSelection)
            .call(this.updateNodes.bind(this))
            .call(this.updateNodePositions.bind(this))
            .on('mouseover', (d) => {this.onNodeEnter.bind(this)(d);})
            .on('mouseout', (d) => {this.onNodeLeave.bind(this)(d);})
            .on('click', (d) => {this.onNodeClick.bind(this)(d);});

        if (this.isInteractive) {
            nodeSelection.call(drag().on('drag', (d) => {
                d.x = event.x;
                d.y = event.y;
                this.updateGraphPositions.bind(this)();
            }));
        } else {
            nodeSelection.on('.drag', null);
        }

        // update edges ////////////////////////////////////////////////////////
        if (updateTemplates) {
            graph.select('.edges').selectAll('g.edge:not(.dragged)').remove();
        }

        let edgeSelection = graph.select('.edges')
            .selectAll('path.edge:not(.dragged)')
            .data(this._edges, edgeId);

        edgeSelection.exit().remove();

        edgeSelection = edgeSelection.enter().append('path')
            .classed('edge', true)
            .attr('fill', 'none')
            .attr('id', edgeId)
          .merge(edgeSelection)
            .call(this.updateEdges.bind(this))
            .call(this.updateEdgePaths.bind(this));
    }


    /**
     * Add nodes to graph.
     *
     * @param nodeSelection d3 selection of nodes to add with bound data
     */
    private createNodes(nodeSelection) {
        const templateRoot = this.getSvg().select('defs');
        nodeSelection.html(function(d) {
            const template = templateRoot.select('#node');
            if (template.empty()) {
                return "<circle></circle>";
            }
            return template.html();
        });
    }

    /**
     * Update existing nodes.
     *
     * @param nodeSelection d3 selection of nodes to update with bound data
     */
    private updateNodes(nodeSelection) {
        if (nodeSelection == null) {
            const svg = this.getSvg();

            const graph = svg.select("g.zoom-group");
            nodeSelection = graph.select('.nodes')
                .selectAll('g.node')
                .data(this._nodes, (d) => {return d.id;});
        }

        nodeSelection
            .call(this.updateNodeHighligts.bind(this));
    }

    /**
     * Update node positions.
     *
     * @param nodeSelection d3 selection of nodes to update with bound data
     */
    private updateNodePositions(nodeSelection) {
        nodeSelection.attr('transform', (d) => {
                const x = d.x != null ? d.x : 0;
                const y = d.y != null ? d.y : 0;
                return `translate(${x},${y})`;
            });
    }

    /**
     * Update existing edges.
     *
     * @param edgeSelection d3 selection of edges to update with bound data
     */
    private updateEdges(edgeSelection) {
        if (edgeSelection == null) {
            const svg = this.getSvg();

            const graph = svg.select("g.zoom-group");
            edgeSelection = graph.select('.edges')
                .selectAll('path.edge:not(.dragged)')
                .data(this._edges, edgeId);
        }

        edgeSelection
            .attr('stroke', 'black')
            .call(this.updateEdgeHighligts.bind(this));
    }

    /**
     * Update existing edge path.
     *
     * @param edgeSelection d3 selection of edges to update with bound data
     */
    private updateEdgePaths(edgeSelection) {
        edgeSelection.attr('d', (d) => {
            const source = this.objectCache.getNode(d.source);
            const target = this.objectCache.getNode(d.target);
            return this.edgeGenerator([source, target]);
        });
    }

    /**
     * UUpdate all node positions and edge paths.
     */
    private updateGraphPositions() {
        const svg = this.getSvg();

        const graph = svg.select("g.zoom-group");
        const nodeSelection = graph.select('.nodes')
            .selectAll('g.node')
            .data(this._nodes, (d) => {return d.id;})
            .call(this.updateNodePositions.bind(this));
        const edgeSelection = graph.select('.edges')
            .selectAll('path.edge:not(.dragged)')
            .data(this._edges, edgeId)
            .call(this.updateEdgePaths.bind(this));
    }

    /**
     * Callback on nodes for mouseEnter event.
     *
     * @param nodeDatum Corresponding datum of node
     */
    private onNodeEnter(nodeDatum) {
        this.hovered.add(nodeDatum.id);
        if (this._mode === 'link' && this.interactionStateData.source != null) {
            this.interactionStateData.target = nodeDatum.id;
        }
        this.updateNodeHighligts();
        this.updateEdgeHighligts();
    }

    /**
     * Callback on nodes for mouseLeave event.
     *
     * @param nodeDatum Corresponding datum of node
     */
    private onNodeLeave(nodeDatum) {
        this.hovered.delete(nodeDatum.id);
        if (this._mode === 'link' && this.interactionStateData.target === nodeDatum.id) {
            this.interactionStateData.target = null;
        }
        this.updateNodeHighligts();
        this.updateEdgeHighligts();
    }

    /**
     * Callback on nodes for click event.
     *
     * @param nodeDatum Corresponding datum of node
     */
    private onNodeClick(nodeDatum) {
        if (this._mode === 'link') {
            return this.onNodeSelectLink(nodeDatum);
        }
        if (this._mode !== 'select') {
            this.updateMode('select');
            this.interactionStateData.selected.add(nodeDatum.id);
        } else if (this.interactionStateData.selected.has(nodeDatum.id)) {
            this.interactionStateData.selected.delete(nodeDatum.id);
            if (this.interactionStateData.selected.size <= 0) {
                this.updateMode(this.interactionStateData.fromMode);
            }
        } else {
            this.interactionStateData.selected.add(nodeDatum.id);
        }
        this.updateNodeHighligts();
        this.updateEdgeHighligts();
    }

    /**
     * Selection logik in 'link' mode.
     *
     * @param nodeDatum Corresponding datum of node
     */
    private onNodeSelectLink(nodeDatum) {
        if (this.interactionStateData.source == null) {
            this.interactionStateData.source = nodeDatum.id;
            return;
        }
        if (nodeDatum.id === this.interactionStateData.source) {
            // doesn't handle edges to self
            this.interactionStateData.source = null;
            this.interactionStateData.target = null;
            return;
        }
        this.interactionStateData.target = nodeDatum.id;
        const oldEdge = this._edges.findIndex((e) => {
            return (e.source === this.interactionStateData.source) &&
            (e.target === this.interactionStateData.target);
        });
        if (oldEdge !== -1) {
            this._edges.splice(oldEdge, 1);
        } else {
            this._edges.push({
                source: this.interactionStateData.source,
                target: this.interactionStateData.target,
            });
        }
        this.objectCache.updateEdgeCache(this._edges);
        this.completeRender();
        this.interactionStateData.source = null;
        this.interactionStateData.target = null;
    }

    /**
     * Calculate highlighted nodes and update their classes.
     */
    private updateNodeHighligts(nodeSelection?) {
        if (nodeSelection == null) {
            const svg = this.getSvg();

            const graph = svg.select("g.zoom-group");
            nodeSelection = graph.select('.nodes')
                .selectAll('g.node')
                .data(this._nodes, (d) => {return d.id;});
        }

        nodeSelection
            .classed('hovered', (d) => this.hovered.has(d.id))
            .classed('selected', (d) => {
                if (this._mode === 'select') {
                    const selected = this.interactionStateData.selected;
                    if (selected != null) {
                        return selected.has(d.id);
                    }
                }
                if (this._mode === 'link') {
                    if (this.interactionStateData.source != null) {
                        if (d.id === this.interactionStateData.source) {
                            return true;
                        }
                    }
                }
                return false;
            });
    }

    /**
     * Calculate highlighted edges and update their classes.
     */
    private updateEdgeHighligts(edgeSelection?) {
        if (edgeSelection == null) {
            const svg = this.getSvg();

            const graph = svg.select("g.zoom-group");
            edgeSelection = graph.select('.edges')
                .selectAll('path.edge:not(.dragged)')
                .data(this._edges, edgeId);
        }

        let nodes: Set<number|string> = new Set();

        if (this.mode === 'link') {
            if (this.interactionStateData.source != null) {
                nodes.add(this.interactionStateData.source);
            }
        } else {
            nodes = this.hovered;
        }

        edgeSelection
            .classed('highlight-outgoing', (d) => nodes.has(d.source))
            .classed('highlight-incoming', (d) => nodes.has(d.target));
    }
}