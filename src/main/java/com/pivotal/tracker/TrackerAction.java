package com.pivotal.tracker;

import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import hudson.model.Action;
import hudson.model.AbstractBuild;

import com.pivotal.tracker.Story;
import java.util.logging.Logger;

public class TrackerAction implements Action {

    private HashMap<Story, Boolean> stories;
    private final static Logger LOGGER = Logger.getLogger(TrackerInterface.class.getName());

    public TrackerAction(HashMap<Story, Boolean> stories) {
        this.stories = stories;
    }

    public Set<Story> deliveredStories() {
        Set<Story> deliveredStories = new HashSet<Story>();
        LOGGER.info("Hello");
        for (Story s : stories.keySet()) {
            if (stories.get(s)) {
                deliveredStories.add(s);
                LOGGER.info("Story " + s.getId() + " is considered delivered");
            } else {
                LOGGER.info("Story " + s.getId() + " is considered unfinished");
            }
        }
        return deliveredStories;
    }


    public String getDisplayName() {
        return "Delivered Stories";
    }

    public String getIconFileName() {
        // We only want the floater Jelly stuff for now.
        return null;
    }

    public String getUrlName() {
        return "";
    }
}
