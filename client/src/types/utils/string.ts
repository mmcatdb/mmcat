export type Printable = {
    printTo(printer: Printer): void;
};

export type Printer = {
    down(): Printer;
    up(): Printer;
    nextLine(): Printer;

    append(printable: Printable): Printer;
    append(string: string): Printer;
    append(number: number): Printer;
    append(object: object): Printer;

    remove(index?: number): Printer;
};

/**
 * Utility method for providing a default printer.
 */
export function createPrinter() {
    return new LineStringBuilder(0);
}

/**
 * Utility method for printing a printable to a string. Should be used in the toString method of the printable.
 */
export function print(printable: Printable): string {
    const printer = createPrinter();
    printable.printTo(printer);
    return printer.toString();
}

class LineStringBuilder implements Printer {
    private readonly stack: string[] = [];

    constructor(
        private indentationLevel: number,
        private readonly indentationStringPerLevel = '    ',
    ) {}

    down(): LineStringBuilder {
        this.indentationLevel++;
        return this;
    }

    up(): LineStringBuilder {
        this.indentationLevel--;
        return this;
    }

    nextLine(): LineStringBuilder {
        const indentation = this.indentationStringPerLevel.repeat(this.indentationLevel);
        this.stack.push('\n' + indentation);
        return this;
    }

    append(value: Printable | string | number | object): LineStringBuilder {
        if (typeof value === 'string') {
            this.stack.push(value);
            return this;
        }

        if (typeof value === 'number') {
            this.stack.push(value.toString());
            return this;
        }

        if ('printTo' in value) {
            const originalLevel = this.indentationLevel;
            value.printTo(this);
            this.indentationLevel = originalLevel;
            return this;
        }

        if ('toString' in value) {
            // eslint-disable-next-line
            this.stack.push(value.toString());
            return this;
        }

        this.stack.push('' + value);
        return this;
    }

    remove(index?: number): LineStringBuilder {
        if (index === undefined) {
            this.stack.pop();
            return this;
        }

        while (index > 0) {
            this.stack.pop();
            index--;
        }

        return this;
    }

    toString(): string {
        // We don't do no trimming here, because we don't need it (we are not comparing strings for equality in tests on FE).
        return this.stack.join('');
    }
}

/** Don't be afraid to add others as needed. */
export enum Casing {
    camel = 'camelCase',
    snake = 'snake_case',
    pascal = 'PascalCase',
    kebab = 'kebab-case',
}

/**
 * Replaces whitespaces by the preferred casing.
 * The string itself is NOT converted to the given casing.
 */
export function replaceWhitespaces(input: string, casing: Casing): string {
    const trimmed = input.trim();

    const split = trimmed.split(/\s+/);
    let output = split[0];

    for (let i = 1; i < split.length; i++) {
        const word = split[i];

        switch (casing) {
        case Casing.camel:
        case Casing.pascal:
            output += word.charAt(0).toUpperCase() + word.slice(1);
            break;
        case Casing.snake:
            output += '_' + word;
            break;
        case Casing.kebab:
            output += '-' + word;
            break;
        }
    }

    return output;
}
