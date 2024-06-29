export class IndentedStringBuilder {
    private intendationString: string;
    private lines: string[] = [];

    public constructor(input?: string | number) {
        this.intendationString = input === undefined ? '' : typeof input === 'string' ? input : IndentedStringBuilder.getTabIntendationString(input);
    }

    public static getTabIntendationString(depth: number): string {
        return [ ...Array(depth) ].map(() => '    ').join();
    }

    public appendIntended(string: string): IndentedStringBuilder {
        return this.append(string, true);
    }

    public append(string: string, intended = false): IndentedStringBuilder {
        if (string === '')
            return this;

        if (this.lines.length === 0)
            this.lines.push(intended ? this.intendationString : '');

        this.lines[this.lines.length - 1] += string;

        return this;
    }

    public appendIntendedLine(line = ''): IndentedStringBuilder {
        return this.appendLine(line, true);
    }

    public appendLine(line = '', intended = false): IndentedStringBuilder {
        this.lines.push((intended ? this.intendationString : '') + line);

        return this;
    }

    public toString(): string {
        return this.lines.join('\n');
    }
}
