package org.opencalaccess.timer;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opencalaccess.timer.eo.AppTask;
import org.opencalaccess.timer.eo.AppTaskInstance;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXEC;

/**
 * The run() method here is the main loop for the tasking process.
 *
 */
public class AppTasker extends Thread {

	@SuppressWarnings("unused")
	private boolean verbose = System.getProperty(U.APP_TASKER_VERBOSE, "false").equalsIgnoreCase("true");

	File abortMarker = new File(System.getProperty(U.APP_TASKER_ABORT_FILE, "/tmp/abortDJ"));

	private boolean abortLaunchProp = System.getProperty(U.APP_TASKER_ABORT_LAUNCH, "false").equalsIgnoreCase("true");
	private boolean abortLaunchMark = abortMarker.exists();

	public static void initialize() {

		String runAppTasks = System.getProperty(U.APP_TASKER_FEATURE_ENABLED, "false");

		String appTaskStartup =   System.getProperty(U.APP_TASKER_CHECK_STARTUP_WAIT_SECONDS, "30");
		String appTaskWait =      System.getProperty(U.APP_TASKER_CHECK_INTERVAL_SECONDS, "60");

		U.log(U.APP_TASKER_FEATURE_ENABLED, " = ", runAppTasks);

		if ("YES".equalsIgnoreCase(runAppTasks) || "TRUE".equalsIgnoreCase(runAppTasks)) {

			(new AppTasker(appTaskStartup, appTaskWait)).start();

			(new AppScheduler("60")).start();
		}
	}

	public AppTasker(String startupWait, String intervalWait) {

		U.log("tasker startupWait = ", startupWait, " seconds");
		U.log("tasker intervalWait = ", intervalWait, " seconds");

		try {
			this.startupWait = Integer.valueOf(startupWait);
		} catch (NumberFormatException e) {
			U.err("could not understand a startupWait = \"", startupWait, ", setting to 10");
			this.startupWait = 10;
		}

		try {
			this.intervalWait = Integer.valueOf(intervalWait);
		} catch (NumberFormatException e) {
			U.err("could not understand an intervalWait = \"", intervalWait, ", setting to 60");
			this.intervalWait = 60;
		}
	}

	private int startupWait;

	private int intervalWait;

	private static final ExecutorService exec = Executors.newSingleThreadExecutor();

	/**
	 * This run() method drives the task submission process.
	 *
	 * The tasksToBeLaunched dictionary ensures that a task does not get re-submitted if it is already
	 * queued or running.
	 */
	public void run() {

		wait(startupWait);

		EOEditingContext ec = ERXEC.newEditingContext();

		while (true) {

			NSArray<AppTask> tasks = AppTask.fetchAllAppTasks(ec);

			for (AppTask task : tasks) {

				if (task.isQueued()) {

					U.log("launch of task ", task.taskName(), " is scheduled to start now.");

					AppTaskInstance instance = task.latestInstance();

					AppTasked tasked = new AppTasked(instance);

					long now = U.now();

					if (abortLaunchProp) { U.log("aborting run via Property"); }
					if (abortLaunchMark) { U.log("aborting run via marker file ", abortMarker.getPath()); }

					if (abortLaunchProp || abortLaunchMark) {
						instance.setStartTime(now);
						instance.setEndTime(now);
						instance.setResult(0);
						instance.setNote("aborted/testing");

					} else {
						exec.submit(tasked);
						instance.setExecTime(now);
					}
					ec.saveChanges();
				}
			}

			// Cycle through the Future values and set the result in the finished instances.
			//
			checkAndClearFinishedTasks(ec);

			wait(intervalWait);
		}
	}

	/**
	 * Run the task manually, now, without waiting for the task loop.
	 *
	 * This is called from an instance method in the AppTasksPage class, for when a user hits "run".
	 */
	public static void runTaskNow(AppTask task) {

		EOEditingContext ec = ERXEC.newEditingContext();

		AppTask localTask = task.localInstanceIn(ec);

		U.log("launch of task ", localTask.taskName(), " is demanded to start now.");

		long now = U.now();

		AppTaskInstance instance = AppTaskInstance.createAppTaskInstance(ec, now, localTask);

		AppTasked tasked = new AppTasked(instance);

		instance.setExecTime(now);

		ec.saveChanges();

		tasked.run();

		instance.setEndTime(U.now());

		ec.saveChanges();
	}

	private void wait(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void checkAndClearFinishedTasks(EOEditingContext ec) {

//		for (String taskName : tasksInExecutor.keySet()) {
//
//			if (tasksInExecutor.get(taskName).isDone()) {
//
//				AppTask task = AppTask.fetchRequiredAppTask(ec, AppTask.TASK_NAME_KEY, taskName);
//
//				NSArray<AppTaskInstance> running = AppTaskInstance.fetchAppTaskInstances(
//						ec,
//						ERXQ.and(
//								AppTaskInstance.TASK.is(task),
//								AppTaskInstance.END_TIME.isNull()),
//						null);
//
//				for (@SuppressWarnings("unused") AppTaskInstance instance : running) {
//
//				}
//			}
//		}
	}
}
