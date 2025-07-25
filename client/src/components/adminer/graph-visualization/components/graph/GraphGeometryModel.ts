import { PairwiseArcsRelationshipRouting } from './PairwiseArcsRelationshipRouting';
import { measureText } from '@/components/adminer/graph-visualization/utils/textMeasurement';
import { type GraphModel } from '@/components/adminer/graph-visualization/types/Graph';
import { type GraphStyleModel } from '@/components/adminer/graph-visualization/types/GraphStyle';
import { type NodeCaptionLine, type NodeModel } from '@/components/adminer/graph-visualization/types/Node';
import { type RelationshipModel } from '@/components/adminer/graph-visualization/types/Relationship';
import { NODE_CAPTION_FONT_SIZE_PX } from './renderers';
import { NODE_RADIUS } from '../../utils/constants';

export class GraphGeometryModel {
    relationshipRouting: PairwiseArcsRelationshipRouting;
    style: GraphStyleModel;
    canvas: HTMLCanvasElement;
    constructor(style: GraphStyleModel) {
        this.style = style;
        this.relationshipRouting = new PairwiseArcsRelationshipRouting(this.style);
        this.canvas = document.createElement('canvas');
    }

    formatNodeCaptions(nodes: NodeModel[]): void {
        const canvas2DContext = this.canvas.getContext('2d');
        if (canvas2DContext) {
            nodes.forEach(node => {
                node.caption = fitCaptionIntoCircle(node, this.style, canvas2DContext);
            });
        }
    }

    formatRelationshipCaptions(relationships: RelationshipModel[]): void {
        relationships.forEach(relationship => {
            const template = this.style.forRelationship(relationship).get('caption');
            relationship.caption = this.style.interpolate(template, relationship);
        });
    }

    onGraphChange(
        graph: GraphModel,
        options = { updateNodes: true, updateRelationships: true },
    ): void {
        if (options.updateNodes)
            this.formatNodeCaptions(graph.nodes());

        if (options.updateRelationships) {
            this.formatRelationshipCaptions(graph.relationships());
            this.relationshipRouting.measureRelationshipCaptions(
                graph.relationships(),
            );
        }
    }

    onTick(graph: GraphModel): void {
        this.relationshipRouting.layoutRelationships(graph);
    }
}

const fitCaptionIntoCircle = (
    node: NodeModel,
    style: GraphStyleModel,
    canvas2DContext: CanvasRenderingContext2D,
): NodeCaptionLine[] => {
    const fontFamily = 'sans-serif';
    const fontSize = NODE_CAPTION_FONT_SIZE_PX;
    // Roughly calculate max text length the circle can fit by radius and font size
    const maxCaptionTextLength = Math.floor((Math.pow(NODE_RADIUS, 2) * Math.PI) / Math.pow(fontSize, 2));
    const template = style.forNode(node).get('caption');
    const nodeText = style.interpolate(template, node);
    const captionText = nodeText.length > maxCaptionTextLength
        ? nodeText.substring(0, maxCaptionTextLength)
        : nodeText;
    const measure = (text: string) => measureText(text, fontFamily, fontSize, canvas2DContext);
    const whiteSpaceMeasureWidth = measure(' ');

    const words = captionText.split(' ');

    const emptyLine = (lineCount: number, lineIndex: number): NodeCaptionLine => {
        // Calculate baseline of the text
        const baseline = (1 + lineIndex - lineCount / 2) * fontSize;

        // The furthest distance between chord (top or bottom of the line) and circle centre
        const chordCentreDistance = lineIndex < lineCount / 2
            ? baseline - fontSize / 2
            : baseline + fontSize / 2;
        const maxLineWidth = Math.sqrt(Math.pow(NODE_RADIUS, 2) - Math.pow(chordCentreDistance, 2)) * 2;
        return {
            node,
            text: '',
            baseline,
            remainingWidth: maxLineWidth,
        };
    };

    const addShortenedNextWord = (
        line: NodeCaptionLine,
        word: string,
    ): string => {
        while (word.length > 2) {
            const newWord = `${word.substring(0, word.length - 2)}\u2026`;
            if (measure(newWord) < line.remainingWidth)
                return `${line.text.split(' ').slice(0, -1).join(' ')} ${newWord}`;

            word = word.substring(0, word.length - 1);
        }
        return `${word}\u2026`;
    };

    function fitOnFixedNumberOfLines(lineCount: number): [ NodeCaptionLine[], number ] {
        const lines = [];
        const wordMeasureWidthList: number[] = words.map((word: string) =>
            measure(`${word}`),
        );
        let wordIndex = 0;
        for (let lineIndex = 0; lineIndex < lineCount; lineIndex++) {
            const line = emptyLine(lineCount, lineIndex);
            while (wordIndex < words.length && wordMeasureWidthList[wordIndex] < line.remainingWidth - whiteSpaceMeasureWidth) {
                line.text = `${line.text} ${words[wordIndex]}`;
                line.remainingWidth -= wordMeasureWidthList[wordIndex] + whiteSpaceMeasureWidth;
                wordIndex++;
            }
            lines.push(line);
        }
        if (wordIndex < words.length) {
            lines[lineCount - 1].text = addShortenedNextWord(
                lines[lineCount - 1],
                words[wordIndex],
            );
        }
        return [ lines, wordIndex ];
    }

    let consumedWords = 0;
    const maxLines = (NODE_RADIUS * 2) / fontSize;

    let lines = [ emptyLine(1, 0) ];
    // Typesetting for finding suitable lines to fit words
    for (let lineCount = 1; lineCount <= maxLines; lineCount++) {
        const [ candidateLines, candidateWords ] = fitOnFixedNumberOfLines(lineCount);

        // If the lines don't have empty line(s), they're probably good fit for the typesetting
        if (!candidateLines.some((line: NodeCaptionLine) => !line.text)) {
            lines = candidateLines;
            consumedWords = candidateWords;
        }
        if (consumedWords >= words.length)
            return lines;
    }
    return lines;
};
