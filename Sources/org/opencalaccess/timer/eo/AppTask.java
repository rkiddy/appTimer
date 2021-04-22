package org.opencalaccess.timer.eo;

import org.opencalaccess.timer.U;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXKey.Type;
import er.extensions.eof.ERXQ;

/**
 * An AppTask needs to exist in one of the following states.
 * <ol>
 *   <li>Quiet</li>
 *   <li>Queued</li>
 *   <li>Execed</li>
 *   <li>Running</li>
 * </ol>
 *
 * These states correspond to a AppTaskInstance with certain settings.
 * <ol>
 *   <li>Quiet - no AppTaskInstance with endTime == NULL exists.</li>
 *   <li>Queued - queuedTime is not NULL and execTime, startTime, and endTime are all NULL.</li>
 *   <li>Execed - queuedTime and execTime are not NULL and startTime and endTime are all NULL. </li>
 *   <li>Running - queuedTime, execTime, and startTime are not NULL and endTime is NULL.</li>
 * </ol>
 *
 * If an AppTask is not in a Quiet state, then there be only one AppTaskInstance that has a endTime == NULL.
 *
 */
public class AppTask extends _AppTask {

	private static final long serialVersionUID = -5769773713087584279L;

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AppTask.class);

	public static final ERXKey<AppTaskInstance> LATEST_INSTANCE = new ERXKey<AppTaskInstance>("latestInstance", Type.Attribute);
	public static final String LATEST_INSTANCE_KEY = LATEST_INSTANCE.key();

    public static NSArray<AppTask> fetchAllActiveTasks(EOEditingContext ec) {
    	return AppTask.fetchAppTasks(
    			ec,
    			AppTask.ACTIVE.is(1),
    			null);
    }

	public static final String TASK_STATE_QUIET = "Quiet";
	public static final String TASK_STATE_QUEUED = "Queued";
	public static final String TASK_STATE_EXECED = "Execed";
	public static final String TASK_STATE_RUNNING = "Running";

	public AppTaskInstance latestInstance() {

		NSArray<AppTaskInstance> instances = AppTaskInstance.fetchAppTaskInstances(
				this.editingContext(),
				AppTaskInstance.TASK.is(this),
				AppTaskInstance.QUEUE_TIME.descs());

		if (instances.isEmpty()) {
			return null;
		} else {
			return instances.get(0);
		}
	}

	public AppTaskInstance latestSuccessfulInstance() {

		NSArray<AppTaskInstance> instances = AppTaskInstance.fetchAppTaskInstances(
				this.editingContext(),
				AppTaskInstance.TASK.is(this),
				AppTaskInstance.START_TIME.descs());

		if (instances.isEmpty()) {
			return null;
		} else {
			for (AppTaskInstance instance : instances) {
				if (instance.result() != null && instance.result() == 0) {
					return instance;
				}
			}
			return null;
		}
	}

	public String fullName() {
		StringBuilder str = new StringBuilder();
		str.append(this.appName());
		str.append(":");
		str.append(this.taskName());
		return str.toString();
	}

	public boolean enabled() { return this.active() == 1; }

	public boolean disabled() { return ! this.enabled(); }

	public String duration(long diff) {

		long seconds = 0;
		long minutes = 0;
		long hours = 0;
		long days = 0;

		seconds = diff % 60;
		diff = diff / 60;

		minutes = diff % 60;
		diff = diff / 60;

		hours = diff % 24;
		diff = diff / 24;

		days = diff;

		StringBuilder str = new StringBuilder();

		if (days > 0) {
			str.append(days);
			str.append("d ");
		}
		if (hours > 0) {
			str.append(hours);
			str.append("h ");
		}
		if (minutes > 0) {
			str.append(minutes);
			str.append("m ");
		}
		if (seconds > 0) {
			str.append(seconds);
			str.append("s");
		}

		return str.toString();
	}

	public String lastInstanceAge() {

		AppTaskInstance latestInstance = this.latestInstance();

		if (latestInstance == null) {
			return null;
		} else {

			Long now = System.currentTimeMillis();
			Long then = latestInstance.endTime();
			if (then == null) {
				return null;
			}
			Long diff = (now - then) / 1000;

			return duration(diff);
		}
	}

	public String runningInstanceAge() {

		AppTaskInstance latestInstance = this.latestInstance();

		if (latestInstance == null) {
			return null;
		} else {

			Long now = System.currentTimeMillis();
			Long then = latestInstance.startTime();
			if (then == null) {
				return null;
			}
			Long diff = (now - then) / 1000;

			return duration(diff);
		}
	}

	public long intervalDuration(String name) {

		if (U.intervalTimes.containsKey(name)) {
			return U.intervalTimes.get(name);
		}

		if (this.intervalName().startsWith(U.APP_TASK_INTERVAL_PREFIX_MINUTES)) {

			long minutes = Long.valueOf(this.intervalName().substring(U.APP_TASK_INTERVAL_PREFIX_MINUTES.length()));

			return minutes * (int) U.one_minute;
		}

		if (this.intervalName().startsWith(U.APP_TASK_INTERVAL_PREFIX_HOURS)) {

			long hours = Long.valueOf(this.intervalName().substring(U.APP_TASK_INTERVAL_PREFIX_HOURS.length()));

			return hours * (int) U.one_hour;
		}

		throw new IllegalArgumentException("Cannot calculate interval length for unknown interval with name: \"" + this.intervalName() + "\"");
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("task: ");
		str.append(this.taskName());
		return str.toString();
	}

	public boolean isQueued() {
		AppTaskInstance latest = latestInstance();
		return latest != null &&
				latest.queueTime() != null &&
				latest.execTime() == null &&
				latest.startTime() == null &&
				latest.endTime() == null;
	}

	public void setToQueued() {
		confirmValid();
		if (this.isQuiet()) {
			AppTaskInstance.createAppTaskInstance(this.editingContext(), U.now(), this);
		} else {
			throw new IllegalArgumentException("Cannot set to Queued unless the task is Quiet.");
		}
	}

	public boolean isExeced() {
		AppTaskInstance latest = latestInstance();
		return latest != null &&
				latest.queueTime() != null &&
				latest.execTime() != null &&
				latest.startTime() == null &&
				latest.endTime() == null;
	}

	public void setToExeced() {
		confirmValid();
		if (this.isQueued()) {
			latestInstance().setExecTime(U.now());
		} else {
			throw new IllegalArgumentException("Cannot set to Running unless the task is Queued.");
		}
	}

	public boolean isRunning() {
		AppTaskInstance latest = latestInstance();
		return latest != null &&
				latest.queueTime() != null &&
				latest.execTime() != null &&
				latest.startTime() != null &&
				latest.endTime() == null;
	}

	public void setToRunning() {
		confirmValid();
		if (this.isExeced()) {
			latestInstance().setStartTime(U.now());
		} else {
			throw new IllegalArgumentException("Cannot set to Running unless the task is Execed.");
		}
	}

	public boolean isQuiet() {
		AppTaskInstance latest = latestInstance();
		return latest == null || latest.endTime() != null;
	}

	public void setToQuiet(int result, String note) {
		confirmValid();
		if ( ! this.isQuiet()) {
			AppTaskInstance instance = latestInstance();
			instance.setEndTime(U.now());
			instance.setResult(result);
			instance.setNote(note);
		} else {
			throw new IllegalArgumentException("Cannot set to Quiet unless the task is not Quiet.");
		}
	}

	public boolean isBouncing() {

		long intervalLength = U.now() - intervalDuration(this.intervalName());

		NSArray<AppTaskInstance> instances = AppTaskInstance.fetchAppTaskInstances(
				this.editingContext(),
				ERXQ.and(
						AppTaskInstance.TASK.is(this),
						AppTaskInstance.END_TIME.greaterThan(intervalLength)),
				null);

		return instances.size() > 3;
	}

	private void confirmValid() {
		if ( ! isValid()) {
			throw new IllegalArgumentException("task \"" + this.taskName() + "\" is NOT in valid state.");
		}
	}

	public boolean isValid() {
		NSArray<AppTaskInstance> found = AppTaskInstance.fetchAppTaskInstances(
				this.editingContext(),
				ERXQ.and(
						AppTaskInstance.TASK.is(this),
						AppTaskInstance.END_TIME.isNull()),
				null);
		return found.size() < 2;
	}

	public String state() {
		if (this.isValid()) {
			if (this.isQuiet()) {
				return TASK_STATE_QUIET;
			}
			if (this.isQueued()) {
				return TASK_STATE_QUEUED;
			}
			if (this.isExeced()) {
				return TASK_STATE_EXECED;
			}
			if (this.isRunning()) {
				return TASK_STATE_RUNNING;
			}
		} else {
			return null;
		}
		return "UNKNOWN";
	}
}
