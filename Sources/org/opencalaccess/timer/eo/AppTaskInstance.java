package org.opencalaccess.timer.eo;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.eof.ERXQ;

public class AppTaskInstance extends _AppTaskInstance {

	private static final long serialVersionUID = 370988125112833848L;

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AppTaskInstance.class);

	public static NSArray<AppTaskInstance> allRunningInstances(EOEditingContext ec) {
		return AppTaskInstance.fetchAppTaskInstances(
				ec,
				ERXQ.and(
						AppTaskInstance.QUEUE_TIME.isNotNull(),
						AppTaskInstance.EXEC_TIME.isNotNull(),
						AppTaskInstance.START_TIME.isNotNull(),
						AppTaskInstance.END_TIME.isNull()),
				null);
	}

	public static NSArray<AppTaskInstance> allQueuedInstances(EOEditingContext ec) {
		return AppTaskInstance.fetchAppTaskInstances(
				ec,
				ERXQ.and(
						AppTaskInstance.QUEUE_TIME.isNotNull(),
						AppTaskInstance.EXEC_TIME.isNotNull(),
						AppTaskInstance.START_TIME.isNull(),
						AppTaskInstance.END_TIME.isNull()),
				null);
	}

	public NSTimestamp queueDate() {
		if (this.queueTime() == null) {
			return null;
		} else {
			return new NSTimestamp(new Date(this.queueTime()));
		}
	}

	public NSTimestamp execDate() {
		if (this.execTime() == null) {
			return null;
		} else {
			return new NSTimestamp(new Date(this.execTime()));
		}
	}

	public NSTimestamp startDate() {
		if (this.startTime() == null) {
			return null;
		} else {
			return new NSTimestamp(new Date(this.startTime()));
		}
	}

	public NSTimestamp endDate() {
		if (this.endTime() == null) {
			return null;
		} else {
			return new NSTimestamp(new Date(this.endTime()));
		}
	}

	public Long runDuration() {
		if (this.endTime() == null || this.startTime() == null) {
			return null;
		} else {
			return (this.endTime() - this.startTime()) / 1000;
		}
	}

}
