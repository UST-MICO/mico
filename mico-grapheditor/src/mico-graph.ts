import {select, scaleLinear, zoom, zoomIdentity, zoomTransform, event, line, curveBasis, drag} from "d3";

import {Node} from './node';
import {Edge, DraggedEdge, edgeId} from './edge';
import { LinkHandle, handlesForRectangle, handlesForCircle, calculateNormal } from "./link-handle";
import { GraphObjectCache } from "./object-cache";
import { wrapText } from "./textwrap";
import { calculateAngle } from "./rotation-vector";

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
    private draggedEdges: DraggedEdge[];
    private _mode: string = 'display'; // interaction mode ['display', 'layout', 'link', 'select']
    private _zoomMode: string = 'both'; // ['none', 'manual', 'automatic', 'both']

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

    /**
     * Callback when a new dragged edge is created.
     *
     * Use this callback only to customize the edge attributes like markers or type!
     */
    public onCreateDraggedEdge: (edge: DraggedEdge) => DraggedEdge;

    /**
     * Callback dragged edge has a new target.
     *
     * Only modify the existing edge!
     */
    public onDraggedEdgeTargetChange: (edge: DraggedEdge, sourceNode?: Node, targetNode?: Node) => void;

    /**
     * Callback when a existing dragged edge is dropped.
     *
     * Use this callback only to customize the edge attributes like markers or type!
     */
    public onDropDraggedEdge: (edge: DraggedEdge, sourceNode?: Node, targetNode?: Node) => Edge;

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
        this.setMode(mode.toLowerCase());
        select(this).attr('mode', mode);
    }

    get zoomMode() {
        return this._zoomMode;
    }

    set zoomMode(mode: string) {
        this.setZoomMode(mode.toLowerCase());
        select(this).attr('zoom', mode);
    }

    constructor() {
        super();
        this._nodes = [];
        this._edges = [];
        this.draggedEdges = [];
        this.objectCache = new GraphObjectCache();
        this.initialized = false;
        this.edgeGenerator = line().x((d) => d.x).y((d) => d.y).curve(curveBasis);

        this.root = this.attachShadow({mode: 'open'});

        select(this.root).html(SHADOW_DOM_TEMPLATE);

        this.mutationObserver = new MutationObserver((mutations) => {
            this.updateTemplates();
            this.completeRender(true);
            this.zoomToBoundingBox(false);
        });
    }

    connectedCallback() {
        if (! this.isConnected) {
            return;
        }
        //this.initialize();
        this.completeRender();
        this.zoomToBoundingBox(false);

        this.mutationObserver.observe(this, {
            childList: true,
            characterData: false,
            subtree: false,
        });
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
            this.setZoomMode(newValue.toLowerCase());
            this.completeRender();
        }
        if (name === 'mode') {
            this.setMode(newValue.toLowerCase());
        }
        this.initialize();
        this.completeRender();
        this.zoomToBoundingBox(false);
    }

    /**
     * Set nodes and redraw graph.
     *
     * @param nodes new nodeList
     * @param redraw if graph should be redrawn
     */
    public setNodes(nodes: Node[], redraw: boolean = false) {
        this.nodeList = nodes;
        if (redraw) {
            this.completeRender();
            this.zoomToBoundingBox(false);
        }
    }

    /**
     * Add a single node to the graph.
     *
     * @param node node to add
     * @param redraw if graph should be redrawn
     */
    public addNode(node: Node, redraw: boolean = false) {
        this._nodes.push(node);
        this.objectCache.updateNodeCache(this._nodes);
        this.onNodeCreate(node);
        if (redraw) {
            this.completeRender();
            this.zoomToBoundingBox(false);
        }
    }

    /**
     * Remove a single node from the graph.
     *
     * @param node node or id to remove
     * @param redraw if the graph should be redrawn
     */
    public removeNode(node: Node|number|string, redraw: boolean = false) {
        const id: string|number = (node as Node).id != null ? (node as Node).id : (node as number|string);
        const index = this._nodes.findIndex(n => n.id === id);
        if (index >= 0) {
            this.onNodeRemove(this._nodes[index]);
            this._nodes.splice(index, 1);
            this.objectCache.updateNodeCache(this._nodes);
            const newEdgeList = [];
            this._edges.forEach(edge => {
                if (edge.source === id) {
                    this.onEdgeRemove(edge);
                    return;
                }
                if (edge.target === id) {
                    this.onEdgeRemove(edge);
                    return;
                }
                newEdgeList.push(edge);
            });
            this.edgeList = newEdgeList;
            if (redraw) {
                this.completeRender();
                this.zoomToBoundingBox(false);
            }
        }
    }

    /**
     * Set edges and redraw graph.
     *
     * @param nodes new edgeList
     * @param redraw if the graph should be redrawn
     */
    public setEdges(nodes: Edge[], redraw: boolean = false) {
        this.edgeList = nodes;
        if (redraw) {
            this.completeRender();
            this.zoomToBoundingBox(false);
        }
    }

    /**
     * Add a single edge to the graph.
     *
     * @param edge edge to add
     * @param redraw if graph should be redrawn
     */
    public addEdge(edge: Edge, redraw: boolean = false) {
        this._edges.push(edge);
        this.objectCache.updateEdgeCache(this._edges);
        if (redraw) {
            this.completeRender();
            this.zoomToBoundingBox(false);
        }
    }

    /**
     * Remove a single edge from the graph.
     *
     * @param edge edge or id to remove
     * @param redraw if the graph should be redrawn
     */
    public removeEdge(edge: Edge, redraw: boolean = false) {
        const index = this._edges.findIndex((e) => {
            return (e.source === edge.source) &&
            (e.target === edge.target);
        });
        if (index >= 0) {
            this.onEdgeRemove(this._edges[index]);
            this._edges.splice(index, 1);
            this.objectCache.updateEdgeCache(this._edges);
            if (redraw) {
                this.completeRender();
                this.zoomToBoundingBox(false);
            }
        }
    }

    /**
     * Set the graph interaction mode and cleanup temp data from old interaction mode.
     *
     * @param mode interaction mode (one of ["display", "layout", "link", "select"])
     */
    public setMode(mode: string) {
        if (mode === this._mode) {
            return;
        }
        const oldMode = this._mode;
        if (mode === 'display') {
            if (this._mode != 'display') {
                this.interactionStateData = null;
                this._mode = 'display';
            }
        } else if (mode === 'layout') {
            if (this._mode != 'layout') {
                this.interactionStateData = null;
                this._mode = 'layout';
            }
        } else if (mode === 'link') {
            if (this._mode != 'link') {
                this.interactionStateData = {
                    source: null,
                    target: null,
                    allowedTargets: new Set(),
                }
                this._mode = 'link';
            }
        } else if (mode === 'select') {
            if (this._mode != 'select') {
                this.interactionStateData = {
                    selected: new Set(),
                    fromMode: this._mode,
                }
                this._mode = 'select';
            }
        } else {
            console.log(`Wrong mode "${mode}". Allowed are: ["display", "layout", "link", "select"]`);
            return
        }

        if (oldMode !== mode) {
            const ev = new CustomEvent('modechange', {
                bubbles: true,
                composed: true,
                cancelable: false,
                detail: {oldMode: oldMode, newMode: mode}});
            this.dispatchEvent(ev);
            this.completeRender();
        }
    }

    /**
     * Set the graph zoom mode.
     *
     * @param mode zoom mode (one of ["none", "manual", "automatic", "both"])
     */
    public setZoomMode(mode: string) {
        if (mode === this._mode) {
            return;
        }
        const oldMode = this._mode;
        if (mode === 'none') {
            if (this._zoomMode != 'none') {
                this._zoomMode = 'none';
            }
        } else if (mode === 'manual') {
            if (this._zoomMode != 'manual') {
                this._zoomMode = 'manual';
            }
        } else if (mode === 'automatic') {
            if (this._mode != 'automatic') {
                this._zoomMode = 'automatic';
            }
        } else if (mode === 'both') {
            if (this._mode != 'both') {
                this._zoomMode = 'both';
            }
        } else {
            console.log(`Wrong zoom mode "${mode}". Allowed are: ["none", "manual", "automatic", "both"]`)
            return;
        }

        if (oldMode !== mode) {
            const ev = new CustomEvent('zoommodechange', {
                bubbles: true,
                composed: true,
                cancelable: false,
                detail: {oldMode: oldMode, newMode: mode}});
            this.dispatchEvent(ev);
            this.completeRender();
        }
    }

    /**
     * Initialize the shadow dom with a drawing svg.
     */
    private initialize() {
        if (!this.initialized) {
            this.initialized = true;

            const svg = select(this.root).append('svg')
                .attr('class', 'graph-editor')
                .attr('width', '100%')
                .attr('height', '100%');
            svg.append('defs');

            this.xScale = scaleLinear()
                .domain([10, 0])
                .range([0, 10]);
            this.yScale = scaleLinear()
                .domain([10, 0])
                .range([0, 10]);

            // setup graph groups //////////////////////////////////////////////

            const graph = svg.append('g')
                .attr('class', 'zoom-group');

            this.zoom = zoom().on('zoom', (d) => {
                graph.attr('transform', event.transform);
            });

            graph.append('g')
                .attr('class', 'edges');

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
     *
     * @param force if false only zooms in zoomMode 'automatic' and 'both' (default=true)
     */
    public zoomToBoundingBox = (force: boolean=true) => {
        if (! this.initialized || ! this.isConnected) {
            return;
        }

        if (!(force || this._zoomMode === 'automatic' || this._zoomMode === 'both')) {
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
     *
     * @param nodeTemplateList list of node templates to use instead of html templates
     * @param styleTemplateList list of style templates to use instead of html templates (not wrapped in style tag!)
     */
    public updateTemplates = (nodeTemplateList?: {id: string, innerHTML: string, [prop: string]: any}[],
                              styleTemplateList?: {id?: string, innerHTML: string, [prop: string]: any}[],
                              markerTemplateList?: {id: string, innerHTML: string, [prop: string]: any}[]) => {
        const templates = select(this).selectAll('template');
        const stylehtml = styleTemplateList != null ? styleTemplateList : [];
        const nodehtml = nodeTemplateList != null ? nodeTemplateList : [];
        const markerhtml = markerTemplateList != null ? markerTemplateList : [];

        if (styleTemplateList == null) {
            const styleTemplates = templates.filter(function() {
                return this.getAttribute('template-type') === 'style';
            });
            styleTemplates.each(function() {
                // extract style attribute from template
                select(this.content).selectAll('style').each(function() {stylehtml.push(this)})
            });
        }
        const styles = select(this.root).selectAll('style').data(stylehtml);
        styles.exit().remove();
        styles.enter().merge(styles).html((d) => d.innerHTML);

        if (nodeTemplateList == null) {
            const nodeTemplates = templates.filter(function() {
                return this.getAttribute('template-type') === 'node';
            });
            nodeTemplates.each(function() {
                nodehtml.push(this);
            });
        }

        this.objectCache.updateNodeTemplateCache(nodehtml);

        if (markerTemplateList == null) {
            const markerTemplates = templates.filter(function() {
                return this.getAttribute('template-type') === 'marker';
            });
            markerTemplates.each(function() {
                markerhtml.push(this);
            });
        }

        this.objectCache.updateMarkerTemplateCache(markerhtml);
    }

    /**
     * Render all changes of the data to the graph.
     */
    public completeRender(updateTemplates: boolean=false) {
        if (! this.initialized || ! this.isConnected) {
            return;
        }
        const svg = this.getSvg();

        if (this._zoomMode === 'manual' || this._zoomMode === 'both') {
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
                this.onNodePositionChange.bind(this)(d);
                this.updateGraphPositions.bind(this)();
            }));
        } else {
            nodeSelection.on('.drag', null);
        }

        // update edges ////////////////////////////////////////////////////////
        if (updateTemplates) {
            graph.select('.edges').selectAll('g.edge:not(.dragged)').remove();
        }
        const self = this;
        let edgeGroupSelection = graph.select('.edges')
            .selectAll('g.edge-group:not(.dragged)')
            .data(this._edges, edgeId);
        edgeGroupSelection.exit().remove();
        edgeGroupSelection.enter()
            .append('g')
            .attr('id', (d) => edgeId(d))
            .classed('edge-group', true)
            .each(function () {
                const edgeGroup = select(this);
                edgeGroup.append('path')
                    .classed('edge', true)
                    .attr('fill', 'none');

                edgeGroup.append('circle')
                    .classed('link-handle', true)
                    .attr('fill', 'black')
                    .attr('r', 3);
            })
          .merge(edgeGroupSelection)
            .classed('ghost', (d) => {
                const id = edgeId(d);
                return this.draggedEdges.some((edge) => edge.createdFrom === id);
            })
            .call(self.updateEdgeGroups.bind(this))
            .call(self.updateEdgePositions.bind(this))
            .on('click', (d) => {this.onEdgeClick.bind(this)(d);});
    }


    /**
     * Add nodes to graph.
     *
     * @param nodeSelection d3 selection of nodes to add with bound data
     */
    private createNodes(nodeSelection) {
        nodeSelection
            .attr('data-template', (d) => this.objectCache.getNodeTemplateId(d.type))
            .html((d) => {
                return this.objectCache.getNodeTemplate(d.type);
            })
            .call(this.updateLinkHandles.bind(this));
    }

    private updateLinkHandles(nodeSelection) {
        const self = this;
        nodeSelection.each(function(d) {
            if (self.objectCache.getNodeTemplateLinkHandles(d.type) != null) {
                return;
            }
            let backgroundSelection = select(this).select('.outline');
            if (backgroundSelection.empty()) {
                backgroundSelection = select(this).select(':first-child');
            }
            if (backgroundSelection.empty()) {
                self.objectCache.setNodeTemplateLinkHandles(d.type, [{
                    id: 1,
                    x: 0,
                    y: 0,
                }]);
                return;
            }
            let linkHandles = backgroundSelection.attr('data-link-handles');
            if (linkHandles == null) {
                linkHandles = 'all';
            } else {
                if (linkHandles.startsWith('[')) {
                    try {
                        linkHandles = JSON.parse(linkHandles);
                        linkHandles.forEach((element, index) => element.id = index);
                        linkHandles.forEach(calculateNormal);
                        self.objectCache.setNodeTemplateLinkHandles(d.type, linkHandles);
                        return;
                    } catch (error) {
                        linkHandles = 'all';
                    }
                }
                linkHandles = linkHandles.toLowerCase();
            }
            if (backgroundSelection.node().tagName === 'circle') {
                const radius = parseFloat(backgroundSelection.attr('r'));
                const handles: LinkHandle[] = handlesForCircle(radius, linkHandles);
                self.objectCache.setNodeTemplateLinkHandles(d.type, handles);
            } else if (backgroundSelection.node().tagName === 'rect') {
                const x = parseFloat(backgroundSelection.attr('x'));
                const y = parseFloat(backgroundSelection.attr('y'));
                const width = parseFloat(backgroundSelection.attr('width'));
                const height = parseFloat(backgroundSelection.attr('height'));
                if (!isNaN(x+y+width+height)) {
                    const handles: LinkHandle[] = handlesForRectangle(x, y, width, height, linkHandles);
                    self.objectCache.setNodeTemplateLinkHandles(d.type, handles);
                }
            } else {
                self.objectCache.setNodeTemplateLinkHandles(d.type, []);
            }
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

            const graph = svg.select('g.zoom-group');
            nodeSelection = graph.select('.nodes')
                .selectAll('g.node')
                .data(this._nodes, (d) => {return d.id;});
        }

        // alias for this for use in closures
        const self = this;

        nodeSelection.each(function(d) {
            const node = select(this);
            let templateType = node.attr('data-template');
            if (templateType !== self.objectCache.getNodeTemplateId(d.type)) {
                node.selectAll().remove();
                self.createNodes(node);
            }
        });

        nodeSelection.each(function (node) {
            const handles = self.objectCache.getNodeTemplateLinkHandles(node.type);
            if (handles == null) {
                return;
            }
            let handleSelection = select(this).selectAll('circle.link-handle').data(handles, (handle) => handle.id);
            handleSelection.exit().remove();
            handleSelection = handleSelection.enter()
                .append('circle')
                .classed('link-handle', true)
              .merge(handleSelection)
                .attr('fill', 'black')
                .attr('cx', (d) => d.x)
                .attr('cy', (d) => d.y)
                .attr('r', 3);


            if (self.isInteractive) {
                handleSelection.call(
                    drag()
                        .subject((handle) => {
                            return self.createDraggedEdge(node);
                        })
                        .container(() => {return self.getSvg().select('g.zoom-group').select('g.edges').node();})
                        .on('drag', () => {
                            self.updateDraggedEdge();
                            self.updateDraggedEdgeGroups();
                        })
                        .on('end', self.dropDraggedEdge.bind(self))
                );
            } else {
                handleSelection.on('.drag', null);
            }
        })

        nodeSelection
            .call(this.updateNodeHighligts.bind(this));

        nodeSelection.each(function(d){
            const singleNodeSelection = select(this);
            const textSelection = singleNodeSelection.selectAll('.text').datum(function() {
                return this.getAttribute('data-content');
            });
            textSelection.each(function(attr) {
                let newText = '';
                if (attr != null) {
                    newText = d[attr];
                }
                if (newText == null) {
                    newText = '';
                }
                wrapText(this, newText);
            });
        });
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

    private updateEdgeGroups(edgeGroupSelection) {
        if (edgeGroupSelection == null) {
            const svg = this.getSvg();

            const graph = svg.select("g.zoom-group");
        }
        const self = this;
        edgeGroupSelection.each(function (d) {
                self.updateEdgeGroup(select(this), d);
            }, this)
            .call(this.updateEdgeHighligts.bind(this));
    }

    private updateDraggedEdgeGroups() {
        const svg = this.getSvg();

        const graph = svg.select("g.zoom-group");
        const edgeGroupSelection = graph.select('.edges')
            .selectAll('g.edge-group.dragged')
            .data(this.draggedEdges, edgeId);

        edgeGroupSelection.exit().remove();
        edgeGroupSelection.enter()
            .append('g')
            .attr('id', (d) => edgeId(d))
            .classed('edge-group', true)
            .classed('dragged', true)
            .each(function () {
                select(this).append('path')
                    .classed('edge', true)
                    .attr('fill', 'none');
            })
          .merge(edgeGroupSelection)
            .call(this.updateEdgeGroups.bind(this))
            .call(this.updateEdgePositions.bind(this));
    }

    private updateEdgePositions(edgeGroupSelection) {
        if (edgeGroupSelection == null) {
            const svg = this.getSvg();

            const graph = svg.select("g.zoom-group");
        }
        const self = this;
        edgeGroupSelection.select('path.edge')
            .call(this.updateEdgePath.bind(this));
        edgeGroupSelection.each(function (d) {
            select(this).selectAll('g.marker').data(d.markers != null ? d.markers : [])
                .call(self.updateMarkerPositions.bind(self));
        }).each(function () {
            const edgeGroup = select(this);
            const path = edgeGroup.select('path.edge');
            const length = path.node().getTotalLength();
            const linkMarkerOffset = 10;
            const linkHandlePos = path.node().getPointAtLength(length - linkMarkerOffset);
            edgeGroup.select('circle.link-handle')
                .attr('cx', linkHandlePos.x)
                .attr('cy', linkHandlePos.y)
                .raise();
        });
    }

    private updateEdgeGroup(edgeGroupSelection, d) {
        const pathSelection = edgeGroupSelection.select('path.edge:not(.dragged)').datum(d);
        pathSelection.call(this.updateEdge.bind(this));
        const markerSelection = edgeGroupSelection.selectAll('g.marker').data(d.markers != null ? d.markers : []);
        markerSelection.exit().remove();
        markerSelection.enter()
            .append('g')
            .classed('marker', true)
            .call(this.createMarker.bind(this))
          .merge(markerSelection)
            .call(this.updateMarker.bind(this))
            .call(this.updateMarkerPositions.bind(this));

        if (this.isInteractive) {
            edgeGroupSelection.select('circle.link-handle').call(drag()
            .subject(() => {
                return this.createDraggedEdgeFromExistingEdge(d);
            })
            .container(() => {return this.getSvg().select('g.zoom-group').select('g.edges').node();})
            .on('start', () => this.completeRender())
            .on('drag', () => {
                this.updateDraggedEdge();
                this.updateDraggedEdgeGroups();
            })
            .on('end', this.dropDraggedEdge.bind(this))
            );
        } else {
            edgeGroupSelection.select('circle.link-handle').on('.drag', null);
        }
    }

    /**
     * Update existing edges.
     *
     * @param edgeSelection d3 selection of edges to update with bound data
     */
    private updateEdge(edgeSelection) {
        if (edgeSelection == null) {
            const svg = this.getSvg();

            const graph = svg.select("g.zoom-group");
            edgeSelection = graph.select('.edges')
                .selectAll('path.edge:not(.dragged)')
                .data(this._edges, edgeId);
        }

        edgeSelection
            .attr('stroke', 'black');
    }

    /**
     * Update existing edge path.
     *
     * @param edgeSelection d3 selection of edges to update with bound data
     */
    private updateEdgePath(edgeSelection) {
        edgeSelection.attr('d', (d) => {
            const handles = this.objectCache.getEdgeLinkHandles(d);
            const points: {x: number, y: number, [prop: string]: any}[] = [];
            points.push(handles.sourceCoordinates);
            if (handles.sourceHandle.normal != null) {
                points.push({
                    x: handles.sourceCoordinates.x + (handles.sourceHandle.normal.dx * 10),
                    y: handles.sourceCoordinates.y + (handles.sourceHandle.normal.dy * 10),
                });
            }
            if (handles.targetHandle.normal != null) {
                points.push({
                    x: handles.targetCoordinates.x + (handles.targetHandle.normal.dx * 10),
                    y: handles.targetCoordinates.y + (handles.targetHandle.normal.dy * 10),
                });
            }
            points.push(handles.targetCoordinates);
            return this.edgeGenerator(points);
        });
    }

    private createMarker = (markerSelection) => {
        markerSelection
            .attr('data-template', (d) => d.template)
            .html((d) => {
                return this.objectCache.getMarkerTemplate(d.template);
            });
    }

    private updateMarker(markerSelection) {
        const self = this;
        markerSelection.each(function(d) {
            const marker = select(this);
            let templateType = marker.attr('data-template');
            if (templateType !== d.template) {
                marker.selectAll().remove();
                self.createMarker(marker);
            }
        });
    }

    private updateMarkerPositions(markerSelection) {
        markerSelection.each(function (d) {
            const parent = select(this.parentElement);
            const marker = select(this);
            const path = parent.select('path.edge');
            const length = path.node().getTotalLength();
            let positionOnLine = d.positionOnLine;
            if (positionOnLine === 'end') {
                positionOnLine = 1;
            }
            if (positionOnLine === 'start') {
                positionOnLine = 0;
            }
            positionOnLine = parseFloat(positionOnLine);
            if (isNaN(positionOnLine)) {
                positionOnLine = 0;
            }
            let transform = '';
            const point = path.node().getPointAtLength(length * positionOnLine);
            transform += `translate(${point.x},${point.y})`;
            if (d.scale != null) {
                transform += `scale(${d.scale})`;
            }
            if (d.rotate != null) {
                let angle = 0;
                if (d.rotate.normal == null) {
                    const epsilon = positionOnLine > 0.5 ? -1e-5 : 1e-5;
                    const point2 = path.node().getPointAtLength(length * (positionOnLine + epsilon));
                    let normal = {
                        dx: positionOnLine > 0.5 ? (point.x - point2.x) : (point2.x - point.x),
                        dy: positionOnLine > 0.5 ? (point.y - point2.y) : (point2.y - point.y),
                    }
                    angle += calculateAngle(normal);
                } else {
                    angle += calculateAngle(d.rotate.normal);
                }
                angle += d.rotate.relativeAngle != null ? d.rotate.relativeAngle : 0;
                transform += `rotate(${angle})`;
            }
            marker.attr('transform', transform);
        });
    }

    /**
     * Update all node positions and edge paths.
     */
    private updateGraphPositions() {
        const svg = this.getSvg();

        const graph = svg.select("g.zoom-group");
        graph.select('.nodes')
            .selectAll('g.node')
            .data(this._nodes, (d) => {return d.id;})
            .call(this.updateNodePositions.bind(this));

        graph.select('.edges')
            .selectAll('g.edge-group:not(.dragged)')
            .data(this._edges, edgeId)
            .call(this.updateEdgePositions.bind(this));

        graph.select('.edges')
            .selectAll('g.edge-group.dragged')
            .data(this.draggedEdges, edgeId)
            .call(this.updateEdgePositions.bind(this));
    }

    private createDraggedEdge(sourceNode: Node): DraggedEdge {
        const validTargets = new Set<string>();
        this._nodes.forEach(node => validTargets.add(node.id.toString()));
        this.objectCache.getEdgesBySource(sourceNode.id).forEach(edge => validTargets.delete(edge.target.toString()));
        validTargets.delete(sourceNode.id.toString())
        let draggedEdge: DraggedEdge = {
            id: sourceNode.id.toString() + Date.now().toString(),
            source: sourceNode.id,
            target: null,
            validTargets: validTargets,
            currentTarget: {x: event.x, y: event.y},
        }
        if (this.onCreateDraggedEdge != null) {
            draggedEdge = this.onCreateDraggedEdge(draggedEdge);
            if (draggedEdge == null) {
                return null;
            }
        }
        this.draggedEdges.push(draggedEdge);
        return draggedEdge;
    }

    private createDraggedEdgeFromExistingEdge(edge: Edge): DraggedEdge {
        const validTargets = new Set<string>();
        this._nodes.forEach(node => validTargets.add(node.id.toString()));
        this.objectCache.getEdgesBySource(edge.source).forEach(edgeOutgoing => {
            if (edgeId(edge) !== edgeId(edgeOutgoing)) {
                validTargets.delete(edgeOutgoing.target.toString());
            }
        });
        let draggedEdge: DraggedEdge = {
            id: edge.source.toString() + Date.now().toString(),
            createdFrom: edgeId(edge),
            source: edge.source,
            target: null,
            validTargets: validTargets,
            currentTarget: {x: event.x, y: event.y},
            markers: [],
        }
        if (edge.markers != null) {
            draggedEdge.markers = JSON.parse(JSON.stringify(edge.markers));
        }
        if (this.onCreateDraggedEdge != null) {
            draggedEdge = this.onCreateDraggedEdge(draggedEdge);
            if (draggedEdge == null) {
                return null;
            }
        }
        this.draggedEdges.push(draggedEdge);
        return draggedEdge;
    }

    private updateDraggedEdge() {
        const oldTarget = event.subject.target;
        event.subject.target = null;
        event.subject.currentTarget.x = event.x;
        event.subject.currentTarget.y = event.y;
        let possibleTarget = this.root.elementFromPoint(event.sourceEvent.clientX, event.sourceEvent.clientY);
        if (possibleTarget != null) {
            let target = select(possibleTarget);
            while (!target.empty()) {
                if (target.classed('node')) {
                    const id = target.attr('id');
                    if (event.subject.source == id) {
                        break;
                    }
                    event.subject.target = id;
                    break;
                }
                target = select(target.node().parentElement);
            }
        }
        if (event.subject.target != null) {
            if (!event.subject.validTargets.has(event.subject.target)) {
                event.subject.target = null;
            }
        }
        if (event.subject.target !== oldTarget) {
            if (this.onDraggedEdgeTargetChange != null) {
                const source = this.objectCache.getNode(event.subject.source);
                const target = event.subject.target != null ? this.objectCache.getNode(event.subject.target) : null;
                this.onDraggedEdgeTargetChange(event.subject, source, target);
            }
        }
    }

    private dropDraggedEdge() {
        let updateEdgeCache = false;
        if (event.subject.createdFrom != null) {
            const edge = this.objectCache.getEdge(event.subject.createdFrom);
            if (event.subject.target !== edge.target.toString()) {
                const index = this._edges.findIndex(edge => edgeId(edge) === event.subject.createdFrom);
                if (!this.onEdgeRemove(this._edges[index])) {
                    return;
                }
                this._edges.splice(index, 1);
                updateEdgeCache = true;
            }
        }
        const index = this.draggedEdges.findIndex(edge => edge.id === event.subject.id);
        this.draggedEdges.splice(index, 1);
        this.updateDraggedEdgeGroups();
        if (event.subject.target != null) {
            let edge = event.subject;
            delete edge.id;
            if (this.onDropDraggedEdge != null) {
                edge = this.onDropDraggedEdge(edge, this.objectCache.getNode(edge.source), this.objectCache.getNode(edge.target));
            }
            if (event.subject.createdFrom != null && event.subject.target === this.objectCache.getEdge(event.subject.createdFrom).target.toString()) {
                this.completeRender();
            } else {
                if (this.onEdgeCreate(edge)) {
                    this._edges.push(edge);
                    updateEdgeCache = true;
                }
            }
        }
        if (updateEdgeCache) {
            this.objectCache.updateEdgeCache(this._edges);
            this.completeRender();
        }
    }

    /**
     * Callback for creating edgeadd events.
     *
     * @param edge the created edge
     * @returns false if event was cancelled
     */
    private onEdgeCreate(edge: Edge): boolean {
        const ev = new CustomEvent('edgeadd', {
            bubbles: true,
            composed: true,
            cancelable: true,
            detail: {edge: edge}});
        return this.dispatchEvent(ev);
    }

    /**
     * Callback for creating edgeremove events.
     *
     * @param edge the created edge
     * @returns false if event was cancelled
     */
    private onEdgeRemove(edge: Edge) {
        const ev = new CustomEvent('edgeremove', {
            bubbles: true,
            composed: true,
            cancelable: true,
            detail: {edge: edge}});
        return this.dispatchEvent(ev);
    }

    /**
     * Callback on edges for click event.
     *
     * @param edgeDatum Corresponding datum of edge
     */
    private onEdgeClick(edgeDatum) {
        const eventDetail: any = {};
        eventDetail.sourceEvent = event;
        eventDetail.edge = edgeDatum;
        const ev = new CustomEvent('edgeclick', {bubbles: true, composed: true, cancelable: true, detail: eventDetail});
        if (!this.dispatchEvent(ev)) {
            return; // prevent default / event cancelled
        }
    }

    /**
     * Callback for creating nodeadd events.
     *
     * @param node the created node
     * @returns false if event was cancelled
     */
    private onNodeCreate(node: Node): boolean {
        const ev = new CustomEvent('nodeadd', {
            bubbles: true,
            composed: true,
            cancelable: true,
            detail: {node: node}});
        return this.dispatchEvent(ev);
    }

    /**
     * Callback for creating noderemove events.
     *
     * @param node the created node
     * @returns false if event was cancelled
     */
    private onNodeRemove(node: Node) {
        const ev = new CustomEvent('noderemove', {
            bubbles: true,
            composed: true,
            cancelable: true,
            detail: {node: node}});
        return this.dispatchEvent(ev);
    }

    /**
     * Callback for creating nodepositionchange events.
     *
     * @param nodes nodes thatchanged
     */
    private onNodePositionChange(node: Node) {
        const ev = new CustomEvent('nodepositionchange', {
            bubbles: true,
            composed: true,
            cancelable: false,
            detail: {node: node}});
        this.dispatchEvent(ev);
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
        const ev = new CustomEvent('nodeenter', {
            bubbles: true,
            composed: true,
            cancelable: false,
            detail: {sourceEvent: event, node: nodeDatum}});
        this.dispatchEvent(ev);
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
        const ev = new CustomEvent('nodeleave', {
            bubbles: true,
            composed: true,
            cancelable: false,
            detail: {sourceEvent: event, node: nodeDatum}});
        this.dispatchEvent(ev);
    }

    /**
     * Callback on nodes for click event.
     *
     * @param nodeDatum Corresponding datum of node
     */
    private onNodeClick = (nodeDatum) => {
        const eventDetail: any = {};
        eventDetail.sourceEvent = event;
        eventDetail.node = nodeDatum;
        if (event.target != null) {
            const target = select(event.target);
            const key = target.attr('data-click');
            if (key != null) {
                eventDetail.key = key;
            }
        }
        const ev = new CustomEvent('nodeclick', {bubbles: true, composed: true, cancelable: true, detail: eventDetail});
        if (!this.dispatchEvent(ev)) {
            return; // prevent default / event cancelled
        }
        if (this._mode === 'link') {
            return this.onNodeSelectLink(nodeDatum);
        }
        if (this._mode !== 'select') {
            this.setMode('select');
            this.interactionStateData.selected.add(nodeDatum.id);
            this.onSelectionChangeInternal();
        } else if (this.interactionStateData.selected.has(nodeDatum.id)) {
            this.interactionStateData.selected.delete(nodeDatum.id);
            this.onSelectionChangeInternal();
            if (this.interactionStateData.selected.size <= 0) {
                this.setMode(this.interactionStateData.fromMode);
            }
        } else {
            this.interactionStateData.selected.add(nodeDatum.id);
            this.onSelectionChangeInternal();
        }
        this.updateNodeHighligts();
        this.updateEdgeHighligts();
    }

    private onSelectionChangeInternal() {
        let selected: Set<number|string> = new Set();
        if (this.mode === 'select') {
            selected = this.interactionStateData.selected;
        }
        const ev = new CustomEvent('selection', {bubbles: true, composed: true, detail: {selection: selected}});
        this.dispatchEvent(ev);
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
            if (!this.onEdgeRemove(this._edges[oldEdge])) {
                return; // event cancelled
            }
            this._edges.splice(oldEdge, 1);
        } else {
            const newEdge: Edge = {
                source: this.interactionStateData.source,
                target: this.interactionStateData.target,
            };
            if (!this.onEdgeCreate(newEdge)) {
                return; // event cancelled
            }
            this._edges.push(newEdge);
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
                .selectAll('g.edge-group:not(.dragged)')
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
