package org.opencalaccess.apptasks;

import java.lang.reflect.InvocationTargetException;

import org.opencalaccess.apptasks.eo.AppTask;
import org.opencalaccess.apptasks.eo.AppTaskInstance;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

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

			String methodParams = task.methodParams();

			if (methodParams != null && ! methodParams.isEmpty()) {

				Object[] paramsObjs = NSArray.componentsSeparatedByString(task.methodParams(), " ").toArray();

				String[] params = new String[paramsObjs.length];

				for (int idx = 0; idx < paramsObjs.length; idx++) {
					params[idx] = (String)paramsObjs[idx];
				}

				targetMethod.invoke(targetObj, new Object[]{params});

			} else {
				targetMethod.invoke(targetObj, new Object[]{});
			}

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

		instance.setResult(0);

		ec.saveChanges();
	}
}
