package org.opencalaccess.timer.ui;

import org.opencalaccess.timer.eo.AppTask;
import org.opencalaccess.timer.eo.AppTaskInstance;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;

import er.extensions.components.ERXComponent;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXQ;

public class AppTaskPage extends ERXComponent {

	private static final long serialVersionUID = -212964592642240960L;

	public AppTaskPage(WOContext context) {
        super(context);
    }

	EOEditingContext ec = ERXEC.newEditingContext();

	public AppTask task;

	public boolean onlyShowNonZero = false;

	public void setTask(AppTask nextFetch) {
		task = nextFetch.localInstanceIn(ec);
	}

	public NSArray<AppTaskInstance> instances() {
		if (task == null) {
			if (onlyShowNonZero) {
				return AppTaskInstance.fetchAppTaskInstances(
						ec,
						ERXQ.or(
								AppTaskInstance.RESULT.isNull(),
								AppTaskInstance.RESULT.greaterThan(1)),
						AppTaskInstance.QUEUE_TIME.descs());
			} else {
				return AppTaskInstance.fetchAllAppTaskInstances(ec, AppTaskInstance.QUEUE_TIME.descs());
			}
		} else {
			return EOSortOrdering.sortedArrayUsingKeyOrderArray(task.instances(), AppTaskInstance.QUEUE_TIME.descs());
		}
	}

	public AppTaskInstance instance;
}