package org.opencalaccess.timer.eo;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSTimestamp;

public class AppTaskInstance extends _AppTaskInstance {

	private static final long serialVersionUID = 370988125112833848L;

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AppTaskInstance.class);

	public Long runtime() {
		if (this.endTime() == null) {
			return null;
		} else {
			return (this.endTime() - this.startTime()) / 1000;
		}
	}

	public NSTimestamp startDate() {
		return new NSTimestamp(new Date(this.startTime()));
	}

	public NSTimestamp endDate() {
		if (this.endTime() == null) {
			return null;
		} else {
			return new NSTimestamp(new Date(this.endTime()));
		}
	}

	public Integer runningTime() {
		if (this.endTime() != null || this.startTime() == null) {
			return null;
		} else {
			return (int)(System.currentTimeMillis() - this.startTime()) / 1000;
		}
	}

	public Integer queuedTime() {
		if (this.endTime() != null) {
			return null;
		} else {
			return (int)(System.currentTimeMillis() - this.queuedTime()) / 1000;
		}
	}
}
