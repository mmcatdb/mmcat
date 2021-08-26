package de.hda.fbi.modules.schemaextraction.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

/**
 * Tree representation of a db collection as a tree structure. This class contains metadata about the collection like
 * unique timestamps and the number of documents.
 */
public class Tree {

    private TreeNode root;

    // name of the Tree
    private String name;

    //todo check if there is asynchronous access. If not Vector can be replaced with ArrayList or TreeSet to increase performance.
    private ArrayList<Integer> timestamps = new ArrayList<>();

    private int numberOfDocuments;

    public Tree(String name, TreeNode aRoot) {

        this.setName(name);
        this.root = aRoot;
    }

    public int getNumberOfDocuments() {
        return this.numberOfDocuments;
    }

    /**
     * Adds a given timestamp to the collection of timestamps of the Tree-Object if the given timestamp is not present
     * in the collection to prevent duplicates
     *
     * @param timestamp the timestamp that should be added.
     */
    public void addTimestamp(int timestamp) {
        if (!timestamps.contains(timestamp)) {
            timestamps.add(timestamp);
            Collections.sort(this.timestamps);
        }
    }

    public boolean hasTimestamp(int version) {
        return this.timestamps.contains(version);
    }

    public void increaseNumberOfDocuments() {
        this.numberOfDocuments++;
    }

    public TreeNode getRoot() {
        return this.root;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Integer> getTimestamps() {
        return timestamps;
    }

    public void setVersions(ArrayList<Integer> timestamps) {
        this.timestamps = timestamps;
    }

    /**
     * Returns the next higher timestamp of the given timestamp. If there is no timestamp found <b>0</b> is returned.
     *
     * @param currentTimestamp
     * @return the next higher timestamp. If no higher timestamp exist <b>0</b> is returned.
     */
    public Integer getTimestampAfter(Integer currentTimestamp) {
        for (Integer timestamp : this.timestamps) {
            if (timestamp > currentTimestamp) {
                return timestamp;
            }
        }
        return 0;
    }

    /**
     * Returns the next lower timestamp of the given timestamp. If there is no timestamp found <b>0</b> is returned.
     *
     * @param lastTimestamp
     * @return the next lower timestamp. If no lower timestamp exist <b>0</b> is returned.
     */
    public Integer getTimestampBefore(Integer lastTimestamp) {
        //necessary to iterate and keep the current lower timestamp
        Integer highestTimestampLowerThanTimestamp = 0;
//        System.out.println("last: " + lastTimestamp);
        for (Integer timestamp : this.timestamps) {
            if (timestamp < lastTimestamp) {
                // if timestamp is lower than the given one keep this until the current timestamp is equal or higher
                // than the given one.
                highestTimestampLowerThanTimestamp = timestamp;
            } else {
                // if equal or higher stop iterating and return the next lowest timestamp.
                break;
            }
        }
//        System.out.println("return " + highestTimestampLowerThanTimestamp);
        return highestTimestampLowerThanTimestamp;
    }
}