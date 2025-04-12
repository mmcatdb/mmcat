import styled from 'styled-components';

export const StyledSvgWrapper = styled.div`
  line-height: 0;
  height: 100%;
  position: relative;
  > svg {
    height: 100%;
    width: 100%;
    background-color: ${props => props.theme.frameBackground};
    .node {
      cursor: pointer;
      > .ring {
        fill: none;
        opacity: 0;
        stroke: #6ac6ff;
      }
      &.selected {
        > .ring {
          stroke: #fdcc59;
          opacity: 0.3;
        }
      }
      &:hover {
        > .ring {
          stroke: #6ac6ff;
          opacity: 0.3;
        }
      }
    }
    .relationship {
      > text {
        fill: ${props => props.theme.primaryText};
      }
      > .overlay {
        opacity: 0;
        fill: #6ac6ff;
      }
      &.selected {
        > .overlay {
          fill: #fdcc59;
          opacity: 0.3;
        }
      }
      &:hover {
        > .overlay {
          fill: #6ac6ff;
          opacity: 0.3;
        }
      }
    }
    .remove_node {
      .expand_node {
        &:hover {
          border: 2px #000 solid;
        }
      }
    }
    .b-outline {
      cursor: pointer;
    }
    path {
      &.context-menu-item {
        stroke-width: 2px;
        fill: ${props => props.theme.primaryBackground};
      }
    }
    text {
      line-height: normal;
      &.context-menu-item {
        fill: #fff;
        text-anchor: middle;
        pointer-events: none;
        font-size: 14px;
      }
    }
    .context-menu-item {
      cursor: pointer;
      &:hover {
        fill: #b9b9b9;
        font-size: 14px;
      }
    }
  }
`;

export const StyledZoomHolder = styled.div<{
  isFullscreen: boolean;
}>`
  position: ${props => (props.isFullscreen ? 'fixed' : 'absolute')};
  display: flex;
  flex-direction: column;
  bottom: 8px;
  left: 8px;
  border-left: ${props => props.theme.inFrameBorder};
  border-right: ${props => props.theme.inFrameBorder};
  border-top: ${props => props.theme.inFrameBorder};
  background: ${props => props.theme.frameSidebarBackground};
  box-shadow: ${props => props.theme.standardShadow};
`;

export const StyledZoomButton = styled.button`
  border: none;
  color: ${props => props.theme.frameButtonTextColor};
  background: transparent;
  padding: 8px;
  &:hover {
    background: ${props => props.theme.frameButtonHoverBackground};
    border-radius: 2px;
  }
  &:enabled:active {
    background: ${props => props.theme.frameButtonActiveBackground};
  }
  &:focus {
    outline: none;
  }
  &:disabled {
    opacity: 0.3;
    cursor: auto;
  }
`;
