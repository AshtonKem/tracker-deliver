package com.pivotal.tracker;

import java.util.HashMap;
import java.util.Set;

import hudson.model.Action;
import hudson.model.AbstractBuild;

public class TrackerAction implements Action {

    private HashMap<Integer, Boolean> stories;

    public TrackerAction(HashMap<Integer, Boolean> stories) {
        this.stories = stories;
        stories.put(1, true);
        stories.put(2, true);
        stories.put(3, true);
    }

    public boolean deliveredStory(int storyId) {
        return stories.get(storyId);
    }

    public String getText() {
        StringBuilder builder = new StringBuilder();
        builder.append("Stories completed</br>");
        builder.append("<ul>");
        for (Integer story : stories.keySet()) {
            builder.append("<li>");
            builder.append(story);
            builder.append("</li>");
        }
        builder.append("</ul>");
        return builder.toString();
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
