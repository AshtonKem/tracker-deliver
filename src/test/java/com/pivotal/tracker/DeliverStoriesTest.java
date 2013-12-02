package com.pivotal.tracker;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jenkinsci.plugins.Pivotal.DeliverStories;
import static org.junit.Assert.*;


@RunWith(JUnit4.class)
public class DeliverStoriesTest{

    @Test
    public void testFindTrackerIds(){
        DeliverStories tracker = new DeliverStories(4);
        int[] expected = {1,2,3};
        int[] actual = tracker.findTrackerIDs("[fixes #1 #2 #3]");
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testFindTrackerIdsCase(){
        DeliverStories tracker = new DeliverStories(4);
        int[] expected = {1,2,3};
        int[] actual = tracker.findTrackerIDs("[Fixes #1 #2 #3]");
        assertArrayEquals(expected, actual);
    }


}
