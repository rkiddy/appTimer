package org.opencalaccess.timer;

import java.lang.reflect.InvocationTargetException;

import org.opencalaccess.timer.eo.AppTask;
import org.opencalaccess.timer.eo.AppTaskInstance;

import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.eof.ERXEC;

/**
 * When this finds the method to execute, the method name must be unique. Methods with the same
 * name and different signatures are not allowed.
 *
 */
public class AppTasked extends Thread {

	private AppTaskInstance instance;

	private EOEditingContext ec = ERXEC.newEditingContext();

	public AppTasked(AppTaskInstance instance) {
		this.instance = instance.localInstanceIn(ec);
	}

	public void run() {

		AppTask task = instance.task();

		try {
			@SuppressWarnings("rawtypes")
			Class targetClazz = java.lang.Class.forName(task.className());

			java.lang.reflect.Method targetMethod = null;

			java.lang.reflect.Method[] methods = targetClazz.getMethods();
			for (java.lang.reflect.Method method : methods) {
				if (method.getName().equals(task.methodName())) {
					targetMethod = method;
				}
			}

			if (targetMethod == null) {
				throw new IllegalArgumentException("Class \"" + task.className() + "\" does not have method named \"" + task.methodName() + "\"");
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

		ec.saveChanges();
	}
}
