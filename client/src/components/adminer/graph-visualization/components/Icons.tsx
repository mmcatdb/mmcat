import { MagnifyingGlassPlusIcon, MagnifyingGlassMinusIcon } from '@heroicons/react/24/solid';

const SMALL_SIZE = 15;

const ZOOM_ICONS_DEFAULT_SIZE_IN_PX = SMALL_SIZE;
const ZOOM_ICONS_LARGE_SCALE_FACTOR = 1.2;

export function ZoomInIcon({ large = false }: { large?: boolean }) {
    const scale = large ? ZOOM_ICONS_LARGE_SCALE_FACTOR : 1;
    return (
        <MagnifyingGlassPlusIcon
            width={scale * ZOOM_ICONS_DEFAULT_SIZE_IN_PX}
            height={scale * ZOOM_ICONS_DEFAULT_SIZE_IN_PX}
        />
    );
}

export function ZoomOutIcon({ large = false }: { large?: boolean }) {
    const scale = large ? ZOOM_ICONS_LARGE_SCALE_FACTOR : 1;
    return (
        <MagnifyingGlassMinusIcon
            width={scale * ZOOM_ICONS_DEFAULT_SIZE_IN_PX}
            height={scale * ZOOM_ICONS_DEFAULT_SIZE_IN_PX}
        />
    );
}

export function ZoomToFitIcon({ large = false }: { large?: boolean }) {
    const scale = large ? ZOOM_ICONS_LARGE_SCALE_FACTOR : 1;
    return (
        <svg
            width={scale * ZOOM_ICONS_DEFAULT_SIZE_IN_PX}
            height={scale * ZOOM_ICONS_DEFAULT_SIZE_IN_PX}
            viewBox='0 0 24 24'
            fill='none'
            xmlns='http://www.w3.org/2000/svg'
        >
            <path
                d='m17.616 17.616-.53-.53zm0-11.232-.53.53zm-11.233 0-.53-.53zm0 11.232.53-.53zM20.341 3.66l-.53.53zm-16.682 0 .53.53zm0 16.682.53-.53zM21.75 14.38a.75.75 0 1 0-1.5 0zm-7.378 5.871a.75.75 0 0 0 0 1.5zm-4.616 1.5a.75.75 0 1 0 0-1.5zM3.75 14.379a.75.75 0 1 0-1.5 0zm-1.5-4.435a.75.75 0 1 0 1.5 0zM9.756 3.75a.75.75 0 1 0 0-1.5zm4.616-1.5a.75.75 0 0 0 0 1.5zm5.878 7.694a.75.75 0 1 0 1.5 0zM7.455 18.81h9.09v-1.5h-9.09zm9.09 0c.6 0 1.177-.238 1.602-.663l-1.061-1.06a.77.77 0 0 1-.541.223zm1.602-.663a2.27 2.27 0 0 0 .663-1.602h-1.5c0 .203-.08.398-.224.541zm.663-1.602v-9.09h-1.5v9.09zm0-9.09c0-.6-.239-1.177-.663-1.602l-1.061 1.06a.77.77 0 0 1 .224.542zm-.663-1.602a2.26 2.26 0 0 0-1.602-.663v1.5a.76.76 0 0 1 .54.224zm-1.602-.663h-9.09v1.5h9.09zm-9.09 0c-.601 0-1.177.238-1.602.663l1.06 1.06a.77.77 0 0 1 .542-.223zm-1.602.663a2.26 2.26 0 0 0-.663 1.602h1.5c0-.203.08-.398.224-.541zM5.19 7.455v9.09h1.5v-9.09zm0 9.09c0 .6.238 1.177.663 1.602l1.06-1.06a.77.77 0 0 1-.223-.542zm.663 1.602a2.26 2.26 0 0 0 1.602.663v-1.5a.77.77 0 0 1-.541-.224zM18.75 21.75a3 3 0 0 0 2.121-.879l-1.06-1.06a1.5 1.5 0 0 1-1.061.439zm2.121-.879a3 3 0 0 0 .879-2.121h-1.5a1.5 1.5 0 0 1-.44 1.06zM21.75 5.25a3 3 0 0 0-.879-2.121l-1.06 1.06a1.5 1.5 0 0 1 .439 1.061zm-.879-2.121a3 3 0 0 0-2.121-.879v1.5a1.5 1.5 0 0 1 1.06.44zM5.25 2.25a3 3 0 0 0-2.121.879l1.06 1.06A1.5 1.5 0 0 1 5.25 3.75zm-2.121.879A3 3 0 0 0 2.25 5.25h1.5c0-.398.158-.78.44-1.06zM2.25 18.75a3 3 0 0 0 .879 2.121l1.06-1.06a1.5 1.5 0 0 1-.439-1.061zm.879 2.121a3 3 0 0 0 2.121.879v-1.5a1.5 1.5 0 0 1-1.06-.44zM21.75 18.75v-4.371h-1.5v4.371zm-3 1.5h-4.378v1.5h4.378zm-13.5 1.5h4.506v-1.5H5.25zm-1.5-3v-4.371h-1.5v4.371zm-1.5-13.5v4.694h1.5V5.25zm3-1.5h4.506v-1.5H5.25zm13.5-1.5h-4.378v1.5h4.378zm1.5 3v4.694h1.5V5.25z'
                fill='currentColor'
            />
        </svg>
    );
}

