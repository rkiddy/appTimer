// DO NOT EDIT.  Make changes to AppTaskInstance.java instead.
package org.opencalaccess.timer.eo;

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
public abstract class _AppTaskInstance extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "AppTaskInstance";

  // Attribute Keys
  public static final ERXKey<Long> END_TIME = new ERXKey<Long>("endTime", Type.Attribute);
  public static final ERXKey<String> NOTE = new ERXKey<String>("note", Type.Attribute);
  public static final ERXKey<Long> QUEUE_TIME = new ERXKey<Long>("queueTime", Type.Attribute);
  public static final ERXKey<Integer> RESULT = new ERXKey<Integer>("result", Type.Attribute);
  public static final ERXKey<Long> START_TIME = new ERXKey<Long>("startTime", Type.Attribute);

  // Relationship Keys
  public static final ERXKey<org.opencalaccess.timer.eo.AppTask> TASK = new ERXKey<org.opencalaccess.timer.eo.AppTask>("task", Type.ToOneRelationship);

  // Attributes
  public static final String END_TIME_KEY = END_TIME.key();
  public static final String NOTE_KEY = NOTE.key();
  public static final String QUEUE_TIME_KEY = QUEUE_TIME.key();
  public static final String RESULT_KEY = RESULT.key();
  public static final String START_TIME_KEY = START_TIME.key();

  // Relationships
  public static final String TASK_KEY = TASK.key();

  private static final Logger log = LoggerFactory.getLogger(_AppTaskInstance.class);

  public AppTaskInstance localInstanceIn(EOEditingContext editingContext) {
    AppTaskInstance localInstance = (AppTaskInstance)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public Long endTime() {
    return (Long) storedValueForKey(_AppTaskInstance.END_TIME_KEY);
  }

  public void setEndTime(Long value) {
    log.debug( "updating endTime from {} to {}", endTime(), value);
    takeStoredValueForKey(value, _AppTaskInstance.END_TIME_KEY);
  }

  public String note() {
    return (String) storedValueForKey(_AppTaskInstance.NOTE_KEY);
  }

  public void setNote(String value) {
    log.debug( "updating note from {} to {}", note(), value);
    takeStoredValueForKey(value, _AppTaskInstance.NOTE_KEY);
  }

  public Long queueTime() {
    return (Long) storedValueForKey(_AppTaskInstance.QUEUE_TIME_KEY);
  }

  public void setQueueTime(Long value) {
    log.debug( "updating queueTime from {} to {}", queueTime(), value);
    takeStoredValueForKey(value, _AppTaskInstance.QUEUE_TIME_KEY);
  }

  public Integer result() {
    return (Integer) storedValueForKey(_AppTaskInstance.RESULT_KEY);
  }

  public void setResult(Integer value) {
    log.debug( "updating result from {} to {}", result(), value);
    takeStoredValueForKey(value, _AppTaskInstance.RESULT_KEY);
  }

  public Long startTime() {
    return (Long) storedValueForKey(_AppTaskInstance.START_TIME_KEY);
  }

  public void setStartTime(Long value) {
    log.debug( "updating startTime from {} to {}", startTime(), value);
    takeStoredValueForKey(value, _AppTaskInstance.START_TIME_KEY);
  }

  public org.opencalaccess.timer.eo.AppTask task() {
    return (org.opencalaccess.timer.eo.AppTask)storedValueForKey(_AppTaskInstance.TASK_KEY);
  }

  public void setTask(org.opencalaccess.timer.eo.AppTask value) {
    takeStoredValueForKey(value, _AppTaskInstance.TASK_KEY);
  }

  public void setTaskRelationship(org.opencalaccess.timer.eo.AppTask value) {
    log.debug("updating task from {} to {}", task(), value);
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
      setTask(value);
    }
    else if (value == null) {
      org.opencalaccess.timer.eo.AppTask oldValue = task();
      if (oldValue != null) {
        removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _AppTaskInstance.TASK_KEY);
      }
    } else {
      addObjectToBothSidesOfRelationshipWithKey(value, _AppTaskInstance.TASK_KEY);
    }
  }


  public static AppTaskInstance createAppTaskInstance(EOEditingContext editingContext, Long queueTime
, org.opencalaccess.timer.eo.AppTask task) {
    AppTaskInstance eo = (AppTaskInstance) EOUtilities.createAndInsertInstance(editingContext, _AppTaskInstance.ENTITY_NAME);
    eo.setQueueTime(queueTime);
    eo.setTaskRelationship(task);
    return eo;
  }

  public static ERXFetchSpecification<AppTaskInstance> fetchSpec() {
    return new ERXFetchSpecification<AppTaskInstance>(_AppTaskInstance.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<AppTaskInstance> fetchAllAppTaskInstances(EOEditingContext editingContext) {
    return _AppTaskInstance.fetchAllAppTaskInstances(editingContext, null);
  }

  public static NSArray<AppTaskInstance> fetchAllAppTaskInstances(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _AppTaskInstance.fetchAppTaskInstances(editingContext, null, sortOrderings);
  }

  public static NSArray<AppTaskInstance> fetchAppTaskInstances(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<AppTaskInstance> fetchSpec = new ERXFetchSpecification<AppTaskInstance>(_AppTaskInstance.ENTITY_NAME, qualifier, sortOrderings);
    NSArray<AppTaskInstance> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static AppTaskInstance fetchAppTaskInstance(EOEditingContext editingContext, String keyName, Object value) {
    return _AppTaskInstance.fetchAppTaskInstance(editingContext, ERXQ.equals(keyName, value));
  }

  public static AppTaskInstance fetchAppTaskInstance(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<AppTaskInstance> eoObjects = _AppTaskInstance.fetchAppTaskInstances(editingContext, qualifier, null);
    AppTaskInstance eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one AppTaskInstance that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static AppTaskInstance fetchRequiredAppTaskInstance(EOEditingContext editingContext, String keyName, Object value) {
    return _AppTaskInstance.fetchRequiredAppTaskInstance(editingContext, ERXQ.equals(keyName, value));
  }

  public static AppTaskInstance fetchRequiredAppTaskInstance(EOEditingContext editingContext, EOQualifier qualifier) {
    AppTaskInstance eoObject = _AppTaskInstance.fetchAppTaskInstance(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no AppTaskInstance that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static AppTaskInstance localInstanceIn(EOEditingContext editingContext, AppTaskInstance eo) {
    AppTaskInstance localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
