import { calculateDefaultNodeColors } from '@neo4j-devtools/word-color';
import { type RelationshipModel } from './Relationship';
import { type NodeModel } from './Node';

export const NODE_CLASS = 'svg-node';
export const RELATIONSHIP_CLASS = 'svg-relationship';

export class Selector {
    tag = '';
    classes: string[] = [];
    constructor(tag: string, classes: null | string[]) {
        this.tag = tag;
        this.classes = classes ?? [];
    }

    toString = (): string => {
        return selectorArrayToString([ this.tag ].concat(this.classes));
    };
}

class StyleElement {
    selector: Selector;
    props: Record<string, string>;
    constructor(selector: Selector) {
        this.selector = selector;
        this.props = {};
    }

    applyRules = (rules: StyleRule[]) => {
        for (const rule of rules) {
            if (rule.matches(this.selector)) {
                this.props = { ...this.props, ...rule.props };
                this.props.caption = this.props.caption || this.props.defaultCaption;
            }
        }
        return this;
    };

    get = (attr: string) => {
        return this.props[attr] || '';
    };
}

class StyleRule {
    selector: Selector;
    props: Record<string, string>;
    constructor(selector1: Selector, props1: Record<string, string>) {
        this.selector = selector1;
        this.props = props1;
    }

    matches = (selector: Selector) => {
        if (this.selector.tag !== selector.tag)
            return false;

        for (const selectorClass of this.selector.classes) {
            if (selectorClass != null && selector.classes.indexOf(selectorClass) === -1)
                return false;
        }
        return true;
    };

    matchesExact = (selector: Selector) => {
        return (
            this.matches(selector) &&
      this.selector.classes.length === selector.classes.length
        );
    };
}

const DEFAULT_STYLE: Record<string, Record<string, string>> = {
    [RELATIONSHIP_CLASS]: {
        'color': '#A5ABB6',
        'shaft-width': '1px',
        'padding': '3px',
        'text-color-internal': '#FFFFFF',
        'caption': '<type>',
    },
};

type DefaultArrayWidthType = { 'shaft-width': string };

const DEFAULT_ARRAY_WIDTHS: DefaultArrayWidthType[] = [ {
    'shaft-width': '1px',
}, {
    'shaft-width': '2px',
}, {
    'shaft-width': '3px',
}, {
    'shaft-width': '5px',
}, {
    'shaft-width': '8px',
}, {
    'shaft-width': '13px',
}, {
    'shaft-width': '25px',
}, {
    'shaft-width': '38px',
} ];

type DefaultColorType = {
    'color': string;
    'border-color': string;
    'text-color-internal': string;
};

const DEFAULT_COLORS: DefaultColorType[] = [ {
    'color': '#604A0E',
    'border-color': '#423204',
    'text-color-internal': '#FFFFFF',
}, {
    'color': '#C990C0',
    'border-color': '#b261a5',
    'text-color-internal': '#FFFFFF',
}, {
    'color': '#F79767',
    'border-color': '#f36924',
    'text-color-internal': '#FFFFFF',
}, {
    'color': '#57C7E3',
    'border-color': '#23b3d7',
    'text-color-internal': '#2A2C34',
}, {
    'color': '#F16667',
    'border-color': '#eb2728',
    'text-color-internal': '#FFFFFF',
}, {
    'color': '#D9C8AE',
    'border-color': '#c0a378',
    'text-color-internal': '#2A2C34',
}, {
    'color': '#8DCC93',
    'border-color': '#5db665',
    'text-color-internal': '#2A2C34',
}, {
    'color': '#ECB5C9',
    'border-color': '#da7298',
    'text-color-internal': '#2A2C34',
}, {
    'color': '#4C8EDA',
    'border-color': '#2870c2',
    'text-color-internal': '#FFFFFF',
}, {
    'color': '#FFC454',
    'border-color': '#d7a013',
    'text-color-internal': '#2A2C34',
}, {
    'color': '#DA7194',
    'border-color': '#cc3c6c',
    'text-color-internal': '#FFFFFF',
}, {
    'color': '#569480',
    'border-color': '#447666',
    'text-color-internal': '#FFFFFF',
} ];

export class GraphStyleModel {
    rules: StyleRule[];

    constructor(private useGeneratedDefaultColors = false) {
        this.rules = [];
        try {
            this.loadRules();
        }
        catch {
            // TODO error
        }
    }

