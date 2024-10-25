import {
    HeartIcon as HeartIconOutline,
    CircleStackIcon as CircleStackIconOutline,
    CodeBracketSquareIcon as CodeBracketSquareIconOutline,
    LightBulbIcon as LightBulbIconOutline,
    DocumentTextIcon as DocumentTextIconOutline,
} from '@heroicons/react/24/outline';
import {
    HeartIcon as HeartIconSolid,
    CircleStackIcon as CircleStackIconSolid,
    CodeBracketSquareIcon as CodeBracketSquareIconSolid,
    LightBulbIcon as LightBulbIconSolid,
    DocumentTextIcon as DocumentTextIconSolid,
} from '@heroicons/react/24/solid';

type IconSet = {
    outline: JSX.Element;
    solid: JSX.Element;
};

export const sidebarIconMap: Record<string, IconSet> = {
    heart: {
        outline: <HeartIconOutline className='sidebar-icon' />,
        solid: <HeartIconSolid className='sidebar-icon' />,
    },
    circleStack: {
        outline: <CircleStackIconOutline className='sidebar-icon' />,
        solid: <CircleStackIconSolid className='sidebar-icon' />,
    },
    codeBracketSquare: {
        outline: <CodeBracketSquareIconOutline className='sidebar-icon' />,
        solid: <CodeBracketSquareIconSolid className='sidebar-icon' />,
    },
    lightBulb: {
        outline: <LightBulbIconOutline className='sidebar-icon' />,
        solid: <LightBulbIconSolid className='sidebar-icon' />,
    },
    documentText: {
        outline: <DocumentTextIconOutline className='sidebar-icon' />,
        solid: <DocumentTextIconSolid className='sidebar-icon' />,
    },
};
