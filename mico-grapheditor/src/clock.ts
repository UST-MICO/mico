import {select, timeParse} from "d3";

const TIME_PARSER = timeParse('%H:%M');

export default class Clock extends HTMLElement {
    _time;
    _initialized;
    _shadowRoot;
    _observer;

    constructor() {
        super();
        this._time = null;
        this._initialized = false;
        this._shadowRoot = this.attachShadow({mode: 'open'});

        this._observer = new MutationObserver(this._updateTemplates.bind(this));
    }

    static get observedAttributes() { return ["time"]; }

    attributeChangedCallback(name, oldValue, newValue) {
      // name will always be "time" due to observedAttributes
      this._time = newValue;
      this._updateRendering();
    }

    connectedCallback() {
      this._updateRendering();

      this._observer.observe(this, {
        childList: true,
        characterData: true,
        subtree: true
      });
    }

    get time() {
      return this._time;
    }

    set time(v) {
        this.setAttribute("time", v);
    }

    _updateRendering() {
        if (!this._initialized) {
            this._initialized = true;
            const svg = select(this._shadowRoot).append('svg')
                .attr('width', 100)
                .attr('height', 100);
            svg.append('circle')
                .attr('cy', 50)
                .attr('cx', 50)
                .attr('r', 40)
                .attr('stroke', 'black')
                .attr('stroke-width', 3)
                .attr('fill', 'none');
            svg.append('line')
                .attr('x1', 50)
                .attr('y1', 50)
                .attr('x2', 50)
                .attr('y2', 15)
                .attr('stroke', 'black')
                .attr('stroke-width', 2)
                .classed('minute', true);
            svg.append('line')
                .attr('x1', 50)
                .attr('y1', 50)
                .attr('x2', 50)
                .attr('y2', 25)
                .attr('stroke', 'black')
                .attr('stroke-width', 3)
                .classed('hour', true);
        }
        if (this.time != null) {
            select(this._shadowRoot).selectAll('h1').text((d, i, nodes) => {
                if (nodes[i].hasAttribute('data-content')) {
                    return  this.time + ' – ' + nodes[i].getAttribute('data-content');
                }
                return 'HI';
            });
            const time = TIME_PARSER(this.time);
            let minuteAngle = 360 * (time.getMinutes()/60);
            select(this._shadowRoot).select('.minute')
                .attr('transform', 'rotate(' + minuteAngle + ',50,50)');
            const hourAngle = 360 * ((time.getHours() % 12)/12);
            select(this._shadowRoot).select('.hour')
                .attr('transform', 'rotate(' + hourAngle + ',50,50)');
        }
    }

    _updateTemplates() {
        if (this.children.length > 0) {
            const clone = document.importNode((this.children[0] as any).content, true);
            this._shadowRoot.append(clone);
            this._updateRendering();
        }
    }
}