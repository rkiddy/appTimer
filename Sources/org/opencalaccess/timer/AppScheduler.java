package org.opencalaccess.timer;

import org.opencalaccess.timer.eo.AppTask;
import org.opencalaccess.timer.eo.AppTaskInstance;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.eof.ERXEC;

/**
 * Check for the last successful run of a task and, if it has been longer ago than is desired and
 * other conditions are met, put the task instance on the queue.
 */
public class AppScheduler extends Thread {

	private int databaseWait;

	public AppScheduler(String databaseWait) {

		U.log("scheduler databaseWait = ", databaseWait, " seconds");

		try {
			this.databaseWait = Integer.valueOf(databaseWait);
		} catch (NumberFormatException e) {
			U.err("could not understand a databaseWait = \"", databaseWait, ", setting to 60");
			this.databaseWait = 60;
		}
	}
	
	private EOEditingContext ec = ERXEC.newEditingContext();

	public void run() {

		while (true) {

			wait(databaseWait);

			NSMutableArray<String> runnables = new NSMutableArray<>();

			NSArray<AppTask> tasks = AppTask.fetchAllActiveTasks(ec);

			for (AppTask task : tasks) {

				// if take is not quiet, then skip this one.
				//
				if ( ! task.isQuiet()) {
					if (AppTimer.verbose()) {
						U.log("Task ", task.taskName(), " is not quiet. Not scheduling.");
					}
					continue;
				}

				if (task.isBouncing()) {
					if (AppTimer.verbose()) {
						U.log("Task ", task.taskName(), " is BOUNCING. Not scheduling.");
					}
					continue;
				}

				AppTaskInstance lastSuccessful = task.latestSuccessfulInstance();

				if (lastSuccessful == null) {

					U.log("for: ", task, " no previous instances, so RUN");

					runnables.add(task.taskName());

					continue;
				}

				/*
				 * Handles checking for intervals: MINUTELY, HOURLY, TWICE_DAILY and DAILY.
				 */
				if (U.intervalTimes.containsKey(task.intervalName())) {

					String interval = task.intervalName();

					long interval_diff = U.intervalTimes.get(interval);

					long now = System.currentTimeMillis();
					long then = lastSuccessful.endTime();

					long diff = now - then;

					if (diff > interval_diff) {

						runnables.add(task.taskName());

						U.log("for: ", task, " testing ", task.intervalName(), " (", interval_diff, "), diff = ", diff, ", so RUN");
					} else {
						if (AppTimer.verbose()) {
							U.log("for: ", task, " testing ", task.intervalName(), " (", interval_diff, "), diff = ", diff, ", so DO NOT run");
						}
					}
				}

				/*
				 * Handles checking for interval: MINUTES_###.
				 *
				 * It checks the number after the "MINUTES_" and calculates based on that.
				 */
				if (task.intervalName().startsWith(U.APP_TASK_INTERVAL_PREFIX_MINUTES)) {

					int minutes = Integer.valueOf(task.intervalName().substring(U.APP_TASK_INTERVAL_PREFIX_MINUTES.length()));

					int interval = minutes * (int) U.one_minute;

					long now = System.currentTimeMillis();
					long then = lastSuccessful.endTime();

					long diff = now - then;

					if (diff > interval) {

						runnables.add(task.taskName());

						U.log("for: ", task, " testing against ", minutes, " MINUTES (", interval, "), diff = ", diff, ", greater, so RUN");
					} else {
						if (AppTimer.verbose()) {
							U.log("for: ", task, " testing against ", minutes, " MINUTES (", interval, "), diff = ", diff, ", not greater, so DO NOT run");
						}
					}
				}

				/*
				 * Handles checking for interval: HOURS_###.
				 *
				 * It checks the number after the "HOURS_" and calculates based on that.
				 */
				if (task.intervalName().startsWith(U.APP_TASK_INTERVAL_PREFIX_HOURS)) {

					int hours = Integer.valueOf(task.intervalName().substring(U.APP_TASK_INTERVAL_PREFIX_HOURS.length()));

					long interval = task.intervalDuration(task.intervalName());

					long now = System.currentTimeMillis();
					long then = lastSuccessful.endTime();

					long diff = now - then;
				
					if (diff > interval) {

						runnables.add(task.taskName());

						U.log("for: ", task, " testing against ", hours, " HOURS (", interval, "), diff = ", diff, ", greater, so RUN");
					} else {
						if (AppTimer.verbose()) {
							U.log("for: ", task, " testing against ", hours, " HOURS (", interval, "), diff = ", diff, ", greater, so DO NOT run");
						}
					}
				}
			}

			if (runnables.size() > 0) {
				U.log("Active tasks # ", tasks.size(), ", schedule-activated # ", runnables.size());
			}

			for (String runnable : runnables) {

				AppTask task = AppTask.fetchRequiredAppTask(ec, AppTask.TASK_NAME_KEY, runnable);

				AppTaskInstance.createAppTaskInstance(ec, U.now(), task);
			}

			ec.saveChanges();
		}
	}

	private void wait(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
