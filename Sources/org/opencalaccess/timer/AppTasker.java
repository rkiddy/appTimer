package org.opencalaccess.timer;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opencalaccess.timer.eo.AppTask;
import org.opencalaccess.timer.eo.AppTaskInstance;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXQ;

/**
 * The run() method here is the main loop for the tasking process.
 *
 */
public class AppTasker extends Thread {

	@SuppressWarnings("unused")
	private boolean verbose = System.getProperty(U.APP_TASKER_VERBOSE, "false").equalsIgnoreCase("true");

	private boolean abortLaunch = System.getProperty(U.APP_TASKER_ABORT_LAUNCH, "false").equalsIgnoreCase("true");

	public static void initialize() {

		String runAppTasks = System.getProperty(U.APP_TASKER_FEATURE_ENABLED, "false");

		String appTaskStartup =   System.getProperty(U.APP_TASKER_CHECK_STARTUP_WAIT_SECONDS, "30");
		String appTaskWait =      System.getProperty(U.APP_TASKER_CHECK_INTERVAL_SECONDS, "60");
		String appDatabseWait =   System.getProperty(U.APP_TASKER_DATABASE_INTERVAL_SECONDS, "60");

		U.log(U.APP_TASKER_FEATURE_ENABLED, " = ", runAppTasks);

		if ("YES".equalsIgnoreCase(runAppTasks) || "TRUE".equalsIgnoreCase(runAppTasks)) {

			(new AppTasker(appTaskStartup, appTaskWait)).start();

			(new AppScheduler(appDatabseWait)).start();
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

					U.log("launch of task \"", task.taskName(), " is scheduled to start now.");

					AppTaskInstance instance = AppTaskInstance.fetchRequiredAppTaskInstance(
							ec,
							ERXQ.and(
									AppTaskInstance.TASK.is(task),
									AppTaskInstance.QUEUE_TIME.isNotNull(),
									AppTaskInstance.START_TIME.isNull(),
									AppTaskInstance.END_TIME.isNull()));

					ec.saveChanges();

					AppTasked tasked = new AppTasked(instance);

					if (abortLaunch) {

						long now = U.now();

						instance.setStartTime(now);
						instance.setEndTime(now);
						instance.setResult(0);
						instance.setNote("aborted/testing");
						ec.saveChanges();

					} else {
						exec.submit(tasked);
					}
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

		U.log("launch of task \"", localTask.taskName(), " is demanded to start now.");

		long now = U.now();

		AppTaskInstance instance = AppTaskInstance.createAppTaskInstance(ec, now, localTask);

		ec.saveChanges();

		AppTasked tasked = new AppTasked(instance);

		tasked.run();
	}

	/**
	 * Run the task manually, now, without waiting for the task loop.
	 */
	public static void XXX_runTaskNow(AppTask task) {

		U.log("START");

		EOEditingContext ec = ERXEC.newEditingContext();

		AppTask localTask = task.localInstanceIn(ec);

		U.log("launch of task \"", localTask.taskName(), " is demanded to start now.");

		AppTaskInstance instance = AppTaskInstance.createAppTaskInstance(ec, U.now(), localTask);

		long now = U.now();

		instance.setQueueTime(now);
		instance.setStartTime(now);

		try {
			@SuppressWarnings("rawtypes")
			Class targetClazz = java.lang.Class.forName(localTask.className());

			java.lang.reflect.Method targetMethod = null;

			java.lang.reflect.Method[] methods = targetClazz.getMethods();
			for (java.lang.reflect.Method method : methods) {
				if (method.getName().equals(localTask.methodName())) {
					targetMethod = method;
				}
			}

			if (targetMethod == null) {
				throw new IllegalArgumentException("Class \"" + localTask.className() + "\" does not have method named \"" + localTask.methodName() + "\"");
			}

			@SuppressWarnings("unchecked")
			Object targetObj = targetClazz.getDeclaredConstructor().newInstance();

			targetMethod.invoke(targetObj, new Object[]{});

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		instance.setEndTime(System.currentTimeMillis());

		// TODO Remove this once every process method is setting a zero result itself.
		// TODO If we got here and result is NULL, we need another error mark with "unknown".
		//
		if (instance.result() == null) {
			instance.setResult(0);
		}

		U.log("saving");

		ec.saveChanges();

		U.log("DONE");
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
