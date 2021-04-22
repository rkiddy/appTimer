package org.opencalaccess.timer.ui;

import java.util.Map;

import org.opencalaccess.timer.AppTasker;
import org.opencalaccess.timer.U;
import org.opencalaccess.timer.eo.AppTask;
import org.opencalaccess.timer.eo.AppTaskInstance;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.components.ERXComponent;
import er.extensions.eof.ERXEC;

/**
 * This page manages the AppTask via the AppTaskInstance objects that it puts
 * into the database.
 *
 * It has no direct interaction with the AppTask queue mechanism.
 *
 * The states for a task are:
 *
 * 1) Queued
 * 2) Running
 * 3) Active
 * 4) Inactive
 *
 * Active and Inactive refer to whether a task will be picked up by the scheduler. A task
 * may be inactive and be run manually whenever desired.
 *
 * @author ray
 */
public class AppTasksPage extends ERXComponent {

	private static final long serialVersionUID = 4964952287321699706L;

	public AppTasksPage(WOContext context) {
        super(context);
    }

	public boolean userCanRead() {
		return true;
	}

	public boolean userCanWrite() {
		return (boolean)session().valueForKey("userCanWrite");
	}

	EOEditingContext ec = ERXEC.newEditingContext();

	public String digest;

	public void setDigest(String digest) {

		if (digest == null) {
			return;
		}

		this.digest = digest;

		EOEnterpriseObject eo = EOUtilities.objectMatchingKeyAndValue(ec, "UserInvite", "digest", digest);

		if (eo != null &&
				eo.valueForKeyPath("userRight.appName").equals("appTasks") &&
				eo.valueForKeyPath("userRight.appRight").equals("WRITE_DATA")) {

			U.log("found INVITE for app: ", appName, ", digest: ", digest);

			session().takeValueForKey(eo, "authenticatedInvite");

		} else {
			System.err.println("found NO invite for app: " + appName + ", digest: " + digest);
			throw new IllegalArgumentException("No authenticated user for digest = \"" + digest + "\"");
		}
	}

	public String appName;

	public boolean sortTasksDescending = true;

	public WOActionResults displayTasksSoonestToRun() {
		sortTasksDescending = false;
		return this.context().page();
	}

	public boolean displayingTasksOldestRan() {
		return sortTasksDescending;
	}

	public WOActionResults displayTasksOldestRan() {
		sortTasksDescending = true;
		return this.context().page();
	}

	/*
	 * For display in the page component.
	 */
	public NSArray<AppTask> tasks() {

		NSArray<AppTask> found = allTasks();

		NSMutableArray<AppTask> queued = new NSMutableArray<>();
		NSMutableArray<AppTask> running = new NSMutableArray<>();
		NSMutableArray<AppTask> others = new NSMutableArray<>();

		/*
		 * Tasks are being put into one of three buckets for sorting qurposes.
		 *
		 * We want to see running task(s) on top, then queued tasks, and then the others.
		 */
		for (AppTask nextTask : found) {

			if (nextTask.latestInstance() == null) {
				others.add(nextTask);
				continue;
			}

			if (nextTask.isExeced()) {
				queued.add(nextTask);
				continue;
			}

			if (nextTask.isQueued()) {
				queued.add(nextTask);
				continue;
			}

			if (nextTask.isRunning()) {
				running.add(nextTask);
				continue;
			}

			others.add(nextTask);
		}

		NSMutableArray<AppTask> sorted = new NSMutableArray<>();

		if (sortTasksDescending) {

			sorted.addAll(
					EOSortOrdering.sortedArrayUsingKeyOrderArray(
							running,
							AppTask.LATEST_INSTANCE.dot(AppTaskInstance.START_TIME).descs()));

			sorted.addAll(
					EOSortOrdering.sortedArrayUsingKeyOrderArray(
							queued,
							AppTask.LATEST_INSTANCE.dot(AppTaskInstance.QUEUE_TIME).descs()));

			sorted.addAll(
					EOSortOrdering.sortedArrayUsingKeyOrderArray(
							others,
							AppTask.LATEST_INSTANCE.dot(AppTaskInstance.END_TIME).descs()));
		} else {

			sorted.addAll(
					EOSortOrdering.sortedArrayUsingKeyOrderArray(
							running,
							AppTask.LATEST_INSTANCE.dot(AppTaskInstance.START_TIME).ascs()));

			sorted.addAll(
					EOSortOrdering.sortedArrayUsingKeyOrderArray(
							queued,
							AppTask.LATEST_INSTANCE.dot(AppTaskInstance.QUEUE_TIME).ascs()));

			NSMutableArray<AppTask> active = new NSMutableArray<>();
			NSMutableArray<AppTask> inactive = new NSMutableArray<>();

			NSArray<AppTask> sortedOthers = EOSortOrdering.sortedArrayUsingKeyOrderArray(
					others,
					AppTask.LATEST_INSTANCE.dot(AppTaskInstance.END_TIME).ascs());

			for (AppTask aTask : sortedOthers) {
				if (aTask.active() == 1) {
					active.add(aTask);
				} else {
					inactive.add(aTask);
				}
			}

			sorted.addAll(active);
			sorted.addAll(inactive);
		}

		return sorted.immutableClone();
	}

