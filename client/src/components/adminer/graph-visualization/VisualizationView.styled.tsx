import styled from 'styled-components';

// import { dim } from 'browser-styles/constants'

export const StyledVisContainer = styled.div<{ isFullscreen: boolean }>`
  width: 100%;
  height: ${
    // props => props.isFullscreen ? '100%' : dim.frameBodyHeight - dim.frameTitlebarHeight * 2 + 'px'
    '100%'
};
  > svg {
    width: 100%;
  }
`;

export const StyledFullSizeContainer = styled.div`
  position: relative;
  height: 100%;
`;
