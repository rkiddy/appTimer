// DO NOT EDIT.  Make changes to AppTask.java instead.
package org.opencalaccess.apptasks.eo;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;

import er.extensions.eof.*;
import er.extensions.eof.ERXKey.Type;
import er.extensions.foundation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("all")
public abstract class _AppTask extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "AppTask";

  // Attribute Keys
  public static final ERXKey<Integer> ACTIVE = new ERXKey<Integer>("active", Type.Attribute);
  public static final ERXKey<String> APP_NAME = new ERXKey<String>("appName", Type.Attribute);
  public static final ERXKey<String> CHECK_NAME = new ERXKey<String>("checkName", Type.Attribute);
  public static final ERXKey<String> CLASS_NAME = new ERXKey<String>("className", Type.Attribute);
  public static final ERXKey<String> INTERVAL_NAME = new ERXKey<String>("intervalName", Type.Attribute);
  public static final ERXKey<String> METHOD_NAME = new ERXKey<String>("methodName", Type.Attribute);
  public static final ERXKey<String> TASK_NAME = new ERXKey<String>("taskName", Type.Attribute);

  // Relationship Keys
  public static final ERXKey<org.opencalaccess.apptasks.eo.AppTaskInstance> INSTANCES = new ERXKey<org.opencalaccess.apptasks.eo.AppTaskInstance>("instances", Type.ToManyRelationship);

  // Attributes
  public static final String ACTIVE_KEY = ACTIVE.key();
  public static final String APP_NAME_KEY = APP_NAME.key();
  public static final String CHECK_NAME_KEY = CHECK_NAME.key();
  public static final String CLASS_NAME_KEY = CLASS_NAME.key();
  public static final String INTERVAL_NAME_KEY = INTERVAL_NAME.key();
  public static final String METHOD_NAME_KEY = METHOD_NAME.key();
  public static final String TASK_NAME_KEY = TASK_NAME.key();

  // Relationships
  public static final String INSTANCES_KEY = INSTANCES.key();

  private static final Logger log = LoggerFactory.getLogger(_AppTask.class);

  public AppTask localInstanceIn(EOEditingContext editingContext) {
    AppTask localInstance = (AppTask)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public Integer active() {
    return (Integer) storedValueForKey(_AppTask.ACTIVE_KEY);
  }

  public void setActive(Integer value) {
    log.debug( "updating active from {} to {}", active(), value);
    takeStoredValueForKey(value, _AppTask.ACTIVE_KEY);
  }

  public String appName() {
    return (String) storedValueForKey(_AppTask.APP_NAME_KEY);
  }

  public void setAppName(String value) {
    log.debug( "updating appName from {} to {}", appName(), value);
    takeStoredValueForKey(value, _AppTask.APP_NAME_KEY);
  }

  public String checkName() {
    return (String) storedValueForKey(_AppTask.CHECK_NAME_KEY);
  }

  public void setCheckName(String value) {
    log.debug( "updating checkName from {} to {}", checkName(), value);
    takeStoredValueForKey(value, _AppTask.CHECK_NAME_KEY);
  }

  public String className() {
    return (String) storedValueForKey(_AppTask.CLASS_NAME_KEY);
  }

  public void setClassName(String value) {
    log.debug( "updating className from {} to {}", className(), value);
    takeStoredValueForKey(value, _AppTask.CLASS_NAME_KEY);
  }

  public String intervalName() {
    return (String) storedValueForKey(_AppTask.INTERVAL_NAME_KEY);
  }

  public void setIntervalName(String value) {
    log.debug( "updating intervalName from {} to {}", intervalName(), value);
    takeStoredValueForKey(value, _AppTask.INTERVAL_NAME_KEY);
  }

  public String methodName() {
    return (String) storedValueForKey(_AppTask.METHOD_NAME_KEY);
  }

  public void setMethodName(String value) {
    log.debug( "updating methodName from {} to {}", methodName(), value);
    takeStoredValueForKey(value, _AppTask.METHOD_NAME_KEY);
  }

  public String taskName() {
    return (String) storedValueForKey(_AppTask.TASK_NAME_KEY);
  }

  public void setTaskName(String value) {
    log.debug( "updating taskName from {} to {}", taskName(), value);
    takeStoredValueForKey(value, _AppTask.TASK_NAME_KEY);
  }

  public NSArray<org.opencalaccess.apptasks.eo.AppTaskInstance> instances() {
    return (NSArray<org.opencalaccess.apptasks.eo.AppTaskInstance>)storedValueForKey(_AppTask.INSTANCES_KEY);
  }

  public NSArray<org.opencalaccess.apptasks.eo.AppTaskInstance> instances(EOQualifier qualifier) {
    return instances(qualifier, null, false);
  }

  public NSArray<org.opencalaccess.apptasks.eo.AppTaskInstance> instances(EOQualifier qualifier, boolean fetch) {
    return instances(qualifier, null, fetch);
  }

  public NSArray<org.opencalaccess.apptasks.eo.AppTaskInstance> instances(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<org.opencalaccess.apptasks.eo.AppTaskInstance> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = ERXQ.equals(org.opencalaccess.apptasks.eo.AppTaskInstance.TASK_KEY, this);

      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        fullQualifier = ERXQ.and(qualifier, inverseQualifier);
      }

      results = org.opencalaccess.apptasks.eo.AppTaskInstance.fetchAppTaskInstances(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = instances();
      if (qualifier != null) {
        results = (NSArray<org.opencalaccess.apptasks.eo.AppTaskInstance>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<org.opencalaccess.apptasks.eo.AppTaskInstance>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }

  public void addToInstances(org.opencalaccess.apptasks.eo.AppTaskInstance object) {
    includeObjectIntoPropertyWithKey(object, _AppTask.INSTANCES_KEY);
  }

  public void removeFromInstances(org.opencalaccess.apptasks.eo.AppTaskInstance object) {
    excludeObjectFromPropertyWithKey(object, _AppTask.INSTANCES_KEY);
  }

  public void addToInstancesRelationship(org.opencalaccess.apptasks.eo.AppTaskInstance object) {
    log.debug("adding {} to instances relationship", object);
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
      addToInstances(object);
    }
    else {
      addObjectToBothSidesOfRelationshipWithKey(object, _AppTask.INSTANCES_KEY);
    }
  }

  public void removeFromInstancesRelationship(org.opencalaccess.apptasks.eo.AppTaskInstance object) {
    log.debug("removing {} from instances relationship", object);
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
      removeFromInstances(object);
    }
    else {
      removeObjectFromBothSidesOfRelationshipWithKey(object, _AppTask.INSTANCES_KEY);
    }
  }

  public org.opencalaccess.apptasks.eo.AppTaskInstance createInstancesRelationship() {
    EOEnterpriseObject eo = EOUtilities.createAndInsertInstance(editingContext(),  org.opencalaccess.apptasks.eo.AppTaskInstance.ENTITY_NAME );
    addObjectToBothSidesOfRelationshipWithKey(eo, _AppTask.INSTANCES_KEY);
    return (org.opencalaccess.apptasks.eo.AppTaskInstance) eo;
  }

  public void deleteInstancesRelationship(org.opencalaccess.apptasks.eo.AppTaskInstance object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _AppTask.INSTANCES_KEY);
  }

  public void deleteAllInstancesRelationships() {
    Enumeration<org.opencalaccess.apptasks.eo.AppTaskInstance> objects = instances().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteInstancesRelationship(objects.nextElement());
    }
  }


  public static AppTask createAppTask(EOEditingContext editingContext, Integer active
, String appName
, String className
, String intervalName
, String methodName
, String taskName
) {
    AppTask eo = (AppTask) EOUtilities.createAndInsertInstance(editingContext, _AppTask.ENTITY_NAME);
    eo.setActive(active);
    eo.setAppName(appName);
    eo.setClassName(className);
    eo.setIntervalName(intervalName);
    eo.setMethodName(methodName);
    eo.setTaskName(taskName);
    return eo;
  }

  public static ERXFetchSpecification<AppTask> fetchSpec() {
    return new ERXFetchSpecification<AppTask>(_AppTask.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<AppTask> fetchAllAppTasks(EOEditingContext editingContext) {
    return _AppTask.fetchAllAppTasks(editingContext, null);
  }

  public static NSArray<AppTask> fetchAllAppTasks(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _AppTask.fetchAppTasks(editingContext, null, sortOrderings);
  }

  public static NSArray<AppTask> fetchAppTasks(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<AppTask> fetchSpec = new ERXFetchSpecification<AppTask>(_AppTask.ENTITY_NAME, qualifier, sortOrderings);
    NSArray<AppTask> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static AppTask fetchAppTask(EOEditingContext editingContext, String keyName, Object value) {
    return _AppTask.fetchAppTask(editingContext, ERXQ.equals(keyName, value));
  }

  public static AppTask fetchAppTask(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<AppTask> eoObjects = _AppTask.fetchAppTasks(editingContext, qualifier, null);
    AppTask eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one AppTask that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static AppTask fetchRequiredAppTask(EOEditingContext editingContext, String keyName, Object value) {
    return _AppTask.fetchRequiredAppTask(editingContext, ERXQ.equals(keyName, value));
  }

  public static AppTask fetchRequiredAppTask(EOEditingContext editingContext, EOQualifier qualifier) {
    AppTask eoObject = _AppTask.fetchAppTask(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no AppTask that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static AppTask localInstanceIn(EOEditingContext editingContext, AppTask eo) {
    AppTask localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
