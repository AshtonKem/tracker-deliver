package com.pivotal.tracker;

import hudson.Launcher;
import hudson.Extension;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.BuildStepDescriptor;
import jenkins.model.Jenkins;
import hudson.ProxyConfiguration;

import hudson.scm.ChangeLogSet;

import hudson.util.FormValidation;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.DataBoundConstructor;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

import hudson.plugins.git.GitChangeSet;

import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.HashSet;
import java.io.IOException;


import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;



/**
 * Created with IntelliJ IDEA.
 * User: pivotal
 * Date: 11/27/13
 * Time: 10:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class DeliverStories extends Notifier {
    private final static Logger LOGGER = Logger.getLogger(DeliverStories.class.getName());
    private final static String TRACKER_URL = "http://localhost:3000"; //Testing only!
    private int projectId;

    @DataBoundConstructor
    public DeliverStories(final int projectId) {
        super();
        this.projectId = projectId;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    private String[] getCommitMessages(ChangeLogSet<? extends ChangeLogSet.Entry> changeSet) {
        if (changeSet.getKind() == "git") {
            Object[] changes = changeSet.getItems();
            String[] messages = new String[changes.length];
            for (int i = 0; i < changes.length; i++) {
                GitChangeSet parsed = (GitChangeSet) changes[i];
                messages[i] = parsed.getMsg();
            }
            return messages;
        } else {
            return new String[0];
        }
    }

    private HashSet<Integer> finishedTrackerStories() throws IOException{
        HttpClient client = getHttpClient();
        String url = TRACKER_URL + "/services/v5/projects/" + projectId + "/stories?with_state=finished";
        GetMethod get = new GetMethod(url);
        get.addRequestHeader("X-TrackerToken", getDescriptor().getTrackerToken());
        get.addRequestHeader("Content-Type", "application/json");
        int responseCode = client.executeMethod(get);
        String response = get.getResponseBodyAsString();
        JSONArray stories = JSONArray.fromObject(response);
        HashSet<Integer> ids = new HashSet<Integer>();
        for (int i = 0; i < stories.size(); i++) {
            JSONObject story = (JSONObject)stories.get(i);
            int id = story.getInt("id");
            ids.add(id);
        }
        return ids;
    }

    private HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        if (Jenkins.getInstance() != null) {
            ProxyConfiguration proxy = Jenkins.getInstance().proxy;
            if (proxy != null) {
                client.getHostConfiguration().setProxy(proxy.name, proxy.port);
            }
        }
        return client;
    }





    public int[] findTrackerIDs(String message) {
        String group = "\\[(fix|finish|complet)(es|ed)?.*\\]";
        ArrayList<String> unchangedIds = new ArrayList<String>();
        Matcher brackets = Pattern.compile(group, Pattern.CASE_INSENSITIVE).matcher(message);
        while (brackets.find()) {
            String match = brackets.group(0);
            Matcher matcherIDs = Pattern.compile("#\\d+").matcher(match);
            while (matcherIDs.find()) {
                unchangedIds.add(matcherIDs.group(0));
            }
        }
        int[] ids = new int[unchangedIds.size()];
        for (int i = 0; i < unchangedIds.size(); i++) {
            ids[i] = Integer.parseInt(unchangedIds.get(i).substring(1));
        }
        return ids;
    }

    private void deliverStory(int id) throws IOException{
        LOGGER.info("Trying to deliver " + id);
        HttpClient client = getHttpClient();
        String url = TRACKER_URL + "/services/v5/projects/" + projectId + "/stories/" + id + "?current_state=delivered";
        PutMethod put = new PutMethod(url);
        put.addRequestHeader("X-TrackerToken", getDescriptor().getTrackerToken());
        put.addRequestHeader("Content-Type", "application/json");
        client.executeMethod(put);
    }

    private boolean shouldExecute(AbstractBuild<?, ?> build) {
        return build.getResult().equals(Result.SUCCESS);
    }

    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException{
        if (shouldExecute(build)) {
            HashSet<Integer> finishedStories = finishedTrackerStories();
            for (String s : getCommitMessages(build.getChangeSet())) {
                for (int id : findTrackerIDs(s)) {
                    if (finishedStories.contains(id)) {
                        deliverStory(id);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    public int getProjectId() {
        return projectId;
    }


    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private String token;

        public String getTrackerToken() {
            return token;
        }

        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Deliver Stories";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            JSONObject trackerSettings = formData.getJSONObject("deliver-stories");
            token = trackerSettings.getString("trackerToken");
            save();
            return super.configure(req,formData);
        }
    }
}
