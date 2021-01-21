package org.opencalaccess.apptasks.ui;

import org.opencalaccess.apptasks.eo.AppTask;
import org.opencalaccess.apptasks.eo.AppTaskInstance;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.components.ERXComponent;
import er.extensions.eof.ERXEC;

public class AppTaskPage extends ERXComponent {

	private static final long serialVersionUID = -212964592642240960L;

	public AppTaskPage(WOContext context) {
        super(context);
    }

	EOEditingContext ec = ERXEC.newEditingContext();

	public AppTask task;

	public void setTask(AppTask nextFetch) {
		task = nextFetch.localInstanceIn(ec);
	}

	public NSArray<AppTaskInstance> instances() {
		if (task == null) {
			return AppTaskInstance.fetchAllAppTaskInstances(ec, AppTaskInstance.START_TIME.descs());
		} else {
			return task.instances();
		}
	}

	public AppTaskInstance instance;
}