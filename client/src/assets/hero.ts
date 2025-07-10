import type plugin from 'tailwindcss/plugin';
import { heroui } from '@heroui/react';

// FIXME There is some bug in ts preventing the export without this cast. Remove when fixed. The bug:
// The inferred type of 'default' cannot be named without a reference to '../../node_modules/tailwindcss/dist/types-B254mqw1.mjs'. This is likely not portable. A type annotation is necessary.
// https://github.com/heroui-inc/heroui/issues/5331
const heoruiExport: ReturnType<typeof plugin> = heroui();
export default heoruiExport;
