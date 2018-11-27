import {select, scaleLinear, zoom, zoomIdentity, zoomTransform, event} from "d3";


export default class GraphEditor extends HTMLElement {

    private mutationObserver: MutationObserver;

    private initialized: boolean;
    private root: ShadowRoot;
    private xScale: scaleLinear;
    private yScale: scaleLinear;
    private zoom: zoom;

    private contentMinHeight = 0;
    private contentMaxHeight = 1;
    private contentMinWidth = 0;
    private contentMaxWidth = 1;

    private isInteractive: boolean = true;
    private _nodes: any[];
    private _edges: any[];

    constructor() {
        super();
        this._nodes = [];
        this._edges = [];
        this.initialized = false;

        this.mutationObserver = new MutationObserver(this.completeRender.bind(this));
    }

    static get observedAttributes() { return ['nodes', 'edges']; }

    attributeChangedCallback(name, oldValue, newValue: string) {
      if (name === 'nodes') {
          newValue = newValue.replace("'", '"');
          console.log(newValue);
          this._nodes =[{'id': 1, 'title': 'hello world'}]; // JSON.parse(newValue);
      }
      if (name === 'edges') {
          this._edges = JSON.parse(newValue);
      }
      this.completeRender();
      this.updateLayout();
    }

    connectedCallback() {
        if (! this.isConnected) {
            return;
        }
        this.initialize();
        this.completeRender();
        this.updateLayout();

        this.mutationObserver.observe(this, {
            childList: true,
            characterData: true,
            subtree: true
        });
    }

    initialize() {
        if (!this.initialized) {
            this.initialized = true;
            this.root = this.attachShadow({mode: 'open'});
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

            svg.call(this.zoom);

            if (!this.isInteractive) {
                svg.on('.zoom', null);
            }


            graph.append('g')
                .attr('class', 'edges')
                .attr('filter', 'url(#shadow-edge)');
            //  .append('circle')
            //    .attr('cx', 0)
            //    .attr('cy', 0)
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

    private updateLayout() {
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

    completeRender() {
        if (! this.initialized || ! this.isConnected) {
            return;
        }
        const svg = this.getSvg();

        this.updateSize();

        const graph = svg.select("g.zoom-group");

        // update nodes ////////////////////////////////////////////////////////
        const nodeSelection = graph.select(".nodes")
            .selectAll("g.node")
            .data(this._nodes, (d) => {return d.id;});

        nodeSelection.exit().remove();

        nodeSelection.enter().append("g")
            .attr("class", "node")
            .call(this.createNodes.bind(this))
          .merge(nodeSelection)
            .call(this.updateNodes.bind(this))
    }


    private createNodes(nodeSelection) {
        nodeSelection.append('circle');
    }


    private updateNodes(nodeSelection) {
        nodeSelection.select('circle')
            .attr('fill', 'black')
            .attr('cx', (d) => {d.x != null ? d.x : 0})
            .attr('cy', (d) => {d.y != null ? d.y : 0})
            .attr('r', 5);
    }
}