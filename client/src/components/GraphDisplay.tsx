import clsx from 'clsx';
import { type MouseEvent, useCallback, useEffect, useMemo, useRef, useState, type WheelEvent } from 'react';

/** In some relative units. The point { x: 0; y: 0 } is in the top left corner. */
type Position = {
    x: number;
    y: number;
};

/** In pixels. */
type ScreenPosition = {
    left: number;
    top: number;
};

type Node = {
    id: string;
    label: string;
    position: Position;
};

type Edge = {
    id: string;
    label: string;
    from: string;
    to: string;
};

type Coordinates = {
    /** Where the point { x: 0; y: 0 } is on the screen. */
    origin: ScreenPosition;
    /** Distance in pixels = scale * distance in position units. */
    scale: number;
}

type GraphProps = Readonly<{
    nodes: Node[];
    edges: Edge[];
    width: number;
    height: number;
}>;

export function GraphDisplay({ nodes, edges, width, height }: GraphProps) {
    const [ localNodes, setLocalNodes ] = useState(nodes);

    const [ coordinates, setCoordinates ] = useState<Coordinates>(() => computeInitialCoordinates(nodes, width, height));
    const screenRef = useRef<HTMLDivElement>(null);

    const wheel = useCallback((e: WheelEvent<HTMLDivElement>) => {
        if (!screenRef.current)
            return;

        e.stopPropagation();
        const rect = screenRef.current.getBoundingClientRect();
        const mouse: ScreenPosition = {
            left: e.clientX - rect.left,
            top: e.clientY - rect.top,
        };

        // We want to transform the coordinates in such a way that the mouse point will be on the same screen position as before.
        // Therefore, it must hold origin.left + scale * mousePosition.x = newOrigin.left + newScale * mousePosition.x and the same for top and y.

        setCoordinates(coordinates => {
            const mousePosition = getPosition(mouse, coordinates);
            const scale = coordinates.scale * (1 - e.deltaY / 1000);
            const origin = {
                left: coordinates.origin.left + (coordinates.scale - scale) * mousePosition.x,
                top: coordinates.origin.top + (coordinates.scale - scale) * mousePosition.y,
            };

            return { origin, scale };
        });
    }, [ screenRef ]);

    const lastPosition = useRef<ScreenPosition | undefined>(undefined);
    const [ isDown, setIsDown ] = useState(false);

    const mouseDown = useCallback((e: MouseEvent<HTMLDivElement>) => {
        if (e.target !== screenRef.current)
            return;
        // We don't want to interfere with the right click.
        if (e.button === 2)
            return;

        setIsDown(true);
        lastPosition.current = { left: e.clientX, top: e.clientY };
    }, []);

    useEffect(() => {
        const mouseup = () => {
            lastPosition.current = undefined;
            setIsDown(false);
        };
        const mousemove = throttle((e: MouseEvent) => {
            if (!lastPosition.current)
                return;

            const diff = { left: e.clientX - lastPosition.current.left, top: e.clientY - lastPosition.current.top };
            lastPosition.current = { left: e.clientX, top: e.clientY };
            setCoordinates(c => ({
                ...c,
                origin: {
                    left: c.origin.left + diff.left,
                    top: c.origin.top + diff.top,
                },
            }));
        });

        document.addEventListener('mouseup', mouseup);
        // @ts-expect-error - What the actual fuck?
        document.addEventListener('mousemove', mousemove);

        return () => {
            document.removeEventListener('mouseup', mouseup);
            // @ts-expect-error - What the actual fuck?
            document.removeEventListener('mousemove', mousemove);
        };
    }, []);

    const setNode = useCallback((node: Node) => {
        setLocalNodes(oldNodes => oldNodes.map(n => n.id === node.id ? node : n));
    }, []);

    console.log('render ' + Math.random());

    return (
        <div
            style={{ width, height }}
            className={clsx('relative bg-slate-400 overflow-hidden', isDown ? 'cursor-grabbing' : 'cursor-grab')}
            onWheel={wheel}
            ref={screenRef}
            onMouseDown={mouseDown}
            id='screen'
        >
            {localNodes.map(node => (
                <NodeDisplay key={node.id} node={node} coordinates={coordinates} setNode={setNode} />
            ))}
            {edges.map(edge => (
                <EdgeDisplay key={edge.id} edge={edge} nodes={localNodes} coordinates={coordinates} />
            ))}
        </div>
    );
}

