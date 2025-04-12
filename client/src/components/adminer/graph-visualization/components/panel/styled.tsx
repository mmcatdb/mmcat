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
