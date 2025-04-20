import styled from 'styled-components';

export const panelWidth = 300;

export const StyledNodeInspectorContainer = styled.div<{
  paneWidth: number;
}>`
  position: absolute;
  right: 8px;
  top: 8px;
  bottom: 8px;
  z-index: 1;
  width: ${props => props.paneWidth}px;
  transition: 0.2s ease-out;
  max-width: 95%;
  background: ${props => props.theme.editorBackground};
  color: ${props => props.theme.primaryText};
  font-family: ${props => props.theme.drawerHeaderFontFamily};
  box-shadow: ${props => props.theme.standardShadow};
  overflow: hidden;
  overflow-y: auto;
`;
export const StyledNodeInspectorTopMenuChevron = styled.button<{
  expanded: boolean;
}>`
  background-color: ${props => props.theme.frameSidebarBackground};
  cursor: pointer;
  position: absolute;
  right: 8px;
  top: 8px;
  z-index: 2;
  width: 32px;
  height: 32px;
  padding: 6px;
  color: ${props => props.theme.frameNodePropertiesPanelIconTextColor};
  text-align: center;
  ${props =>
        !props.expanded &&
    `background: ${props.theme.editorBackground};
       box-shadow: ${props.theme.standardShadow};
    `}
`;

export const PaneContainer = styled.div`
  width: ${panelWidth}px;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 16px;
`;
