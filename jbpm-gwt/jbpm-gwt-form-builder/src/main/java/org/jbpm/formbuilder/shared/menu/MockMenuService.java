package org.jbpm.formbuilder.shared.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.command.EditFormRedoCommand;
import org.jbpm.formbuilder.client.command.EditFormUndoCommand;
import org.jbpm.formbuilder.client.command.PreviewFormAsFtlCommand;
import org.jbpm.formbuilder.client.command.PreviewFormAsXslCommand;
import org.jbpm.formbuilder.client.effect.AddItemFormEffect;
import org.jbpm.formbuilder.client.effect.DeleteItemFormEffect;
import org.jbpm.formbuilder.client.effect.DoneEffect;
import org.jbpm.formbuilder.client.effect.RemoveEffect;
import org.jbpm.formbuilder.client.effect.ResizeEffect;
import org.jbpm.formbuilder.client.effect.SaveAsMenuOptionFormEffect;
import org.jbpm.formbuilder.client.menu.items.CheckBoxMenuItem;
import org.jbpm.formbuilder.client.menu.items.ComboBoxMenuItem;
import org.jbpm.formbuilder.client.menu.items.CompleteButtonMenuItem;
import org.jbpm.formbuilder.client.menu.items.FileInputMenuItem;
import org.jbpm.formbuilder.client.menu.items.HTMLMenuItem;
import org.jbpm.formbuilder.client.menu.items.HeaderMenuItem;
import org.jbpm.formbuilder.client.menu.items.HiddenMenuItem;
import org.jbpm.formbuilder.client.menu.items.ImageMenuItem;
import org.jbpm.formbuilder.client.menu.items.LabelMenuItem;
import org.jbpm.formbuilder.client.menu.items.PasswordFieldMenuItem;
import org.jbpm.formbuilder.client.menu.items.RadioButtonMenuItem;
import org.jbpm.formbuilder.client.menu.items.TableLayoutMenuItem;
import org.jbpm.formbuilder.client.menu.items.TextAreaMenuItem;
import org.jbpm.formbuilder.client.menu.items.TextFieldMenuItem;

public class MockMenuService implements MenuService {

    private final Map<String, List<MenuItemDescription>> items = new HashMap<String, List<MenuItemDescription>>();
    private final List<MenuOptionDescription> options = new ArrayList<MenuOptionDescription>();
    
