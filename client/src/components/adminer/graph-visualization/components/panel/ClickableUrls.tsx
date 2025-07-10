import { type ElementType, Fragment, type JSX } from 'react';

// credits to https://www.regextester.com/96504, modified though
const URL_REGEX = /(?:https?|s?ftp|bolt):\/\/(?:(?:[^\s()<>]+|\((?:[^\s()<>]+|(?:\([^\s()<>]+\)))?\))+(?:\((?:[^\s()<>]+|(?:\(?:[^\s()<>]+\)))?\)|[^\s`!()\[\]{};:'".,<>?«»“”‘’]))?/gi;

type ClickableUrlsProps = {
  text?: string | null;
  WrappingTag?: keyof JSX.IntrinsicElements | ElementType;
}

export function ClickableUrls({
    text,
    WrappingTag = 'span',
}: ClickableUrlsProps) {
    const definedText = text ?? '';
    const urls = definedText.match(URL_REGEX) ?? [];
    return (
        <WrappingTag>
            {definedText.split(URL_REGEX).map((text, index) => {
                /* since we never move these components this key should be fine */
                return (
                    <Fragment key={index}>
                        {text}
                        {urls[index] && (
                            <a href={urls[index]} target='_blank' rel='noreferrer'>
                                {urls[index]}
                            </a>
                        )}
                    </Fragment>
                );
            })}
        </WrappingTag>
    );
}
