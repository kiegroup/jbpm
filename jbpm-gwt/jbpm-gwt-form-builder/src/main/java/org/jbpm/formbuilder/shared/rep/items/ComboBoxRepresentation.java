package org.jbpm.formbuilder.shared.rep.items;

import java.util.List;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.Script;

public class ComboBoxRepresentation extends FormItemRepresentation {

    private List<OptionRepresentation> elements;
    private Script elementsPopulationScript;
    private String name;
    private String id;

    public List<OptionRepresentation> getElements() {
        return elements;
    }

    public void setElements(List<OptionRepresentation> elements) {
        this.elements = elements;
    }

    public Script getElementsPopulationScript() {
        return elementsPopulationScript;
    }

    public void setElementsPopulationScript(Script elementsPopulationScript) {
        this.elementsPopulationScript = elementsPopulationScript;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
