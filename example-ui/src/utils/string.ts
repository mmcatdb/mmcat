export class IntendedStringBuilder {
    private intendationString: string;
    private lines: string[] = [];

    public constructor(input?: string | number) {
        this.intendationString = input === undefined ? '' : typeof input === 'string' ? input : IntendedStringBuilder.getTabIntendationString(input);
    }

    public static getTabIntendationString(depth: number): string {
        return [ ...Array(depth) ].map(item => '    ').join();
    }

    public appendIntended(string: string): IntendedStringBuilder {
        return this.append(string, true);
    }

    public append(string: string, intended = false): IntendedStringBuilder {
        if (string === '')
            return this;

        if (this.lines.length === 0)
            this.lines.push(intended ? this.intendationString : '');

        this.lines[this.lines.length - 1] += string;

        return this;
    }

    public appendIntendedLine(line: string = ''): IntendedStringBuilder {
        return this.appendLine(line, true);
    }

    public appendLine(line: string = '', intended = false): IntendedStringBuilder {
        this.lines.push((intended ? this.intendationString : '') + line);

        return this;
    }

    public toString(): string {
        return this.lines.join('\n');
    }
}
