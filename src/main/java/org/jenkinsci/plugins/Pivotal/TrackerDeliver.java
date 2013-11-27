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

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.DataBoundConstructor;

import net.sf.json.JSONObject;

import java.util.logging.Logger;

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

    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        LOGGER.info("Hello there! Your ProjectID is " + projectId);
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
