package com.pivotal.tracker;

import jenkins.model.Jenkins;

import hudson.util.FormValidation;
import hudson.ProxyConfiguration;

import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.HashSet;
import java.util.HashMap;



class TrackerInterface {
    public final static String TRACKER_URL = "https://www.pivotaltracker.com";
    private final static Logger LOGGER = Logger.getLogger(TrackerInterface.class.getName());

    public static FormValidation doTestToken(String token) {
        HttpClient client = getHttpClient();
        String url = TRACKER_URL + "/services/v5/me?fields=name";
        GetMethod get = new GetMethod(url);
        get.addRequestHeader("X-TrackerToken", token);
        get.addRequestHeader("Content-Type", "application/json");
        try {
            int responseCode = client.executeMethod(get);
            String response = get.getResponseBodyAsString();
            if (responseCode != 200) {
                return FormValidation.error("We couldn't authenticate this token");
            }

            JSONObject user = JSONObject.fromObject(response);
            String name = user.getString("name");
            return FormValidation.ok("This Token belongs to " + name + ".");
        } catch (IOException e) {
            return FormValidation.error("We couldn't authenticate this token");
        }
    }


    private static HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        if (Jenkins.getInstance() != null) {
            ProxyConfiguration proxy = Jenkins.getInstance().proxy;
            if (proxy != null) {
                client.getHostConfiguration().setProxy(proxy.name, proxy.port);
            }
        }
        return client;
    }

    public static HashSet<Integer> finishedTrackerStories(int projectId, String token) throws IOException{
        HashSet<Integer> ids = new HashSet<Integer>();
        HttpClient client = getHttpClient();
        String url = TRACKER_URL + "/services/v5/projects/" + projectId + "/stories?with_state=finished";
        GetMethod get = new GetMethod(url);
        get.addRequestHeader("X-TrackerToken", token);
        get.addRequestHeader("Content-Type", "application/json");
        int responseCode = client.executeMethod(get);
        if (responseCode != 200) {
            // Failure case. Unauthorized or similar.
            return ids;
        }
        String response = get.getResponseBodyAsString();
        JSONArray stories = JSONArray.fromObject(response);
        for (int i = 0; i < stories.size(); i++) {
            JSONObject story = (JSONObject)stories.get(i);
            int id = story.getInt("id");
            ids.add(id);
        }
        return ids;
    }

    public static boolean deliverStory(int projectId, String token, int id) throws IOException{
        LOGGER.info("Trying to deliver " + id);
        HttpClient client = getHttpClient();
        String url = TRACKER_URL + "/services/v5/projects/" + projectId + "/stories/" + id + "?current_state=delivered";
        PutMethod put = new PutMethod(url);
        put.addRequestHeader("X-TrackerToken", token);
        put.addRequestHeader("Content-Type", "application/json");
        return client.executeMethod(put) == 200;
    }
}
