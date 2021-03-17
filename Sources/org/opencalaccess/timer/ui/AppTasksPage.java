package org.opencalaccess.timer.ui;

import org.opencalaccess.timer.AppTasker;
import org.opencalaccess.timer.U;
import org.opencalaccess.timer.eo.AppTask;
import org.opencalaccess.timer.eo.AppTaskInstance;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.components.ERXComponent;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXQ;

public class AppTasksPage extends ERXComponent {

	private static final long serialVersionUID = 4964952287321699706L;

	public AppTasksPage(WOContext context) {
        super(context);
    }

	EOEditingContext ec = ERXEC.newEditingContext();

	public String digest;

	public void setDigest(String digest) {

		if (digest == null) {
			throw new IllegalArgumentException("No digest included in request");
		}

		this.digest = digest;

		EOEnterpriseObject eo = EOUtilities.objectMatchingKeyAndValue(ec, "UserInvite", "digest", digest);

		if (eo != null &&
				eo.valueForKeyPath("userRight.appName").equals("appTasks") &&
				eo.valueForKeyPath("userRight.appRight").equals("WRITE_DATA")) {

			U.log("found INVITE for app: ", appName, ", digest: ", digest);
		} else {
			System.err.println("found NO invite for app: " + appName + ", digest: " + digest);
			throw new IllegalArgumentException("No authenticated user for digest = \"" + digest + "\"");
		}
	}

	public String appName;

	public NSArray<AppTask> tasks() {

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

		NSMutableArray<AppTask> queued = new NSMutableArray<>();
		NSMutableArray<AppTask> running = new NSMutableArray<>();
		NSMutableArray<AppTask> others = new NSMutableArray<>();

		for (AppTask nextTask : found) {

			if (nextTask.latestInstance() == null) {
				others.add(nextTask);
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

		sorted.addAll(
				EOSortOrdering.sortedArrayUsingKeyOrderArray(
						running,
						AppTask.LATEST_INSTANCE.dot(AppTaskInstance.START_TIME).descs()));

		sorted.addAll(
				EOSortOrdering.sortedArrayUsingKeyOrderArray(
						queued,
						AppTask.LATEST_INSTANCE.dot(AppTaskInstance.START_TIME).descs()));

		sorted.addAll(
				EOSortOrdering.sortedArrayUsingKeyOrderArray(
						others,
						AppTask.LATEST_INSTANCE.dot(AppTaskInstance.START_TIME).descs()));

		return sorted.immutableClone();
	}

	public AppTask task;

	public String appFilterName;

	/**
	 * Flip the active/inactive state of the task, which may lead to tasks being stsrted.
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
	 * A launch is scheduled for later but I want a launch now, with the next job check,
	 * so this will do that.
	 */
	public WOActionResults pushTaskLaunch() {
		U.log("LAUNCH of task \"", task.appName(), ":", task.taskName(), " requested");
		AppTasker.addTaskForLaunch(task);
		return this.context().page();
	}

	/**
	 * Enabled in local mode, start an immediate run. This works even if the task check
	 * is not running.
	 */
	public WOActionResults runTaskNow() {
		U.log("IMMEDIATE RUN of task \"", task.appName(), ":", task.taskName(), " beginning");
		AppTasker.runTaskNow(task);
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

	public WOActionResults dequeueTask() {

		AppTaskInstance instance = AppTaskInstance.fetchRequiredAppTaskInstance(
				ec,
				ERXQ.and(
						AppTaskInstance.TASK.is(task),
						AppTaskInstance.QUEUE_TIME.isNotNull(),
						AppTaskInstance.START_TIME.isNull(),
						AppTaskInstance.END_TIME.isNull()));

		U.log("Dequeing task: -> ", task.taskName());

		instance.setResult(0);
		instance.setNote("Dequeued");
		instance.setEndTime(U.now());
		ec.saveChanges();

		return this.context().page();
	}

	public WOActionResults abortTask() {

		AppTaskInstance instance = AppTaskInstance.fetchRequiredAppTaskInstance(
				ec,
				ERXQ.and(
						AppTaskInstance.TASK.is(task),
						AppTaskInstance.QUEUE_TIME.isNotNull(),
						AppTaskInstance.START_TIME.isNotNull(),
						AppTaskInstance.END_TIME.isNull()));

		U.log("Aborting task: -> ", task.taskName());

		instance.setResult(2);
		instance.setNote("Manual abort");
		instance.setEndTime(U.now());
		ec.saveChanges();

		long diff = (instance.endTime() - instance.startTime()) / (60 * 1000L);

		U.log("Aborted task run time = ", diff, " minutes");

		return this.context().page();
	}

	/**
	 * The task is active, but we only want to know that if it is not queued or running right now.
	 */
	public boolean taskIsActive() {
		boolean quiet = task.isQuiet();
		boolean active = task.active() == 1;
		return quiet && active;
	}

	/**
	 * The task is inactive, but we only want to know that if it is not queued or running right now.
	 */
	public boolean taskIsInactive() {
		boolean quiet = task.isQuiet();
		boolean active = task.active() == 1;
		return quiet && ! active;
	}

	public boolean isLocalMode() {
		return System.getProperty("FeatureLocalModeEnabled", "false").equals("true");
	}
}
