package org.jbpm.formbuilder.server.form;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.drools.repository.AssetItem;
import org.drools.repository.AssetItemIterator;
import org.drools.repository.PackageItem;
import org.drools.repository.RulesRepository;
import org.drools.repository.VersionableItem;
import org.jbpm.formbuilder.server.render.Renderer;
import org.jbpm.formbuilder.server.render.RendererException;
import org.jbpm.formbuilder.server.render.RendererFactory;
import org.jbpm.formbuilder.server.trans.Language;
import org.jbpm.formbuilder.server.trans.LanguageException;
import org.jbpm.formbuilder.server.trans.LanguageFactory;
import org.jbpm.formbuilder.shared.form.FormDefinitionService;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public class GuvnorFormDefinitionService implements FormDefinitionService {

    public static final String XFORM_TYPE = "XFORM";
    
    private final RulesRepository repo;
    
    public GuvnorFormDefinitionService(RulesRepository repo) {
        super();
        this.repo = repo;
    }
    
    public String generateForm(String pkgName, String language, FormRepresentation form) {
        try {
            Language lang = LanguageFactory.getInstance().getLanguage(language);
            URL url = lang.translateForm(form);
            Renderer render = RendererFactory.getInstance().getRenderer(language);
            PackageItem pkg = repo.loadPackage(pkgName);
            AssetItem item = pkg.loadAsset(xformName(form) + "_" + language);
            if (item == null) {
                item = pkg.addAsset(xformName(form), language + " representation for xform " + form.getDocumentation());
            }
            Object obj = render.render(url, new HashMap<String, Object>()); //TODO 
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(obj);
            item.updateBinaryContentAttachment(new ByteArrayInputStream(bout.toByteArray()));
            item.checkin("Auto-Save " + new Date());
            return url.toExternalForm();
        } catch (LanguageException e) {
            throw new RuntimeException("problem generating form", e); //TODO
        } catch (RendererException e) {
            throw new RuntimeException("problem generating form", e); //TODO
        } catch (IOException e) {
            throw new RuntimeException("problem generating form", e); //TODO
        }
    }

    public void saveForm(String pkgName, String comment, FormRepresentation form) {
        PackageItem pkg = repo.loadPackage(pkgName);
        String assetName = xformName(form);
        AssetItem item = pkg.loadAsset(assetName);
        if (item == null) {
            item = pkg.addAsset(assetName, form.getDocumentation());
            item.updateType(XFORM_TYPE);
        } else if (item.getLastModified().getTime().getTime() > form.getLastModified()) {
            throw new RuntimeException("form already changed"); //TODO
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(form);
            item.updateBinaryContentAttachment(new ByteArrayInputStream(bout.toByteArray()));
            item.checkin(comment);
            //should generate actual forms and save them.
            for (String language : LanguageFactory.getInstance().getLanguages()) {
                generateForm(pkgName, language, form);
            }
        } catch (IOException e) {
            throw new RuntimeException("problem saving form"); //TODO
        }
    }

    private String xformName(FormRepresentation form) {
        String assetName = "form_" + form.getTaskId() + "_" + form.getName();
        return assetName;
    }

    public List<FormRepresentation> getForms(String pkgName) {
        PackageItem pkg = repo.loadPackage(pkgName);
        AssetItemIterator items = pkg.queryAssets(VersionableItem.TYPE_PROPERTY_NAME + "=" + XFORM_TYPE);
        List<FormRepresentation> retval = new ArrayList<FormRepresentation>((int) items.getSize());
        while (items.hasNext()) {
            AssetItem item = items.next();
            try {
                ObjectInputStream oin = new ObjectInputStream(item.getBinaryContentAttachment());
                FormRepresentation xform = (FormRepresentation) oin.readObject();
                retval.add(xform);
            } catch (IOException e) {
                throw new RuntimeException("problem loading forms", e); //TODO
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("problem loading forms", e); //TODO
            }
        }
        return retval;
    }

    public FormRepresentation getFormByTaskId(String pkgName, String taskId) {
        // TODO Auto-generated method stub
        return null;
    }
}
