import {
    HeartIcon as HeartIconOutline,
    CircleStackIcon as CircleStackIconOutline,
    CodeBracketSquareIcon as CodeBracketSquareIconOutline,
    LightBulbIcon as LightBulbIconOutline,
    DocumentTextIcon as DocumentTextIconOutline,
    RocketLaunchIcon as RocketLaunchIconOutline,
    PlayCircleIcon as PlayCircleIconOutline,
} from '@heroicons/react/24/outline';
import {
    HeartIcon as HeartIconSolid,
    CircleStackIcon as CircleStackIconSolid,
    CodeBracketSquareIcon as CodeBracketSquareIconSolid,
    LightBulbIcon as LightBulbIconSolid,
    DocumentTextIcon as DocumentTextIconSolid,
    RocketLaunchIcon as RocketLaunchIconSolid,
    PlayCircleIcon as PlayCircleIconSolid,
} from '@heroicons/react/24/solid';
import { ArrowPathIcon, CheckCircleIcon, ExclamationCircleIcon, PauseCircleIcon, XCircleIcon } from '@heroicons/react/24/outline';

type IconSet = {
    outline: JSX.Element;
    solid: JSX.Element;
};

const sidebarIconStyle='mr-2 w-6 h-6';

export const sidebarIconMap: Record<string, IconSet> = {
    heart: {
        outline: <HeartIconOutline className={sidebarIconStyle} />,
        solid: <HeartIconSolid className={sidebarIconStyle} />,
    },
    circleStack: {
        outline: <CircleStackIconOutline className={sidebarIconStyle} />,
        solid: <CircleStackIconSolid className={sidebarIconStyle} />,
    },
    codeBracketSquare: {
        outline: <CodeBracketSquareIconOutline className={sidebarIconStyle} />,
        solid: <CodeBracketSquareIconSolid className={sidebarIconStyle} />,
    },
    lightBulb: {
        outline: <LightBulbIconOutline className={sidebarIconStyle} />,
        solid: <LightBulbIconSolid className={sidebarIconStyle} />,
    },
    documentText: {
        outline: <DocumentTextIconOutline className={sidebarIconStyle} />,
        solid: <DocumentTextIconSolid className={sidebarIconStyle} />,
    },
    rocket: {
        outline: <RocketLaunchIconOutline className={sidebarIconStyle} />,
        solid: <RocketLaunchIconSolid className={sidebarIconStyle} />,
    },
    playCircle: {
        outline: <PlayCircleIconOutline className={sidebarIconStyle} />,
        solid: <PlayCircleIconSolid className={sidebarIconStyle} />,
    },
};

const jobStatusIconStyle = 'w-8 h-8';

export function getJobStatusIcon(state: string): JSX.Element {
    switch (state) {
    case 'Disabled':
        return <ExclamationCircleIcon className={`text-gray-400 ${jobStatusIconStyle}`} />;
    case 'Ready':
        return <ArrowPathIcon className={`text-blue-400 ${jobStatusIconStyle}`} />;
    case 'Running':
        return <ArrowPathIcon className={`text-blue-500 animate-spin ${jobStatusIconStyle}`} />;
    case 'Waiting':
        return <PauseCircleIcon className={`text-yellow-500 ${jobStatusIconStyle}`} />;
    case 'Finished':
        return <CheckCircleIcon className={`text-green-500 ${jobStatusIconStyle}`} />;
    case 'Failed':
        return <XCircleIcon className={`text-red-500 ${jobStatusIconStyle}`} />;
    default:
        return <ExclamationCircleIcon className={`text-gray-200 ${jobStatusIconStyle}`} />;
    }
}