export const menuIcons = {
    'Expand / Collapse':
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><g class="icon"><defs><style>.a{fill:none;stroke:currentColor;stroke-linecap:round;stroke-linejoin:round;stroke-width:1.5px;}</style></defs><title>Expand / Collapse</title><circle class="a" cx="13.5" cy="10.498" r="3.75"/><circle class="a" cx="21" cy="2.998" r="2.25"/><circle class="a" cx="21" cy="15.748" r="2.25"/><circle class="a" cx="13.5" cy="20.998" r="2.25"/><circle class="a" cx="3" cy="20.998" r="2.25"/><circle class="a" cx="3.75" cy="5.248" r="2.25"/><line class="a" x1="16.151" y1="7.848" x2="19.411" y2="4.588"/><line class="a" x1="16.794" y1="12.292" x2="19.079" y2="14.577"/><line class="a" x1="13.5" y1="14.248" x2="13.5" y2="18.748"/><line class="a" x1="10.851" y1="13.147" x2="4.59" y2="19.408"/><line class="a" x1="10.001" y1="9.149" x2="5.61" y2="6.514"/></g></svg>',
    Unlock:
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><g class="icon"><defs><style>.a{fill:none;stroke:currentColor;stroke-linecap:round;stroke-linejoin:round;stroke-width:1.5px;}</style></defs><title>Unlock</title><path class="a" d="M.75,9.75V6a5.25,5.25,0,0,1,10.5,0V9.75"/><rect class="a" x="6.75" y="9.75" width="16.5" height="13.5" rx="1.5" ry="1.5"/><line class="a" x1="15" y1="15" x2="15" y2="18"/></g></svg>',
    Remove:
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><g class="icon"><defs><style>.a{fill:none;stroke:currentColor;stroke-linecap:round;stroke-linejoin:round;stroke-width:1.5px;}</style></defs><title>Remove</title><path class="a" d="M8.153,13.664a12.271,12.271,0,0,1-5.936-4.15L1.008,7.96a.75.75,0,0,1,0-.92L2.217,5.486A12.268,12.268,0,0,1,11.9.75h0a12.269,12.269,0,0,1,9.684,4.736L22.792,7.04a.748.748,0,0,1,0,.92L21.584,9.514"/><path class="a" d="M10.4,10.937A3.749,3.749,0,1,1,15.338,9"/><circle class="a" cx="17.15" cy="17.25" r="6"/><line class="a" x1="14.15" y1="17.25" x2="20.15" y2="17.25"/></g></svg>',
};
