package org.jbpm.formbuilder.server.form;

import javax.jcr.Repository; 
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.core.RepositoryFactoryImpl;
import org.drools.repository.RulesRepository;
import org.drools.repository.remoteapi.RestAPI;
import org.jbpm.formbuilder.shared.form.FormDefinitionService;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.trans.Language;
import org.jbpm.formbuilder.shared.rep.trans.LanguageException;
import org.jbpm.formbuilder.shared.rep.trans.LanguageFactory;

public class GuvnorFormDefinitionService implements FormDefinitionService {

    private final RestAPI api;
    
    public GuvnorFormDefinitionService() {
        super();
        try {
            Repository repository = new RepositoryFactoryImpl().getRepository(null);
            Session session = repository.login();
            RulesRepository repo = new RulesRepository(session);
            this.api = new RestAPI(repo);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String generateForm(String language, FormRepresentation form) {
        try {
            Language lang = LanguageFactory.getInstance().getLanguage(language);
            String retval = lang.translateForm(form);
            return retval;
        } catch (LanguageException e) {
            String error = ""; //TODO
            return error;
        }
    }

    public void saveForm(String url, FormRepresentation form) {
        //TODO see how to save something: api.post(url, asInputStream(form), form.getComments());
    }

}