    public MockMenuService() {
        List<MenuItemDescription> controls = new ArrayList<MenuItemDescription>();
        List<MenuItemDescription> visuals = new ArrayList<MenuItemDescription>();
        List<MenuItemDescription> layouts = new ArrayList<MenuItemDescription>();

        List<FormEffectDescription> effects = new ArrayList<FormEffectDescription>();
        FormEffectDescription removeEffect = new FormEffectDescription();
        removeEffect.setClassName(RemoveEffect.class.getName());
        effects.add(removeEffect);
        FormEffectDescription doneEffect = new FormEffectDescription();
        doneEffect.setClassName(DoneEffect.class.getName());
        effects.add(doneEffect);
        FormEffectDescription resizeEffect = new FormEffectDescription();
        resizeEffect.setClassName(ResizeEffect.class.getName());
        effects.add(resizeEffect);
        FormEffectDescription saveMenuOption = new FormEffectDescription();
        saveMenuOption.setClassName(SaveAsMenuOptionFormEffect.class.getName());
        effects.add(saveMenuOption);
        
        List<FormEffectDescription> effectsOptions = new ArrayList<FormEffectDescription>();
        effectsOptions.add(removeEffect);
        effectsOptions.add(doneEffect);
        effectsOptions.add(resizeEffect);
        effectsOptions.add(saveMenuOption);
        FormEffectDescription addItemEffect = new FormEffectDescription();
        addItemEffect.setClassName(AddItemFormEffect.class.getName());
        effectsOptions.add(addItemEffect);
        FormEffectDescription deleteItemEffect = new FormEffectDescription();
        deleteItemEffect.setClassName(DeleteItemFormEffect.class.getName());
        effectsOptions.add(deleteItemEffect);
        
        MenuItemDescription header = new MenuItemDescription();
        header.setClassName(HeaderMenuItem.class.getName());
        header.setEffects(effects);
        visuals.add(header);
        MenuItemDescription label = new MenuItemDescription();
        label.setClassName(LabelMenuItem.class.getName());
        label.setEffects(effects);
        visuals.add(label);
        MenuItemDescription image = new MenuItemDescription();
        image.setClassName(ImageMenuItem.class.getName());
        image.setEffects(effects);
        visuals.add(image);
        MenuItemDescription html = new MenuItemDescription();
        html.setClassName(HTMLMenuItem.class.getName());
        html.setEffects(effects);
        visuals.add(html);
        items.put("Visual Components", visuals);
        
        
        MenuItemDescription combo = new MenuItemDescription();
        combo.setClassName(ComboBoxMenuItem.class.getName());
        combo.setEffects(effectsOptions);
        controls.add(combo);
        MenuItemDescription textfield = new MenuItemDescription();
        textfield.setClassName(TextFieldMenuItem.class.getName());
        textfield.setEffects(effects);
        controls.add(textfield);
        MenuItemDescription password = new MenuItemDescription();
        password.setClassName(PasswordFieldMenuItem.class.getName());
        password.setEffects(effects);
        controls.add(password);
        MenuItemDescription completeButton = new MenuItemDescription();
        completeButton.setClassName(CompleteButtonMenuItem.class.getName());
        completeButton.setEffects(effects);
        controls.add(completeButton);
        MenuItemDescription textarea = new MenuItemDescription();
        textarea.setClassName(TextAreaMenuItem.class.getName());
        textarea.setEffects(effects);
        controls.add(textarea);
        MenuItemDescription hidden = new MenuItemDescription();
        hidden.setClassName(HiddenMenuItem.class.getName());
        hidden.setEffects(effects);
        controls.add(hidden);
        MenuItemDescription fileInput = new MenuItemDescription();
        fileInput.setClassName(FileInputMenuItem.class.getName());
        fileInput.setEffects(effects);
        controls.add(fileInput);
        MenuItemDescription checkbox = new MenuItemDescription();
        checkbox.setClassName(CheckBoxMenuItem.class.getName());
        checkbox.setEffects(effects);
        controls.add(checkbox);
        MenuItemDescription radioButton = new MenuItemDescription();
        radioButton.setClassName(RadioButtonMenuItem.class.getName());
        radioButton.setEffects(effects);
        controls.add(radioButton);
        items.put("Control Components", controls);

        MenuItemDescription tableLayout = new MenuItemDescription();
        tableLayout.setClassName(TableLayoutMenuItem.class.getName());
        tableLayout.setEffects(effects);
        layouts.add(tableLayout);
        items.put("Layout Components", layouts);
        
        MenuOptionDescription saveOption = new MenuOptionDescription();
        saveOption.setHtml("Save");
        
        List<MenuOptionDescription> saveMenu = new ArrayList<MenuOptionDescription>();

        MenuOptionDescription saveFtl = new MenuOptionDescription();
        saveFtl.setHtml("As FTL");
        saveFtl.setCommandClass(PreviewFormAsFtlCommand.class.getName());
        
        MenuOptionDescription saveXsl = new MenuOptionDescription();
        saveXsl.setHtml("As XSL");
        saveXsl.setCommandClass(PreviewFormAsXslCommand.class.getName());
        
        saveMenu.add(saveFtl);
        saveMenu.add(saveXsl);
        
        saveOption.setSubMenu(saveMenu);
        
        MenuOptionDescription editOption = new MenuOptionDescription();
        editOption.setHtml("Edit");
        
        List<MenuOptionDescription> editMenu = new ArrayList<MenuOptionDescription>();
        
        MenuOptionDescription editUndo = new MenuOptionDescription();
        editUndo.setHtml("Undo");
        editUndo.setCommandClass(EditFormUndoCommand.class.getName());
        
        MenuOptionDescription editRedo = new MenuOptionDescription();
        editRedo.setHtml("Redo");
        editRedo.setCommandClass(EditFormRedoCommand.class.getName());

        editMenu.add(editUndo);
        editMenu.add(editRedo);
        
        editOption.setSubMenu(editMenu);
        
        options.add(saveOption);
        options.add(editOption);
    }
    
    public List<MenuOptionDescription> listOptions() {
        return options;
    }

    public Map<String, List<MenuItemDescription>> listItems() {
        return items;
    }

    public void save(MenuItemDescription item) {
        Map<String, List<MenuItemDescription>> items = listItems();
        List<MenuItemDescription> customItems = items.get("Custom");
        if (customItems == null) {
            customItems = new ArrayList<MenuItemDescription>();
        }
        customItems.add(item);
        items.put("Custom", customItems);
    }

    public void delete(MenuItemDescription item) {
        Map<String, List<MenuItemDescription>> items = listItems();
        List<MenuItemDescription> customItems = items.get("Custom");
        if (customItems == null) {
            customItems = new ArrayList<MenuItemDescription>();
        }
        customItems.remove(item);
        items.put("Custom", customItems);
    }

}
