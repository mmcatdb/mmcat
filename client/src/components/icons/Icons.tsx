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
import { MdBolt, MdDashboard, MdOutlineDashboard, MdWorkOutline } from 'react-icons/md';
import {
    MdOutlineBolt,
    MdWork,
} from 'react-icons/md';
import {
    HiOutlinePencilSquare,
    HiPencilSquare,
    HiMiniMagnifyingGlass,
} from 'react-icons/hi2';
import { HiDatabase, HiOutlineDatabase } from 'react-icons/hi';
import { BiCategory, BiSolidCategory } from 'react-icons/bi';

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
    shcemacategory: {
        outline: <BiCategory className={sidebarIconStyle} />,
        solid: <BiSolidCategory className={sidebarIconStyle} />,
    },
    overview: {
        outline: <MdOutlineDashboard className={sidebarIconStyle} />,
        solid: <MdDashboard className={sidebarIconStyle} />,
    },
    editor: {
        outline: <HiOutlinePencilSquare className={sidebarIconStyle} />,
        solid: <HiPencilSquare className={sidebarIconStyle} />,
    },
    datasources: {
        outline: <HiOutlineDatabase className={sidebarIconStyle} />,
        solid: <HiDatabase className={sidebarIconStyle} />,
    },
    actions: {
        outline: <MdOutlineBolt className={sidebarIconStyle} />,
        solid: <MdBolt className={sidebarIconStyle} />,
    },
    jobs: {
        outline: <MdWorkOutline className={sidebarIconStyle} />,
        solid: <MdWork className={sidebarIconStyle} />,
    },
    querying: {
        outline: <HiMiniMagnifyingGlass className={sidebarIconStyle} />,
        solid: <HiMiniMagnifyingGlass className={sidebarIconStyle} />,
    },
};

const jobStatusIconStyle = 'w-8 h-8';

const jobStateStyles: Record<JobState, { color: string, bg: string }> = {
    Disabled: { color: 'text-default-400', bg: 'bg-default-400' },
    Ready: { color: 'text-primary-400', bg: 'bg-primary-400' },
    Running: { color: 'text-primary-500', bg: 'bg-primary-500' },
    Waiting: { color: 'text-warning-500', bg: 'bg-yellow-500' },
    Finished: { color: 'text-success-500', bg: 'bg-success-400' },
    Failed: { color: 'text-danger-400', bg: 'bg-danger-400' },
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
        return <QuestionMarkCircleIcon className={`text-default-200 ${jobStatusIconStyle}`} />;
    }
}
