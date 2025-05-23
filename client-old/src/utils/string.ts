export class IndentedStringBuilder {
    private indentationString: string;
    private lines: string[] = [];

    public constructor(input?: string | number) {
        this.indentationString = input === undefined ? '' : typeof input === 'string' ? input : IndentedStringBuilder.getTabIndentationString(input);
    }

    public static getTabIndentationString(depth: number): string {
        return [ ...Array(depth) ].map(() => '    ').join();
    }

    public appendIndented(string: string): IndentedStringBuilder {
        return this.append(string, true);
    }

    public append(string: string, indented = false): IndentedStringBuilder {
        if (string === '')
            return this;

        if (this.lines.length === 0)
            this.lines.push(indented ? this.indentationString : '');

        this.lines[this.lines.length - 1] += string;

        return this;
    }

    public appendIndentedLine(line = ''): IndentedStringBuilder {
        return this.appendLine(line, true);
    }

    public appendLine(line = '', indented = false): IndentedStringBuilder {
        this.lines.push((indented ? this.indentationString : '') + line);

        return this;
    }

    public toString(): string {
        return this.lines.join('\n');
    }
}
