/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.migration.tools;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jbpm.migration.JbpmMigration;

/**
 * JPDL -> BPMN2 definition migration. Calls migration tool, which executes xslt
 * template transformation.
 */
public class MigrationHelper {

    /**
     * Migration from JPDL to BPMN2 format.
     *
     * @param jpdlFile
     *            File with process definition in JPDL.
     * @param bpmnFile
     *            File which should contain process definition in BPMN after
     *            migration.
     * @throws IllegalArgumentException
     *             if Jpdl or Bpmn2 definition file is not valid.
     */
    public static void migration(File jpdlFile, File bpmnFile) {
        String jpdlStr;
        try {
            jpdlStr = FileUtils.readFileToString(jpdlFile);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        if (!JbpmMigration.validateJpdl(jpdlStr)) {
            throw new IllegalArgumentException("JPDL definition file is not valid");
        }

        String bpmnStr = JbpmMigration.transform(jpdlStr);

        try {
            FileUtils.writeStringToFile(bpmnFile, bpmnStr);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        if (!JbpmMigration.validateBpmn(bpmnStr)) {
            throw new IllegalArgumentException("BPMN2 definition file is not valid");
        }
    }
}
