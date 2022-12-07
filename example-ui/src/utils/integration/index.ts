import { linkDataspecer, type ImportedDataspecer } from './linker';
import { parseDataspecer } from './parser';

export { type ImportedDataspecer } from './linker';

export function importDataspecer({ resources }: any): ImportedDataspecer {
    const parsed = parseDataspecer({ resources });
    return linkDataspecer(parsed);
}

export { addImportedToGraph } from './creator';
