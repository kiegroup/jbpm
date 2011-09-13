/*
 * Copyright 2011 JBoss Inc
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
package org.jbpm.formbuilder.server.trans.gwt;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.jbpm.formbuilder.server.form.FormEncodingServerFactory;
import org.jbpm.formbuilder.server.trans.LanguageException;
import org.jbpm.formbuilder.shared.api.FormItemRepresentation;
import org.jbpm.formbuilder.shared.api.FormRepresentation;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;

public class Translator implements org.jbpm.formbuilder.server.trans.Translator {

    private static final String LANG = "gwt";
    
    @Override
    public String getLanguage() {
        return LANG;
    }

    @Override
    public URL translateForm(FormRepresentation form) throws LanguageException {
        FormRepresentationEncoder encoder = FormEncodingServerFactory.getEncoder();
        try {
            String json = encoder.encode(form);
            File file = File.createTempFile("form-gwt-", ".json");
            FileUtils.writeStringToFile(file, json);
            return FileUtils.toURLs(new File[] { file })[0];
        } catch (IOException e) {
            throw new LanguageException("Problem writing temporal file", e);
        } catch (FormEncodingException e) {
            throw new LanguageException("Problem encoding form", e);
        }
    }

    @Override
    public Object translateItem(FormItemRepresentation item) throws LanguageException {
        /* not used */
        return null;
    }

}
