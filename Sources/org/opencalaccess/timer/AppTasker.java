package org.opencalaccess.timer;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opencalaccess.timer.eo.AppTask;
import org.opencalaccess.timer.eo.AppTaskInstance;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXEC;

public class AppTasker extends Thread {

	public static boolean verbose() {
		return System.getProperty("AppTaskerVerbose", "false").equalsIgnoreCase("true");
	}

	public static void initialize() {

		String runAppTasks = System.getProperty(U.APP_TASKER_FEATURE_ENABLED, "false");

		String appTaskStartup =   System.getProperty(U.APP_TASKER_CHECK_STARTUP_WAIT_SECONDS, "30");
		String appTaskWait =      System.getProperty(U.APP_TASKER_CHECK_INTERVAL_SECONDS, "60");

		U.log(U.APP_TASKER_FEATURE_ENABLED, " = ", runAppTasks);

		if ("YES".equalsIgnoreCase(runAppTasks) || "TRUE".equalsIgnoreCase(runAppTasks)) {
			(new AppTasker(appTaskStartup, appTaskWait)).start();
		}
	}

	public AppTasker(String startupWait, String intervalWait) {
		try {	
			this.startupWait = Integer.valueOf(startupWait);
			this.intervalWait = Integer.valueOf(intervalWait);
		} catch (NumberFormatException e) {
			this.startupWait = 10;
			this.intervalWait = 60;
		}
	}

	private EOEditingContext ec = ERXEC.newEditingContext();

	private int startupWait;

	private int intervalWait;
	
	private static final ExecutorService exec = Executors.newSingleThreadExecutor();

	private static final ConcurrentHashMap<String,AppTask> tasksToBeLaunched = new ConcurrentHashMap<>();

	public void run() {

		U.log("tasker startupWait = ", startupWait, " seconds");
		U.log("tasker intervalWait = ", intervalWait, " seconds");

		wait(startupWait);

		while (true) {

			NSArray<AppTaskInstance> running = AppTask.allRunningInstances(ec);

			U.log("running instances # ", running.size());

			if (verbose()) {
				for (AppTaskInstance instance : running) {
					U.log("task instance: ", instance);
				}
			}

			tasksToBeLaunched.putAll(AppTimer.tasksNeedingRun(ec));

			U.log("tasks needing to run # ", tasksToBeLaunched.size());

			for (Map.Entry<String,AppTask> taskEntry : tasksToBeLaunched.entrySet()) {

				AppTask task = taskEntry.getValue().localInstanceIn(ec);

				//
				// For now, we are only running the first task.
				//
				U.log("runnable task found: ", task);

				AppTaskInstance instance = AppTaskInstance.createAppTaskInstance(ec, System.currentTimeMillis(), task);

				ec.saveChanges();

				AppTasked tasked = new AppTasked(instance);

				exec.submit(tasked);

				tasksToBeLaunched.remove(task.fullName());
			}

			wait(intervalWait);
		}
	}

	public static void addTaskForLaunch(AppTask task) {
		tasksToBeLaunched.putIfAbsent(task.fullName(), task);
	}

	public static void runTaskNow(AppTask task) {

		U.log("START");

		EOEditingContext ec = ERXEC.newEditingContext();

		AppTask localTask = task.localInstanceIn(ec);

		U.log("launch of task \"", localTask.taskName(), " is requested");

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
}