function computeInitialCoordinates(nodes: Node[], width: number, height: number): Coordinates {
    const minX = Math.min(...nodes.map(node => node.position.x));
    const maxX = Math.max(...nodes.map(node => node.position.x));
    const minY = Math.min(...nodes.map(node => node.position.y));
    const maxY = Math.max(...nodes.map(node => node.position.y));

    const scale = Math.min(width / (100 + maxX - minX), height / (100 + maxY - minY));

    const centroid = {
        x: (minX + maxX) / 2,
        y: (minY + maxY) / 2,
    };

    const origin = {
        left: (width / 2) - scale * centroid.x,
        top: (height / 2) - scale * centroid.y,
    };

    return { origin, scale };
}

function getPosition(screenPosition: ScreenPosition, coordinates: Coordinates): Position {
    return {
        x: (screenPosition.left - coordinates.origin.left) / coordinates.scale,
        y: (screenPosition.top - coordinates.origin.top) / coordinates.scale,
    };
}

function getScreenPosition(position: Position, coordinates: Coordinates): ScreenPosition {
    return {
        left: coordinates.origin.left + coordinates.scale * position.x,
        top: coordinates.origin.top + coordinates.scale * position.y,
    };
}

const THROTTLE_DURATION_MS = 20;
function throttle<T extends(...args: Parameters<T>) => void>(callback: T): T {
    let timeout: NodeJS.Timeout | undefined = undefined;

    return ((...args: Parameters<T>) => {
        if (timeout)
            return;

        callback(...args);
        timeout = setTimeout(() => {
            timeout = undefined;
        }, THROTTLE_DURATION_MS);
    }) as T;
}

type NodeDisplayProps = Readonly<{
    node: Node;
    coordinates: Coordinates;
    setNode: (node: Node) => void;
}>;

function NodeDisplay({ node, coordinates, setNode }: NodeDisplayProps) {
    const [ isDragging, setIsDragging ] = useState(false);

    function click(e: MouseEvent) {
        console.log(e);
    }

    function mouseDown() {
        setIsDragging(true);
    }

    function mouseUp(e: MouseEvent<HTMLDivElement>) {
        if (!isDragging)
            return;

        const rect = document.getElementById('screen')!.getBoundingClientRect();
        const mouse: ScreenPosition = {
            left: e.clientX - rect.left,
            top: e.clientY - rect.top,
        };

        setIsDragging(false);
        setNode({ ...node, position: getPosition(mouse, coordinates) });
        console.log('SET NODE');
    }


    function mouseMove(e: MouseEvent<HTMLDivElement>) {
        if (!isDragging)
            return;

        const rect = document.getElementById('screen')!.getBoundingClientRect();
        const mouse: ScreenPosition = {
            left: e.clientX - rect.left,
            top: e.clientY - rect.top,
        };

        setNode({ ...node, position: getPosition(mouse, coordinates) });
    }

    return (
        <div className='absolute w-0 h-0 select-none z-10' style={getScreenPosition(node.position, coordinates)}>
            <div
                className='absolute w-8 h-8 -left-4 -top-4 rounded-full border-2 border-slate-700 bg-white hover:bg-green-400 cursor-pointer active:bg-green-500'
                onClick={click}
                onMouseDown={mouseDown}
                onMouseUp={mouseUp}
                onMouseMove={mouseMove}
            />
            <div className='w-fit'>
                <span className='relative -left-1/2 -top-10'>
                    {node.label}
                </span>
            </div>
        </div>
    );
}

/** In pixels */
const EDGE_OFFSET = 20;

type EdgeDisplayProps = Readonly<{
    edge: Edge;
    nodes: Node[];
    coordinates: Coordinates;
}>;

function EdgeDisplay({ edge, nodes, coordinates }: EdgeDisplayProps) {
    const cache = useMemo(() => ({
        from: nodes.find(node => node.id === edge.from)!,
        to: nodes.find(node => node.id === edge.to)!,
    }), [ edge, nodes ]);

    const start = getScreenPosition(cache.from.position, coordinates);
    const end = getScreenPosition(cache.to.position, coordinates);

    const center = {
        left: (start.left + end.left) / 2,
        top: (start.top + end.top) / 2,
    };
    const x = end.left - start.left;
    const y = end.top - start.top;
    const angle = Math.atan2(y, x);

    const width = Math.sqrt(x * x + y * y) - 2 * EDGE_OFFSET;

    return (
        <div className='absolute w-0 h-0 select-none' style={center}>
            <div className='absolute h-1 bg-slate-700 rounded-full' style={{ width, transform: `translateX(-50%) rotate(${angle}rad)` }}>

            </div>
        </div>
    );
}
