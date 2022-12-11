import type { ImportedDataspecer } from '@/types/integration';
import { linkDataspecer, } from './linker';
import { parseDataspecer } from './parser';

export function importDataspecer({ resources }: any): ImportedDataspecer {
    const parsed = parseDataspecer({ resources });
    console.log(parsed);
    return linkDataspecer(parsed);
}

export { addImportedToGraph } from './creator';
