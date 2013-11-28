package org.jenkinsci.plugins.Pivotal;

import hudson.Launcher;
import hudson.Extension;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.BuildStepDescriptor;

import hudson.scm.ChangeLogSet;

import hudson.util.FormValidation;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.DataBoundConstructor;

import net.sf.json.JSONObject;

import hudson.plugins.git.GitChangeSet;

import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: pivotal
 * Date: 11/27/13
 * Time: 10:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class TrackerDeliver extends Notifier {
    private final static Logger LOGGER = Logger.getLogger(TrackerDeliver.class.getName());
    private int projectId;

    @DataBoundConstructor
    public TrackerDeliver(final int projectId) {
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

    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        LOGGER.info("========================================================");
        for (String s : getCommitMessages(build.getChangeSet())) {
            LOGGER.info("IDs: " + findTrackerIDs(s));
        }
        LOGGER.info("========================================================");
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
            return "Tracker Deliver";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            JSONObject trackerSettings = formData.getJSONObject("tracker-deliver");
            token = trackerSettings.getString("trackerToken");
            save();
            return super.configure(req,formData);
        }
    }
}
