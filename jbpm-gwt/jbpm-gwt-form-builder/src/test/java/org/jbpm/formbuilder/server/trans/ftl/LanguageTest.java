package org.jbpm.formbuilder.server.trans.ftl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.items.CheckBoxRepresentation;
import org.jbpm.formbuilder.shared.rep.items.ComboBoxRepresentation;
import org.jbpm.formbuilder.shared.rep.items.OptionRepresentation;

public class LanguageTest extends TestCase {

    public void testFormBasic() throws Exception {
        Language lang = new Language();
        FormRepresentation form = new FormRepresentation();
        form.setTaskId("taskNameXXX");
        ComboBoxRepresentation combo = new ComboBoxRepresentation();
        combo.setName("comboName");
        List<OptionRepresentation> options = new ArrayList<OptionRepresentation>();
        OptionRepresentation option1 = new OptionRepresentation();
        option1.setLabel("Label 1");
        option1.setValue("l1");
        OptionRepresentation option2 = new OptionRepresentation();
        option2.setLabel("Label 2");
        option2.setValue("l2");
        options.add(option1);
        options.add(option2);
        combo.setElements(options);
        CheckBoxRepresentation checkbox = new CheckBoxRepresentation();
        checkbox.setFormValue("S");
        checkbox.setName("checkboxName");
        form.addFormItem(combo);
        form.addFormItem(checkbox);
        String script = lang.translateForm(form);
        System.out.println("script = " + script);
    }
}
