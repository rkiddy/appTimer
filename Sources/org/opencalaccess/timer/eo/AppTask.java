package org.opencalaccess.timer.eo;

import org.opencalaccess.timer.U;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXKey.Type;
import er.extensions.eof.ERXQ;

public class AppTask extends _AppTask {

	private static final long serialVersionUID = -5769773713087584279L;

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AppTask.class);

	public static final ERXKey<AppTaskInstance> LATEST_INSTANCE = new ERXKey<AppTaskInstance>("latestInstance", Type.Attribute);
	public static final String LATEST_INSTANCE_KEY = LATEST_INSTANCE.key();

	public static NSArray<AppTaskInstance> allRunningInstances(EOEditingContext ec) {
		return AppTaskInstance.fetchAppTaskInstances(
				ec,
				ERXQ.and(
						AppTaskInstance.QUEUE_TIME.isNotNull(),
						AppTaskInstance.START_TIME.isNotNull(),
						AppTaskInstance.END_TIME.isNull()),
				null);
	}

	public static NSArray<AppTaskInstance> allQueuedInstances(EOEditingContext ec) {
		return AppTaskInstance.fetchAppTaskInstances(
				ec,
				ERXQ.and(
						AppTaskInstance.QUEUE_TIME.isNotNull(),
						AppTaskInstance.START_TIME.isNull(),
						AppTaskInstance.END_TIME.isNull()),
				null);
	}

	public NSArray<AppTaskInstance> runningInstances() {
		return AppTaskInstance.fetchAppTaskInstances(
				this.editingContext(),
				AppTaskInstance.TASK.is(this).and(AppTaskInstance.END_TIME.isNull()),
				null);
	}

	public AppTaskInstance latestInstance() {

		NSArray<AppTaskInstance> instances = AppTaskInstance.fetchAppTaskInstances(
				this.editingContext(),
				AppTaskInstance.TASK.is(this),
				AppTaskInstance.START_TIME.descs());

		if (instances.isEmpty()) {
			return null;
		} else {
			return instances.get(0);
		}
	}

	public boolean enabled() { return this.active() == 1; }

	public boolean disabled() { return ! this.enabled(); }

	public String fullName() {
		StringBuilder str = new StringBuilder();
		str.append(this.appName());
		str.append(":");
		str.append(this.taskName());
		return str.toString();
	}

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
		NSArray<AppTaskInstance> instances = AppTaskInstance.fetchAppTaskInstances(
				this.editingContext(),
				ERXQ.and(
						AppTaskInstance.TASK.is(this),
						AppTaskInstance.QUEUE_TIME.isNotNull(),
						AppTaskInstance.EXEC_TIME.isNull(),
						AppTaskInstance.START_TIME.isNull(),
						AppTaskInstance.END_TIME.isNull()),
				null);
		return ! instances.isEmpty();
	}

	public boolean isExeced() {
		NSArray<AppTaskInstance> instances = AppTaskInstance.fetchAppTaskInstances(
				this.editingContext(),
				ERXQ.and(
						AppTaskInstance.TASK.is(this),
						AppTaskInstance.QUEUE_TIME.isNotNull(),
						AppTaskInstance.EXEC_TIME.isNotNull(),
						AppTaskInstance.START_TIME.isNull(),
						AppTaskInstance.END_TIME.isNull()),
				null);
		return ! instances.isEmpty();
	}

	public boolean isRunning() {
		NSArray<AppTaskInstance> instances = AppTaskInstance.fetchAppTaskInstances(
				this.editingContext(),
				ERXQ.and(
						AppTaskInstance.TASK.is(this),
						AppTaskInstance.QUEUE_TIME.isNotNull(),
						AppTaskInstance.EXEC_TIME.isNull(),
						AppTaskInstance.START_TIME.isNotNull(),
						AppTaskInstance.END_TIME.isNull()),
				null);
		return ! instances.isEmpty();
	}

	public boolean isQuiet() {
		NSArray<AppTaskInstance> instances = AppTaskInstance.fetchAppTaskInstances(
				this.editingContext(),
				ERXQ.and(
						AppTaskInstance.TASK.is(this),
						AppTaskInstance.END_TIME.isNull()),
				null);
		return instances.isEmpty();
	}

	public boolean isBouncing() {

		long oldest = U.now() - intervalDuration(this.intervalName());

		NSArray<AppTaskInstance> instances = AppTaskInstance.fetchAppTaskInstances(
				this.editingContext(),
				ERXQ.and(
						AppTaskInstance.TASK.is(this),
						AppTaskInstance.END_TIME.greaterThan(oldest)),
				null);

		return instances.size() > 3;
	}
}
