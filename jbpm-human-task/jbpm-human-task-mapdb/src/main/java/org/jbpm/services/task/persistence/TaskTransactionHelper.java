package org.jbpm.services.task.persistence;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.drools.persistence.TransactionManager;

public class TaskTransactionHelper {

	private static final String TSK_UPDETEABLE_RESOURCE = "task-updateable-resource";
	
    @SuppressWarnings("unchecked")
    public static void addToUpdatableSet(TransactionManager txm, MapDBElement element) {
        if (element == null) {
            return;
        }
        Set<MapDBElement> toBeUpdated = (Set<MapDBElement>) txm.getResource(TSK_UPDETEABLE_RESOURCE);
        if (toBeUpdated == null) {
            toBeUpdated = new LinkedHashSet<MapDBElement>();
            txm.putResource(TSK_UPDETEABLE_RESOURCE, toBeUpdated);
        }
        toBeUpdated.add(element);
    }

    @SuppressWarnings("unchecked")
    public static void removeFromUpdatableSet(TransactionManager txm, MapDBElement element) {
        Set<MapDBElement> toBeUpdated = (Set<MapDBElement>) txm.getResource(TSK_UPDETEABLE_RESOURCE);
        if (toBeUpdated == null) {
            return;
        }
        toBeUpdated.remove(element);
    }

    @SuppressWarnings("unchecked")
    public static Set<MapDBElement> getUpdateableSet(TransactionManager txm) {
        Set<MapDBElement> toBeUpdated = (Set<MapDBElement>) txm.getResource(TSK_UPDETEABLE_RESOURCE);
        if (toBeUpdated == null) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<MapDBElement>(toBeUpdated);
    }


}