    parseSelector(key: string): Selector {
        const tokens = selectorStringToArray(key);
        return new Selector(tokens[0], tokens.slice(1));
    }

    nodeSelector(node: { labels: null | string[] } = { labels: null }): Selector {
        const classes = node.labels ?? [];
        return new Selector(NODE_CLASS, classes);
    }

    relationshipSelector(rel: { type: null | string } = { type: null }): Selector {
        const classes = rel.type != null ? [ rel.type ] : [];
        return new Selector(RELATIONSHIP_CLASS, classes);
    }

    findRule(selector: Selector, rules: StyleRule[]): StyleRule | undefined {
        for (const rule of rules) {
            if (rule.matchesExact(selector))
                return rule;
        }
        return undefined;
    }

    findAvailableDefaultColor(rules: StyleRule[]): DefaultColorType {
        const usedColors = rules
            .filter((rule: StyleRule) => {
                return rule.props.color != null;
            })
            .map((rule: StyleRule) => {
                return rule.props.color;
            });

        // @ts-expect-error ts-migrate(2365) FIXME: Operator '>' cannot be applied to types 'number' a... Remove this comment to see the full error message
        const index = usedColors.length - 1 > DEFAULT_COLORS ? 0 : usedColors.length - 1;
        return DEFAULT_COLORS[index];
    }

    getDefaultNodeCaption(item: NodeModel | { labels: string[] }): { caption: string } | { defaultCaption: string } {
        if (!('propertyList' in item) || !item.propertyList?.length) {
            return {
                defaultCaption: '<id>',
            };
        }

        const captionPrioOrder = [
            /^name$/i,
            /^title$/i,
            /^label$/i,
            /name$/i,
            /description$/i,
            /^.+/,
        ];

        const defaultCaption = captionPrioOrder.reduceRight((leading, current) => {
            const hits = ('propertyList' in item ? item.propertyList : []).filter(prop => current.test(prop.key));
            if (hits.length)
                return `{${hits[0].key}}`;
            else
                return leading;

        }, '');

        return {
            caption: defaultCaption ||'<id>',
        };
    }

    calculateStyle(selector: Selector): StyleElement {
        return new StyleElement(selector).applyRules(this.rules);
    }

    setDefaultNodeStyle(selector: Selector, item: NodeModel | { labels: string[] }): void {
        let defaultColor = true;
        let defaultCaption = true;
        for (const rule of this.rules) {
            if (rule.selector.classes.length > 0 && rule.matches(selector)) {
                if ('color' in rule.props)
                    defaultColor = false;

                if ('caption' in rule.props)
                    defaultCaption = false;

            }
        }
        const minimalSelector = new Selector(
            selector.tag,
            selector.classes.sort().slice(0, 1),
        );
        if (defaultColor) {
            const calcColor = (label: Selector): DefaultColorType => {
                const { backgroundColor, borderColor, textColor } =
                    calculateDefaultNodeColors(label.classes[0]);

                return {
                    'border-color': borderColor,
                    'text-color-internal': textColor,
                    color: backgroundColor,
                };
            };

            this.changeForSelector(
                minimalSelector,
                this.useGeneratedDefaultColors
                    ? calcColor(minimalSelector)
                    : this.findAvailableDefaultColor(this.rules),
            );
        }
        if (defaultCaption)
            this.changeForSelector(minimalSelector, this.getDefaultNodeCaption(item));
    }

    changeForSelector(selector: Selector, props: Record<string, string>): StyleRule {
        let rule = this.findRule(selector, this.rules);
        if (rule == null) {
            rule = new StyleRule(selector, props);
            this.rules.push(rule);
        }
        rule.props = { ...rule.props, ...props };
        return rule;
    }

    destroyRule(rule: StyleRule): void {
        const idx = this.rules.indexOf(rule);
        if (idx != null)
            this.rules.splice(idx, 1);

    }

    importGrass(string: string): void {
        try {
            const rules = this.parse(string);
            this.loadRules(rules);
        }
        catch {
            // TODO error
        }
    }

