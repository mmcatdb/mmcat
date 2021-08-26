package de.hda.fbi.modules.schemaextraction.tree;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;

public class TreeTest {

    private static Logger LOGGER = LoggerFactory.getLogger(TreeTest.class);

    @Test
    public void getTimestampAfter_emptyListOfTimestamp_zeroIsReturned() {
        Tree tree = new Tree("", null);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            //test random numbers just in case.
            Integer randomNumber = random.nextInt(50);
            LOGGER.debug("Testing number " + randomNumber);
            assertEquals(0, tree.getTimestampAfter(randomNumber), 0);
        }
    }

    @Test
    public void getTimestampAfter_listContainingOneElementWhichIsLower_zeroIsReturned() {
        Tree tree = new Tree("", null);
        tree.addTimestamp(0);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            //test random numbers which is higher than 0.
            Integer randomNumber = random.nextInt(50) + 1;
            LOGGER.debug("Testing number " + randomNumber);
            assertEquals(0, tree.getTimestampAfter(randomNumber), 0);
        }
    }

    @Test
    public void getTimestampAfter_listContainingMultipleElementsWhichAreLower_zeroIsReturned() {
        Tree tree = new Tree("", null);
        for (int i = 0; i < 10; i++) {
            tree.addTimestamp(i);
        }
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            //test random numbers which is higher than 10.
            Integer randomNumber = random.nextInt(50) + 10;
            LOGGER.debug("Testing number " + randomNumber);
            assertEquals(0, tree.getTimestampAfter(randomNumber), 0);
        }
    }

    @Test
    public void getTimestampAfter_listContainingOneElementWhichIsHigher_timestampIsReturned() {
        Tree tree = new Tree("", null);
        tree.addTimestamp(50);
        for (int i = 0; i < 10; i++) {
            //test random numbers which are between 0 and 5 (exclusive).
            Integer randomNumber = ThreadLocalRandom.current().nextInt(0, 50);
            LOGGER.debug("Testing number " + randomNumber);
            assertEquals(50, tree.getTimestampAfter(randomNumber), 0);
        }
    }

    @Test
    public void getTimestampAfter_listContainingOneElementWhichIsEqual_zeroIsReturned() {
        Tree tree = new Tree("", null);
        tree.addTimestamp(50);
        assertEquals(0, tree.getTimestampAfter(50), 0);
    }

    //------------------------------------------------------------------------------------------------------------------

    @Test
    public void getTimestampBefore_emptyListOfTimestamp_zeroIsReturned() {
        Tree tree = new Tree("", null);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            //test random numbers just in case.
            Integer randomNumber = random.nextInt(50);
            LOGGER.debug("Testing number " + randomNumber);
            assertEquals(0, tree.getTimestampBefore(randomNumber), 0);
        }
    }

    @Test
    public void getTimestampBefore_listContainingOneElementWhichIsHigher_zeroIsReturned() {
        Tree tree = new Tree("", null);
        tree.addTimestamp(55);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            //test random numbers which is lower than 0.
            Integer randomNumber = random.nextInt(50) + 1;
            LOGGER.debug("Testing number " + randomNumber);
            assertEquals(0, tree.getTimestampBefore(randomNumber), 0);
        }
    }

    @Test
    public void getTimestampBefore_listContainingMultipleElementsWhichAreHigher_zeroIsReturned() {
        Tree tree = new Tree("", null);
        for (int i = 0; i < 10; i++) {
            tree.addTimestamp(i + 50);
        }
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            //test random numbers which are lower than 50.
            Integer randomNumber = random.nextInt(50);
            LOGGER.debug("Testing number " + randomNumber);
            assertEquals(0, tree.getTimestampBefore(randomNumber), 0);
        }
    }

    @Test
    public void getTimestampBefore_listContainingOneElementWhichIsLower_timestampIsReturned() {
        Tree tree = new Tree("", null);
        tree.addTimestamp(0);
        for (int i = 0; i < 10; i++) {
            //test random numbers which are between 0 and 5 (exclusive).
            Integer randomNumber = ThreadLocalRandom.current().nextInt(1, 50);
            LOGGER.debug("Testing number " + randomNumber);
            assertEquals(0, tree.getTimestampBefore(randomNumber), 0);
        }
    }

    @Test
    public void getTimestampBefore_listContainingOneElementWhichIsEqual_zeroIsReturned() {
        Tree tree = new Tree("", null);
        tree.addTimestamp(50);
        assertEquals(0, tree.getTimestampBefore(50), 0);
    }


}