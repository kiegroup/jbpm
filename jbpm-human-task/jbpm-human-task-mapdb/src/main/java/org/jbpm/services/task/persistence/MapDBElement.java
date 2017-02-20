package org.jbpm.services.task.persistence;

import org.mapdb.DB;

public interface MapDBElement {

	void updateOnMap(DB db);

}
