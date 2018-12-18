import {select, scaleLinear, zoom, zoomIdentity, zoomTransform, event, line, curveStep, drag} from "d3";


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
        console.log('"' + overflow + '"')
    }
    // TODO wrap multiline textbox
    //console.log(x, y, width, height, newText)
    //console.log(text.node().getBBox());
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
