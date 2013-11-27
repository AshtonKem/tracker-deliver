package org.jenkinsci.plugins.trackerdeliver;

import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;

/**
 * Created with IntelliJ IDEA.
 * User: pivotal
 * Date: 11/27/13
 * Time: 10:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class TrackerDeliver extends Notifier {

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }


}
