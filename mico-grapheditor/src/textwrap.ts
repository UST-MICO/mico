import {select, scaleLinear, zoom, zoomIdentity, zoomTransform, event, line, curveStep, drag} from "d3";

/**
 * Wrap text in an svg text element.
 *
 * Only wraps text if a 'width' or 'data-width' attribute is
 * present on text element.
 *
 * For multiline wrapping an additional 'height' or 'data-height'
 * attribute is neccessary.
 *
 * Partly uses css attributes 'text-overflow' and 'word-break'
 * to determine how to wrap text.
 *
 * @param element element to wrap text into
 * @param newText text to wrap
 */
export function wrapText(element: SVGTextElement, newText) {
    const text = select(element);
    const x = parseFloat(text.attr('x'));
    const y = parseFloat(text.attr('y'));
    let width = parseFloat(text.attr('width'));
    if (isNaN(width)) {
        width = parseFloat(text.attr('data-width'));
    }
    let height = parseFloat(text.attr('height'));
    if (isNaN(height)) {
        height = parseFloat(text.attr('data-height'));
    }
    if (isNaN(width)) {
        text.text(newText);
        return;
    }

    // get overflowMode from css style attribute
    let overflowMode = text.style('text-overflow');
    if (overflowMode == null) {
        overflowMode = 'ellipsis';
    }

    // get wordBreak from css style attribute
    let wordBreak = text.style('word-break');
    if (wordBreak == null) {
        wordBreak = 'break-word';
    }


    if (isNaN(height)) {
        const overflow = lTrim(wrapSingleLine(element, width, newText, overflowMode, wordBreak));
        text.attr('data-wrapped', overflow !== '' ? 'true' : 'false');
        return;
    }

    // wrap multiline
    const spanSelection = calculateMultiline(text, height, x, y);
    const lines = spanSelection.nodes();
    for (let index = 0; index < lines.length; index++) {
        const line = lines[index];
        const last = index < (lines.length-1);
        newText = lTrim(wrapSingleLine(line, width, newText, last ? 'clip' : overflowMode, last ? wordBreak : 'break-all'))
    }
}

/**
 * Trim trailing whitespace
 *
 * @param text to trim
 */
function rTrim(text: string) {
    return text.replace(/\s+$/, '');
}

/**
 * Trim leading whitespace
 *
 * @param text to trim
 */
function lTrim(text: string) {
    return text.replace(/^\s+/, '');
}

/**
 * Calculate and create a multiline span group.
 *
 * @param text parent text element
 * @param height max height
 * @param x
 * @param y
 * @param linespacing 'auto' or number (default: 'auto')
 */
function calculateMultiline(text, height, x, y, linespacing: string='auto') {
    let lineheight = parseFloat(text.attr('data-lineheight'));
    if (isNaN(lineheight)) {
        lineheight = parseFloat(text.style('line-height'));
        if (isNaN(lineheight)) {
            text.text('M'); // use M as measurement character.
            lineheight = text.node().getBBox().height;
            text.text(null);
        }
        text.attr('data-lineheight', lineheight);
    }
    const lines: number[] = [];
    if (linespacing === 'auto') {
        // ideal linespacing => max number of lines, equal distance, last line at y+height
        let nrOfLines = Math.floor(height / lineheight);
        if (nrOfLines <= 0) {
            nrOfLines = 1;
        } else {
            lineheight = height / nrOfLines;
        }
        linespacing = '1';
    }
    let currentY = 0;
    let factor = parseFloat(linespacing);
    if (isNaN(factor)) {
        factor = 1;
    }
    while (currentY < height) {
        lines.push(y + currentY);
        currentY += lineheight * factor;
    }

    const spanSelection = text.selectAll('tspan').data(lines);
    spanSelection.exit().remove();
    spanSelection.enter().append('tspan').attr('x', x).attr('y', d => d);
    return spanSelection;
}

