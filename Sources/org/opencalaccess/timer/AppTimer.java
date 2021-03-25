package org.opencalaccess.timer;

import er.extensions.ERXFrameworkPrincipal;

/**
 * Schedule tasks to run when desired by putting them on the queue.
 *
 * This only interacts with the database and not the AppTask executor.
 *
 * @author ray
 */
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

	public static boolean verbose() {
		return System.getProperty("AppTaskerVerbose", "false").equalsIgnoreCase("true");
	}
}
