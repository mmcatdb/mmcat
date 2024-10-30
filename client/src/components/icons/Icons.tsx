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

const className='mr-2 w-5 h-5';

export const sidebarIconMap: Record<string, IconSet> = {
    heart: {
        outline: <HeartIconOutline className={className} />,
        solid: <HeartIconSolid className={className} />,
    },
    circleStack: {
        outline: <CircleStackIconOutline className={className} />,
        solid: <CircleStackIconSolid className={className} />,
    },
    codeBracketSquare: {
        outline: <CodeBracketSquareIconOutline className={className} />,
        solid: <CodeBracketSquareIconSolid className={className} />,
    },
    lightBulb: {
        outline: <LightBulbIconOutline className={className} />,
        solid: <LightBulbIconSolid className={className} />,
    },
    documentText: {
        outline: <DocumentTextIconOutline className={className} />,
        solid: <DocumentTextIconSolid className={className} />,
    },
};
