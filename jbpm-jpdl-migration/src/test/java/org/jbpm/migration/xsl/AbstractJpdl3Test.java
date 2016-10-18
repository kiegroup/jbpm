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
package org.jbpm.migration.xsl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.text.MessageFormat;

import org.apache.commons.io.FileUtils;
import org.jbpm.migration.JbpmMigration;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Base class for tests for the jPDL process definition transformer with JAXP.
 */
public abstract class AbstractJpdl3Test {
    // XSLT sheet.
    private static final String XSLT_SHEET = "src/main/resources/jpdl3-bpmn2.xsl";

    // Results file of transformation using a Format.
    private static final String RESULTS_FILE_FORMAT = "target/migrated_processes/{0}/processdefinition.bpmn2";
    
    // Directory for test to place ouput created using a Format.
    private static final String RESULTS_DIR_FORMAT = "target/migrated_processes/{0}";
    
    /** Allow for subclasses to override the JPDL validation, as some (ESB) processes are not valid. */
    protected boolean validateJpdl = true;

    @BeforeClass
    public static void oneTimeSetUp() {
        // Make sure the style sheet is available.
        final File xsltSheet = new File(XSLT_SHEET);
        assertThat("Stylesheet missing.", xsltSheet.exists(), is(true));
    }

    /**
     * Transform the input file to an output file.
     */
    @Test
    public void transformjPDL3() throws Exception {
        // Make sure the input file is available.
        final File jpdl = new File(getJpdlFile());
        assertThat("Indicated input file missing.", jpdl.exists(), is(true));

        if (validateJpdl) {
            // Validate the input process definition.
            assertThat("Not a valid jPDL definition.", JbpmMigration.validateJpdl(FileUtils.readFileToString(jpdl)), is(true));
        }
    
        // Remove any previously existing output first.
        File bpmn = new File(getResultsFile());
        if (bpmn.exists()) {
            assertThat("Unable to clean output.", bpmn.delete(), is(true));
        }
        
        // Ensure directory exists.
        String results = MessageFormat.format(RESULTS_DIR_FORMAT, jpdl.getParentFile().getName());
        File location = new File(results);
        if ( !location.exists() && !location.mkdirs()) {
            fail("Problem creating output directory: " + results);
        }

        // Transform the input file; creates the output file.
        JbpmMigration.main(new String[] { jpdl.getPath(), XSLT_SHEET, getResultsFile() });

        // Check that an output file is created.
        bpmn = new File(getResultsFile());
        assertThat("Expected output file missing.", bpmn.exists(), is(true));

        // Validate the output process definition.
        assertThat("Not a valid BPMN definition.", JbpmMigration.validateBpmn(FileUtils.readFileToString(bpmn)), is(true));
    }

    /**
     * Provide a results file path.
     * 
     * @return String.
     */
    protected String getResultsFile() {
        File jpdl = new File(getJpdlFile());
        String resultsFile = MessageFormat.format(RESULTS_FILE_FORMAT, jpdl.getParentFile().getName());
        return resultsFile;
    }

    protected abstract String getJpdlFile();
}