    parse(string: string) {
        const chars = string.split('');
        let insideString = false;
        let insideProps = false;
        let keyword = '';
        let props = '';
        const rawRules: Record<string, string> = {};

        for (const c of chars) {
            let skipThis = true;
            switch (c) {
            case '{':
                if (!insideString)
                    insideProps = true;
                else
                    skipThis = false;

                break;
            case '}':
                if (!insideString) {
                    insideProps = false;
                    rawRules[keyword] = props;
                    keyword = '';
                    props = '';
                }
                else {
                    skipThis = false;
                }
                break;
            case '\'':
                // @ts-expect-error ts-migrate(2447) FIXME: The '^=' operator is not allowed for boolean types... Remove this comment to see the full error message
                insideString ^= true;
                break;
            default:
                skipThis = false;
            }
            if (skipThis)
                continue;

            if (insideProps) {
                props += c;
            }
            else {
                if (!c.match(/[\s\n]/))
                    keyword += c;
            }
        }

        const rules: Record<string, Record<string, string>> = {};

        for (const k in rawRules) {
            const v = rawRules[k];
            rules[k] = {};
            v.split(';').forEach(prop => {
                const [ key, val ] = prop.split(':');
                if (key && val)
                    rules[k][key.trim()] = val.trim();
            });
        }
        return rules;
    }

    resetToDefault = (): void => {
        this.loadRules();
    };

    toSheet = () => {
        const sheet: Record<string, Record<string, string>> = {};
        this.rules.forEach((rule: StyleRule) => {
            sheet[rule.selector.toString()] = rule.props;
        });
        return sheet;
    };

    toString = (): string => {
        let str = '';
        this.rules.forEach((r: StyleRule) => {
            str += `${r.selector.toString()} {\n`;
            for (const k in r.props) {
                let v = r.props[k];
                if (k === 'caption')
                    v = `'${v}'`;

                str += `  ${k}: ${v};\n`;
            }
            str += '}\n\n';
        });
        return str;
    };

    loadRules = (data?: Record<string, Record<string, string>>): void => {
        const localData = typeof data === 'object' ? data : DEFAULT_STYLE;
        this.rules = [];
        for (const key in localData) {
            const props = localData[key];
            this.rules.push(new StyleRule(this.parseSelector(key), props));
        }
    };

    defaultArrayWidths(): DefaultArrayWidthType[] {
        return DEFAULT_ARRAY_WIDTHS;
    }

    defaultColors(): DefaultColorType[] {
        return DEFAULT_COLORS;
    }

    interpolate = (str: string, item: NodeModel | RelationshipModel) => {
        let ips = str.replace(/\{([^{}]*)\}/g, (_: unknown, b: string) => {
            const r = item.propertyMap[b];
            if (typeof r === 'object')
                throw new Error('FIXME Cannot interpolate object');
                // return r.join(', ');
            if (typeof r === 'number')
                throw new Error('FIXME Cannot interpolate number');

            if (typeof r === 'string')
                return r;

            return '';
        });
        if (ips.length < 1 && str === '{type}' && item.isRelationship)
            ips = '<type>';

        if (ips.length < 1 && str === '{id}' && item.isNode)
            ips = '<id>';

        return ips.replace(/^<(id|type)>$/, (_: unknown, b: 'id' | 'type') => {
            const r = (item as unknown as Record<string, unknown>)[b];
            if (typeof r === 'number')
                throw new Error('FIXME Cannot interpolate number');

            if (typeof r === 'string')
                return r;

            return '';
        });
    };

    forNode = (node: NodeModel | { labels: string[] }): StyleElement => {
        const selector = this.nodeSelector(node);
        if ((node.labels != null ? node.labels.length : 0) > 0)
            this.setDefaultNodeStyle(selector, node);

        return this.calculateStyle(selector);
    };

    forRelationship = (rel: RelationshipModel | { type: string }): StyleElement => {
        const selector = this.relationshipSelector(rel);
        return this.calculateStyle(selector);
    };
}

const selectorStringToArray = (selector: string) => {
    // Negative lookbehind simulation since js support is very limited.
    // We want to match all . that are not preceded by \\
    // Instead we reverse and look
    // for . that are not followed by \\ (negative lookahead)
    const reverseSelector = selector.split('').reverse().join('');
    const re = /(.+?)(?!\.\\)(?:\.|$)/g;
    const out = [];
    let m;
    while ((m = re.exec(reverseSelector)) !== null) {
        const res = m[1].split('').reverse().join('');
        out.push(res);
    }

    return out
        .filter(r => r)
        .reverse()
        .map(r => r.replace(/\\./g, '.'));
};

const selectorArrayToString = (selectors: string[]) => {
    const escaped = selectors.map((r: string) => r.replace(/\./g, '\\.'));
    return escaped.join('.');
};
