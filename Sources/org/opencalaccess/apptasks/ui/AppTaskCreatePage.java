package org.opencalaccess.apptasks.ui;

import org.opencalaccess.apptasks.U;
import org.opencalaccess.apptasks.eo.AppTask;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.components.ERXComponent;
import er.extensions.eof.ERXEC;

public class AppTaskCreatePage extends ERXComponent {

	private static final long serialVersionUID = -6080525782828766664L;

	public AppTaskCreatePage(WOContext context) {
        super(context);
    }

	EOEditingContext ec = ERXEC.newEditingContext();

	public String taskName;
	public String appName;
	public String className;
	public String methodName;
	public String methodParams;
	public String checkMethodName;

	public NSArray<String> availableIntervalNames = new NSArray<>(
			U.APP_TASK_INTERVAL_DAILY,
			U.APP_TASK_INTERVAL_HOURLY,
			U.APP_TASK_INTERVAL_MINUTELY,
			U.APP_TASK_INTERVAL_TWICE_DAILY
			);

	public NSArray<String> availableIntervalPrefixes = new NSArray<>(
			U.APP_TASK_INTERVAL_PREFIX_MINUTES,
			U.APP_TASK_INTERVAL_PREFIX_HOURS
			);

	public String anIntervalName;
	public String anIntervalPrefix;

	public String selectedIntervalName;
	public String selectedIntervalPrefix;

	public String intervalNum;

	private String intervalName;

	public String digest;

	public WOActionResults createTask() {

		if (selectedIntervalName != null && ! selectedIntervalName.equals("none")) {
			intervalName = selectedIntervalName;
		} else {
			intervalName = selectedIntervalPrefix + intervalNum;
		}

		AppTask task = AppTask.createAppTask(
				ec,
				0,
				appName,
				className,
				intervalName,
				methodName,
				taskName);

		if (methodParams != null) {
			task.setMethodParams(methodParams);
		}

		if (checkMethodName != null) {
			task.setCheckName(checkMethodName);
		}

		ec.saveChanges();

		return pageWithName(AppTasksPage.class);
	}
}
