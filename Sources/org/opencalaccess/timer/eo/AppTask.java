package org.opencalaccess.timer.eo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXQ;
import er.extensions.eof.ERXKey.Type;

public class AppTask extends _AppTask {

	private static final long serialVersionUID = -5769773713087584279L;

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AppTask.class);

	public static final ERXKey<AppTaskInstance> LATEST_INSTANCE = new ERXKey<AppTaskInstance>("latestInstance", Type.Attribute);
	public static final String LATEST_INSTANCE_KEY = LATEST_INSTANCE.key();

	public static NSArray<AppTaskInstance> allRunningInstances(EOEditingContext ec) {
		return AppTaskInstance.fetchAppTaskInstances(ec, AppTaskInstance.END_TIME.isNull(), null);
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

	public String lastInstanceAge() {

		AppTaskInstance latestInstance = this.latestInstance();

		if (latestInstance == null) {
			return null;
		} else {

			Long now = System.currentTimeMillis();
			Long then = latestInstance.endTime();
			if (then == null) {
				then = latestInstance.startTime();
			}
			Long diff = (now - then) / 1000;

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
}