	/*
	 * For display in the page component, interated into by the tasks() method.
	 */
	public AppTask task;

	/*
	 * If we are filtering on the application name, such as "dailyJail", it is stored here and
	 * used by the tasks method as a filter.
	 */
	public String appFilterName;

	/**
	 * Flip the active/inactive state of the task, which may lead to tasks being started.
	 */
	public WOActionResults flipEnableTask() {
		if (task.active() == 0) {
			task.setActive(1);
			U.log("task \"", task.appName(), ":", task.taskName(), " set to ACTIVE");
		} else {
			task.setActive(0);
			U.log("task \"", task.appName(), ":", task.taskName(), " set to INACTIVE");
		}
		ec.saveChanges();
		return this.context().page();
	}

	/**
	 * Go to AppTaskCreatePage for creating a new task.
	 */
	public WOActionResults createNewTask() {
		AppTaskCreatePage nextPage = pageWithName(AppTaskCreatePage.class);
		nextPage.digest = digest;
		return nextPage;
	}

	/**
	 * A launch is scheduled for later but I want a launch now, with the next job check,
	 * so this will do that.
	 */
	public WOActionResults enqueueTask() {

		AppTaskInstance.createAppTaskInstance(ec, U.now(), task);

		ec.saveChanges();

		U.log("task \"", task.appName(), ":", task.taskName(), " is QUEUED to run");

		return this.context().page();
	}

	public WOActionResults dequeueTask() {

		task.setToQuiet(0, "dequeued, NOT run");

		ec.saveChanges();

		U.log("task \"", task.appName(), ":", task.taskName(), " is DEQUEUED and will not run");

		return this.context().page();
	}

	public WOActionResults runTask() {

		AppTasker.runTaskNow(task);

		return this.context().page();
	}

	public WOActionResults abortTask() {

		task.setToQuiet(2, "aborted");

		ec.saveChanges();

		U.log("task \"", task.appName(), ":", task.taskName(), " is ABORTED and is stopped");

		return this.context().page();
	}

	public boolean taskIsQueued() {
		return task.isQueued();
	}

	public boolean taskIsRunning() {
		return task.isRunning();
	}

	public boolean isLocalMode() {
		return System.getProperty("FeatureLocalModeEnabled", "false").equals("true");
	}

	private NSArray<AppTask> allTasks() {

		NSArray<AppTask> found = null;

		if (appFilterName == null) {

			found = AppTask.fetchAllAppTasks(
					ec,
					null);
		} else {

			found = AppTask.fetchAppTasks(
					ec,
					AppTask.APP_NAME.is(appFilterName),
					null);
		}

		return found;
	}

	public NSArray<String> taskThreads() {

		NSArray<String> taskClasses = AppTask.fetchAllActiveTasks(ec).valueForKey(AppTask.CLASS_NAME);

		Map<Thread,StackTraceElement[]> threads = Thread.getAllStackTraces();

		NSMutableSet<String> found = new NSMutableSet<>();

		for (Thread thread : threads.keySet()) {
			for (StackTraceElement elt : threads.get(thread)) {
				if (taskClasses.contains(elt.getClassName())) {
					found.add(elt.getClassName());
				}
			}
		}

		return new NSArray<String>(found);
	}

	public String taskThread;
}
