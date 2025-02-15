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
import { CheckCircleIcon, QuestionMarkCircleIcon, 
    PauseCircleIcon, XCircleIcon, EllipsisHorizontalCircleIcon,
    StopCircleIcon,
} from '@heroicons/react/24/outline';
import { type JobState } from '@/types/job';

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

const jobStateStyles: Record<JobState, { color: string, bg: string }> = {
    Disabled: { color: 'text-zinc-400', bg: 'bg-zinc-400' },
    Ready: { color: 'text-blue-400', bg: 'bg-blue-400' },
    Running: { color: 'text-blue-500', bg: 'bg-blue-500' },
    Waiting: { color: 'text-yellow-500', bg: 'bg-yellow-500' },
    Finished: { color: 'text-green-500', bg: 'bg-green-500' },
    Failed: { color: 'text-red-500', bg: 'bg-red-500' },
};

export function getJobStateTextStyle(state: JobState): string {
    return jobStateStyles[state]?.bg;
}

function getJobStateIconStyle(state: JobState): string {
    return jobStateStyles[state]?.color;
}

export function getJobStatusIcon(state: string): JSX.Element {
    const iconColor = getJobStateIconStyle(state as JobState);

    switch (state) {
    case 'Disabled':
        return <StopCircleIcon className={`${iconColor} ${jobStatusIconStyle}`} />;
    case 'Ready':
        return <PlayCircleIconOutline className={`${iconColor} ${jobStatusIconStyle}`} />;
    case 'Running':
        return <EllipsisHorizontalCircleIcon className={`${iconColor} animate-spin ${jobStatusIconStyle}`} />;
    case 'Waiting':
        return <PauseCircleIcon className={`${iconColor} ${jobStatusIconStyle}`} />;
    case 'Finished':
        return <CheckCircleIcon className={`${iconColor} ${jobStatusIconStyle}`} />;
    case 'Failed':
        return <XCircleIcon className={`${iconColor} ${jobStatusIconStyle}`} />;
    default:
        return <QuestionMarkCircleIcon className={`text-zinc-200 ${jobStatusIconStyle}`} />;
    }
}
