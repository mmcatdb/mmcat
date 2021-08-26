package de.hda.fbi.modules.schemaextraction.tree;

import org.junit.Before;
import org.junit.Test;

import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class TreeNodeInfoServiceImplTest {


    private TreeNodeInfoService treeNodeInfoService;

    @Before
    public void init() {
        treeNodeInfoService = new TreeNodeInfoServiceImpl();
    }

    @Test
    public void getDeletedNodes_exactlyOneDeletedNodeExists_oneDeletedNodeIsReturned() {
        TreeNode tree = new TreeNode("root", 0, null, null, 0, null, "");
        tree.addTreeNodeInfo(null, 1, null);

        TreeNode child = new TreeNode("child", 0, null, null, 0, null, "");
        for (int i = 0; i < 10; i++) {
            child.addTreeNodeInfo(null, i, null);
        }
        tree.addChild(child);


        Vector<TreeNode> deletedNodes = treeNodeInfoService.getDeletedNodes(tree, 9, 10);

        assertNotNull(deletedNodes);
        assertEquals(1, deletedNodes.size());
        assertEquals("child", deletedNodes.get(0).getName());
    }

    @Test
    public void getDeletedNodes_multipleChildButOnlyOneDeletedNode_oneDeletedNodeIsReturned() {
        TreeNode tree = new TreeNode("root", 0, null, null, 0, null, "");
        tree.addTreeNodeInfo(null, 1, null);

        for (int j = 0; j < 10; j++) {
            TreeNode child = new TreeNode("child_" + j, 0, null, null, 0, null, "");
            for (int i = 0; i < 10; i++) {
                child.addTreeNodeInfo(null, i, null);
            }
            tree.addChild(child);
        }
        TreeNode child = new TreeNode("child_failure", 0, null, null, 0, null, "");
        for (int i = 0; i < 9; i++) {
            child.addTreeNodeInfo(null, i, null);
        }
        tree.addChild(child);

        Vector<TreeNode> deletedNodes = treeNodeInfoService.getDeletedNodes(tree, 8, 9);

        assertNotNull(deletedNodes);
        assertEquals(1, deletedNodes.size());
        assertEquals("child_failure", deletedNodes.get(0).getName());
    }

    @Test
    public void getDeletedNodes_multipleChildsEveryHasDeletedNodeInfo_allDeletedNodesIsReturned() {
        TreeNode tree = new TreeNode("root", 0, null, null, 0, null, "");
        tree.addTreeNodeInfo(null, 1, null);

        int numChilds = 10;

        for (int j = 0; j < numChilds; j++) {
            TreeNode child = new TreeNode("child_" + j, 0, null, null, 0, null, "");
            for (int i = 0; i < 10; i++) {
                child.addTreeNodeInfo(null, i, null);
            }
            tree.addChild(child);
        }

        Vector<TreeNode> deletedNodes = treeNodeInfoService.getDeletedNodes(tree, 9, 10);

        assertNotNull(deletedNodes);
        assertEquals(numChilds, deletedNodes.size());
        for (int i = 0; i < deletedNodes.size(); i++) {
            assertEquals("child_" + i, deletedNodes.get(i).getName());
        }
    }

    @Test
    public void getDeletedNodes_noChildNodeExists_emptyListIsReturned() {
        TreeNode tree = new TreeNode("root", 0, null, null, 0, null, "");
        tree.addTreeNodeInfo(null, 1, null);


        Vector<TreeNode> deletedNodes = treeNodeInfoService.getDeletedNodes(tree, 0, 1);

        assertNotNull(deletedNodes);
        assertEquals(0, deletedNodes.size());
    }

    //TODO write tests

}