/**
 * Wrap text in a single line and return the overflow.
 *
 * @param element element to wraptext into
 * @param width max linewidth for text
 * @param newText
 * @param mode wrapping mode
 * @param wordBreak break mode
 */
function wrapSingleLine(element: SVGTextElement|SVGTSpanElement, width: number,
                        newText: string, mode: string = 'ellipsis',
                        wordBreak: string = 'break-word'): string {

    const text = select(element);
    const oldText = text.text();

    // Allow manual linewraps with newline
    let suffix = '';

    if (newText.includes('\n')) {
        const index = newText.indexOf('\n');
        suffix = newText.substr(index);
        newText = newText.substring(0, index);
    }

    // shortcuts (when text is already wrapped)
    if (oldText != null && oldText !== '') {
        if (oldText.startsWith(newText)) {
            // newText is shorter
            text.text(newText);
            return '' + suffix;
        }
        if (mode === 'clip') {
            if (text.attr('data-wrapped') === 'true' && newText.startsWith(oldText)) {
                // odlText was wrapped and newText begins with oldText
                return newText.substr(oldText.length) + suffix;
            }
        } else {
            if (newText.endsWith('…')) {
                // oldText was wrapped (starts with '…')
                if (newText.startsWith(oldText.substr(0, oldText.length - 1))) {
                    // newText begins with oldText
                    return newText.substr(oldText.length - 1) + suffix;
                }
            }
        }
    }

    // Try naive without wrapping
    text.text(newText);
    if (text.node().getBBox().width <= width) {
        return '' + suffix;
    }

    if (wordBreak === 'break-all' || newText.indexOf(' ') < 0) {
        return wrapCharacters(newText, text, width, mode === 'clip' ? '' : '…') + suffix;
    } else {
        return wrapWords(newText, text, width, mode === 'clip' ? '' : '…') + suffix;
    }
}

/**
 * Wrap single line, can break after every character.
 *
 * @param newText
 * @param text d3 selection of element to wrap text into
 * @param width width of the  line
 * @param overflowChar wrapping mode
 */
function wrapCharacters(newText: string, text: any, width: number, overflowChar: string) {
    let divider = newText.length;
    let lastText = newText;
    let step = newText.length;
    let counter = 0;
    while (step > 1 && counter < 1000) {
        counter++;
        step = Math.ceil(step / 2);
        if (text.node().getBBox().width > width) {
            divider -= step;
        }
        else {
            divider += step;
        }
        text.text(rTrim(newText.substr(0, divider)) + overflowChar);
    }
    if (text.node().getBBox().width > width) {
        divider -= step;
        text.text(rTrim(newText.substr(0, divider)) + overflowChar);
    }
    return newText.substr(divider);
}


/**
 * Wrap single line, can break at spaces only.
 *
 * @param newText
 * @param text d3 selection of element to wrap text into
 * @param width width of the  line
 * @param overflowChar wrapping mode
 */
function wrapWords(newText: string, text: any, width: number, overflowChar: string) {
    let lastWhitespace = -1;
    let nextWhitespace = newText.indexOf(' ');
    while (nextWhitespace < newText.length && !(lastWhitespace < 0 && nextWhitespace < 0)) {
        // while(not(reached end of string) && not(no space in string))
        text.text(rTrim(newText.substr(0, nextWhitespace)) + overflowChar);
        if (text.node().getBBox().width > width) {
            // last word was too much
            break;
        }
        lastWhitespace = nextWhitespace;
        if (lastWhitespace + 1 >= newText.length) {
            // reached end of text
            nextWhitespace = newText.length;
        } else {
            // calculate next space position
            nextWhitespace = newText.indexOf(' ', (lastWhitespace < 0) ? 0 : lastWhitespace + 1);
        }
    }
    if (lastWhitespace < 0) {
        // one long word
        return wrapCharacters(newText, text, width, '-');
    }
    text.text(rTrim(newText.substr(0, lastWhitespace)) + overflowChar);
    return newText.substr(lastWhitespace);
}
