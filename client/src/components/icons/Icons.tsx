import { HeartIcon as HeartIconOutline, CircleStackIcon as CircleStackIconOutline, CodeBracketSquareIcon as CodeBracketSquareIconOutline, LightBulbIcon as LightBulbIconOutline, DocumentTextIcon as DocumentTextIconOutline, RocketLaunchIcon as RocketLaunchIconOutline, PlayCircleIcon as PlayCircleIconOutline } from '@heroicons/react/24/outline';
import { HeartIcon as HeartIconSolid, CircleStackIcon as CircleStackIconSolid, CodeBracketSquareIcon as CodeBracketSquareIconSolid, LightBulbIcon as LightBulbIconSolid, DocumentTextIcon as DocumentTextIconSolid, RocketLaunchIcon as RocketLaunchIconSolid, PlayCircleIcon as PlayCircleIconSolid } from '@heroicons/react/24/solid';
import { CheckCircleIcon, QuestionMarkCircleIcon, PauseCircleIcon, XCircleIcon, EllipsisHorizontalCircleIcon, StopCircleIcon } from '@heroicons/react/24/outline';
import { JobState } from '@/types/job';
import { MdDashboard, MdOutlineDashboard } from 'react-icons/md';
import { HiOutlinePencilSquare, HiPencilSquare } from 'react-icons/hi2';
import { HiDatabase, HiOutlineDatabase } from 'react-icons/hi';
import { BiCategory, BiSolidCategory } from 'react-icons/bi';

/**
 * Enum for sidebar icon keys
 */
export enum SidebarIconKey {
    Heart = 'heart',
    CircleStack = 'circleStack',
    CodeBracketSquare = 'codeBracketSquare',
    LightBulb = 'lightBulb',
    DocumentText = 'documentText',
    Rocket = 'rocket',
    PlayCircle = 'playCircle',
    SchemaCategory = 'shcemacategory',
    Overview = 'overview',
    Editor = 'editor',
    Datasources = 'datasources',
}

/**
 * Type for an icon set with outline and solid variants
 */
type IconSet = {
    outline: JSX.Element;
    solid: JSX.Element;
};

const SIDEBAR_ICON_STYLE = 'mr-2 w-6 h-6';
const JOB_STATUS_ICON_STYLE = 'w-8 h-8';
  
/**
 * Sidebar icon mapping
 */
export const sidebarIconMap: Record<SidebarIconKey, IconSet> = {
    [SidebarIconKey.Heart]: {
        outline: <HeartIconOutline className={SIDEBAR_ICON_STYLE} />,
        solid: <HeartIconSolid className={SIDEBAR_ICON_STYLE} />,
    },
    [SidebarIconKey.CircleStack]: {
        outline: <CircleStackIconOutline className={SIDEBAR_ICON_STYLE} />,
        solid: <CircleStackIconSolid className={SIDEBAR_ICON_STYLE} />,
    },
    [SidebarIconKey.CodeBracketSquare]: {
        outline: <CodeBracketSquareIconOutline className={SIDEBAR_ICON_STYLE} />,
        solid: <CodeBracketSquareIconSolid className={SIDEBAR_ICON_STYLE} />,
    },
    [SidebarIconKey.LightBulb]: {
        outline: <LightBulbIconOutline className={SIDEBAR_ICON_STYLE} />,
        solid: <LightBulbIconSolid className={SIDEBAR_ICON_STYLE} />,
    },
    [SidebarIconKey.DocumentText]: {
        outline: <DocumentTextIconOutline className={SIDEBAR_ICON_STYLE} />,
        solid: <DocumentTextIconSolid className={SIDEBAR_ICON_STYLE} />,
    },
    [SidebarIconKey.Rocket]: {
        outline: <RocketLaunchIconOutline className={SIDEBAR_ICON_STYLE} />,
        solid: <RocketLaunchIconSolid className={SIDEBAR_ICON_STYLE} />,
    },
    [SidebarIconKey.PlayCircle]: {
        outline: <PlayCircleIconOutline className={SIDEBAR_ICON_STYLE} />,
        solid: <PlayCircleIconSolid className={SIDEBAR_ICON_STYLE} />,
    },
    [SidebarIconKey.SchemaCategory]: {
        outline: <BiCategory className={SIDEBAR_ICON_STYLE} />,
        solid: <BiSolidCategory className={SIDEBAR_ICON_STYLE} />,
    },
    [SidebarIconKey.Overview]: {
        outline: <MdOutlineDashboard className={SIDEBAR_ICON_STYLE} />,
        solid: <MdDashboard className={SIDEBAR_ICON_STYLE} />,
    },
    [SidebarIconKey.Editor]: {
        outline: <HiOutlinePencilSquare className={SIDEBAR_ICON_STYLE} />,
        solid: <HiPencilSquare className={SIDEBAR_ICON_STYLE} />,
    },
    [SidebarIconKey.Datasources]: {
        outline: <HiOutlineDatabase className={SIDEBAR_ICON_STYLE} />,
        solid: <HiDatabase className={SIDEBAR_ICON_STYLE} />,
    },
};

/**
 * Styling configuration for job states, mapping each state to color and background.
 */
const jobStateStyles: Record<JobState, { color: string, bg: string }> = {
    [JobState.Disabled]: { color: 'text-default-400', bg: 'bg-default-400' },
    [JobState.Ready]: { color: 'text-primary-400', bg: 'bg-primary-400' },
    [JobState.Running]: { color: 'text-primary-500', bg: 'bg-primary-500' },
    [JobState.Waiting]: { color: 'text-warning-500', bg: 'bg-yellow-500' },
    [JobState.Finished]: { color: 'text-success-500', bg: 'bg-success-400' },
    [JobState.Failed]: { color: 'text-danger-400', bg: 'bg-danger-400' },
};

export function getJobStateTextStyle(state: JobState): string {
    return jobStateStyles[state].bg;
}
function getJobStateIconStyle(state: JobState): string {
    return jobStateStyles[state].color;
}
  
/**
 * Returns the appropriate icon for a job's status.
 */
export function getJobStateIcon(state: JobState): JSX.Element {
    const iconColor = getJobStateIconStyle(state);
  
    switch (state) {
    case JobState.Disabled:
        return <StopCircleIcon className={`${iconColor} ${JOB_STATUS_ICON_STYLE}`} />;
    case JobState.Ready:
        return <PlayCircleIconOutline className={`${iconColor} ${JOB_STATUS_ICON_STYLE}`} />;
    case JobState.Running:
        return <EllipsisHorizontalCircleIcon className={`${iconColor} animate-spin ${JOB_STATUS_ICON_STYLE}`} />;
    case JobState.Waiting:
        return <PauseCircleIcon className={`${iconColor} ${JOB_STATUS_ICON_STYLE}`} />;
    case JobState.Finished:
        return <CheckCircleIcon className={`${iconColor} ${JOB_STATUS_ICON_STYLE}`} />;
    case JobState.Failed:
        return <XCircleIcon className={`${iconColor} ${JOB_STATUS_ICON_STYLE}`} />;
    default:
        return <QuestionMarkCircleIcon className={`text-default-200 ${JOB_STATUS_ICON_STYLE}`} />;
    }
}