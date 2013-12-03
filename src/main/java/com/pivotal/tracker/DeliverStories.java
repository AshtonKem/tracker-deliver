package com.pivotal.tracker;

import com.pivotal.tracker.TrackerInterface;

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
import org.kohsuke.stapler.QueryParameter;

import net.sf.json.JSONObject;

import hudson.plugins.git.GitChangeSet;

import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.HashSet;
import java.io.IOException;


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

    private String getToken() {
        return getDescriptor().getTrackerToken();
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

    private boolean shouldExecute(AbstractBuild<?, ?> build) {
        return build.getResult().equals(Result.SUCCESS);
    }

    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException{
        if (shouldExecute(build)) {
            HashSet<Integer> finishedStories = finishedTrackerStories(projectId, getToken());
            for (String s : getCommitMessages(build.getChangeSet())) {
                for (int id : findTrackerIDs(s)) {
                    if (finishedStories.contains(id)) {
                        deliverStory(projectId, getToken(), id);
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

        public FormValidation doTestToken(@QueryParameter("trackerToken") final String token) {
            return TrackerInterface.doTestToken(token);
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
