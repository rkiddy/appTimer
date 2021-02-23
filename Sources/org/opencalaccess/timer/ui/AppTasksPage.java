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
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;

import er.extensions.components.ERXComponent;
import er.extensions.eof.ERXEC;

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

		return EOSortOrdering.sortedArrayUsingKeyOrderArray(
				found,
				AppTask.LATEST_INSTANCE.dot(AppTaskInstance.END_TIME).descs());
	}

	public AppTask task;

	public String appFilterName;

	public boolean displayTasksPerfs = false;
	public boolean displayTasksDefns = true;

	public WOActionResults showTaskPerfs() {
		displayTasksPerfs = true;
		displayTasksDefns = false;
		return this.context().page();
	}

	public WOActionResults showTaskDefns() {
		displayTasksPerfs = false;
		displayTasksDefns = true;
		return this.context().page();
	}

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

	public WOActionResults pushTaskLaunch() {
		U.log("LAUNCH of task \"", task.appName(), ":", task.taskName(), " requested");
		AppTasker.addTaskForLaunch(task);
		return this.context().page();
	}

	public WOActionResults createNewTask() {
		AppTaskCreatePage nextPage = pageWithName(AppTaskCreatePage.class);
		nextPage.digest = digest;
		return nextPage;
	}
}