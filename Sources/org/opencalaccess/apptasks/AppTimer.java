package org.opencalaccess.apptasks;

import org.opencalaccess.apptasks.eo.AppTask;
import org.opencalaccess.apptasks.eo.AppTaskInstance;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXFrameworkPrincipal;
import er.extensions.eof.ERXQ;

public class AppTimer extends ERXFrameworkPrincipal {

	protected static AppTimer sharedInstance;

	@SuppressWarnings("unchecked")
	public final static Class<? extends ERXFrameworkPrincipal> REQUIRES[] = new Class[] {};

	static {
		setUpFrameworkPrincipalClass(AppTimer.class);
	}

	public static AppTimer sharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = sharedInstance(AppTimer.class);
		}
		return sharedInstance;
	}

	@Override
	public void finishInitialization() {
		log.debug("AppTimer loaded");
	}

	public static NSDictionary<String,AppTask> tasksNeedingRun(EOEditingContext ec) {

		NSMutableDictionary<String,AppTask> runnables = new NSMutableDictionary<>();

		NSArray<AppTask> tasks = AppTask.fetchAppTasks(ec, AppTask.ACTIVE.is(1), null);

		for (AppTask task : tasks) {

			NSArray<AppTaskInstance> running = AppTaskInstance.fetchAppTaskInstances(
					ec,
					AppTaskInstance.END_TIME.isNull(),
					null);

			if (running.size() > 0) {
				continue;
			}

			NSArray<AppTaskInstance> lastSuccessful = AppTaskInstance.fetchAppTaskInstances(
					ec,
					ERXQ.and(AppTaskInstance.END_TIME.isNotNull(), AppTaskInstance.RESULT.is(0), AppTaskInstance.TASK.is(task)),
					AppTaskInstance.END_TIME.descs());

			if (lastSuccessful.isEmpty()) {

				U.log("for: ", task, " no previous instances, so RUN");

				runnables.put(task.fullName(), task);

			} else {

				AppTaskInstance lastRun = lastSuccessful.get(0);

				if (U.intervalTimes.containsKey(task.intervalName())) {

					String interval = task.intervalName();

					long interval_diff = U.intervalTimes.get(interval);

					long now = System.currentTimeMillis();
					long then = lastRun.endTime();

					long diff = now - then;

					if (diff > interval_diff) {

						runnables.put(task.fullName(), task);

						U.log("for: ",
								task,
								" testing " +
								task.intervalName(),
								" (",
								interval_diff,
								"), diff = ",
								diff,
								", so RUN");
					} else {
						U.log("for: ",
								task,
								" testing " +
								task.intervalName(),
								" (",
								interval_diff,
								"), diff = ",
								diff,
								", so DO NOT run");
					}
				}

				// TODO: This could be done via a lookup by matching with regex that ends in _NUM.
				//
				if (task.intervalName().startsWith(U.APP_TASK_INTERVAL_PREFIX_MINUTES)) {

					int minutes = Integer.valueOf(task.intervalName().substring(U.APP_TASK_INTERVAL_PREFIX_MINUTES.length()));

					int interval = minutes * (int)U.one_minute;

					long now = System.currentTimeMillis();
					long then = lastRun.endTime();

					long diff = now - then;

					if (diff > interval) {
						runnables.put(task.fullName(), task);
						U.log("for: ", task, " testing against ", minutes, " MINUTES (", interval, "), diff = ", diff, ", greater, so RUN");
					} else {
						U.log("for: ", task, " testing against ", minutes, " MINUTES (", interval, "), diff = ", diff, ", not greater, so DO NOT run");
					}
				}

				// TODO: This could be done via a lookup by matching with regex that ends in _NUM.
				//
				if (task.intervalName().startsWith(U.APP_TASK_INTERVAL_PREFIX_HOURS)) {

					int hours = Integer.valueOf(task.intervalName().substring(U.APP_TASK_INTERVAL_PREFIX_HOURS.length()));

					int interval = hours * (int)U.one_hour;

					long now = System.currentTimeMillis();
					long then = lastRun.endTime();

					long diff = now - then;

					if (diff > interval) {
						runnables.put(task.fullName(), task);
						U.log("for: ", task, " testing against ", hours, " HOURS (", interval, "), diff = ", diff, ", greater, so RUN");
					} else {
						U.log("for: ", task, " testing against ", hours, " HOURS (", interval, "), diff = ", diff, ", greater, so DO NOT run");
					}
				}
			}
		}

		return runnables.immutableClone();
	}
}
