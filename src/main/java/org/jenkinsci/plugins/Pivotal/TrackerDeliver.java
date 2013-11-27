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
    private String trackerToken;

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        LOGGER.info("Hello there!");
        return true;
    }

    @DataBoundConstructor
    public TrackerDeliver(final String trackerToken) {
        super();
        this.trackerToken = trackerToken;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private String token;

        public String getToken() {
            return token;
        }

        @Override
        public TrackerDeliver newInstance(StaplerRequest sr) {
            if (token == null) token = sr.getParameter("trackerToken");
            return new TrackerDeliver(token);
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Tracker Deliver";
        }

    }
}
