package com.pivotal.tracker;

import java.io.IOException;

class Story{
    private final int id;
    private final String name;
    private final String url;
    private final Status status;
    private final int projectId;
    private final String token;

    enum Status {
        UNSCHEDULED, UNSTARTED, STARTED,
        FINISHED, DELIVERED, REJECTED, ACCEPTED
    };


    public Story(int id, int projectId, String token, String name, String url, Status status)
    {
        this.id = id;
        this.projectId = projectId;
        this.token = token;
        this.name = name;
        this.url = url;
        this.status = status;
    }

    public String toString() {
        return "#" + id + " - <a href=\"" + url + "\">" + name + "</a>";
    }

    public boolean deliver() throws IOException{
        return TrackerInterface.deliverStory(this.projectId, this.token, this.id);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
