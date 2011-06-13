package org.jbpm.formbuilder.shared.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.command.EditFormRedoCommand;
import org.jbpm.formbuilder.client.command.EditFormUndoCommand;
import org.jbpm.formbuilder.client.command.SaveFormAsFtlCommand;
import org.jbpm.formbuilder.client.command.SaveFormAsXslCommand;
import org.jbpm.formbuilder.client.effect.AddItemFormEffect;
import org.jbpm.formbuilder.client.effect.DeleteItemFormEffect;
import org.jbpm.formbuilder.client.effect.DoneEffect;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.effect.RemoveEffect;
import org.jbpm.formbuilder.client.effect.ResizeEffect;
import org.jbpm.formbuilder.client.effect.SaveAsMenuOptionFormEffect;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.menu.items.AbsoluteLayoutMenuItem;
import org.jbpm.formbuilder.client.menu.items.CheckBoxMenuItem;
import org.jbpm.formbuilder.client.menu.items.ComboBoxMenuItem;
import org.jbpm.formbuilder.client.menu.items.CompleteButtonMenuItem;
import org.jbpm.formbuilder.client.menu.items.FileInputMenuItem;
import org.jbpm.formbuilder.client.menu.items.HTMLMenuItem;
import org.jbpm.formbuilder.client.menu.items.HeaderMenuItem;
import org.jbpm.formbuilder.client.menu.items.HiddenMenuItem;
import org.jbpm.formbuilder.client.menu.items.HorizontalLayoutMenuItem;
import org.jbpm.formbuilder.client.menu.items.ImageMenuItem;
import org.jbpm.formbuilder.client.menu.items.LabelMenuItem;
import org.jbpm.formbuilder.client.menu.items.PasswordFieldMenuItem;
import org.jbpm.formbuilder.client.menu.items.RadioButtonMenuItem;
import org.jbpm.formbuilder.client.menu.items.TableLayoutMenuItem;
import org.jbpm.formbuilder.client.menu.items.TextAreaMenuItem;
import org.jbpm.formbuilder.client.menu.items.TextFieldMenuItem;
import org.jbpm.formbuilder.client.options.MainMenuOption;

public class MockMenuService implements MenuService {

    private final Map<String, List<FBMenuItem>> items = new HashMap<String, List<FBMenuItem>>();
    private final List<MainMenuOption> options = new ArrayList<MainMenuOption>();
    
    public MockMenuService() {
        List<FBMenuItem> controls = new ArrayList<FBMenuItem>();
        List<FBMenuItem> visuals = new ArrayList<FBMenuItem>();
        List<FBMenuItem> layouts = new ArrayList<FBMenuItem>();

        List<FBFormEffect> effects = new ArrayList<FBFormEffect>();
        effects.add(new RemoveEffect());
        effects.add(new DoneEffect());
        effects.add(new ResizeEffect());
        effects.add(new SaveAsMenuOptionFormEffect());
        
        List<FBFormEffect> effectsOptions = new ArrayList<FBFormEffect>();
        effectsOptions.add(new RemoveEffect());
        effectsOptions.add(new DoneEffect());
        effectsOptions.add(new ResizeEffect());
        effectsOptions.add(new SaveAsMenuOptionFormEffect());
        effectsOptions.add(new AddItemFormEffect());
        effectsOptions.add(new DeleteItemFormEffect());
        
        visuals.add(new HeaderMenuItem(effects));
        visuals.add(new LabelMenuItem(effects));
        visuals.add(new ImageMenuItem(effects));
        visuals.add(new HTMLMenuItem(effects));
        items.put("Visual Components", visuals);
        
        controls.add(new ComboBoxMenuItem(effectsOptions));
        controls.add(new TextFieldMenuItem(effects));
        controls.add(new PasswordFieldMenuItem(effects));
        controls.add(new CompleteButtonMenuItem(effects));
        controls.add(new TextAreaMenuItem(effects));
        controls.add(new HiddenMenuItem(effects));
        controls.add(new FileInputMenuItem(effects));
        controls.add(new CheckBoxMenuItem(effects));
        controls.add(new RadioButtonMenuItem(effects));
        items.put("Control Components", controls);
        
        layouts.add(new HorizontalLayoutMenuItem(effects));
        layouts.add(new TableLayoutMenuItem(effects));
        layouts.add(new AbsoluteLayoutMenuItem(effects));
        items.put("Layout Components", layouts);
        
        MainMenuOption saveOption = new MainMenuOption();
        saveOption.setHtml("Save");
        
        List<MainMenuOption> saveMenu = new ArrayList<MainMenuOption>();

        MainMenuOption saveFtl = new MainMenuOption();
        saveFtl.setHtml("As FTL");
        saveFtl.setCommand(new SaveFormAsFtlCommand());
        
        MainMenuOption saveXsl = new MainMenuOption();
        saveXsl.setHtml("As XSL");
        saveXsl.setCommand(new SaveFormAsXslCommand());
        
        saveMenu.add(saveFtl);
        saveMenu.add(saveXsl);
        
        saveOption.setSubMenu(saveMenu);
        
        MainMenuOption editOption = new MainMenuOption();
        editOption.setHtml("Edit");
        
        List<MainMenuOption> editMenu = new ArrayList<MainMenuOption>();
        
        MainMenuOption editUndo = new MainMenuOption();
        editUndo.setHtml("Undo");
        editUndo.setCommand(new EditFormUndoCommand());
        
        MainMenuOption editRedo = new MainMenuOption();
        editRedo.setHtml("Redo");
        editRedo.setCommand(new EditFormRedoCommand());

        editMenu.add(editUndo);
        editMenu.add(editRedo);
        
        editOption.setSubMenu(editMenu);
        
        options.add(saveOption);
        options.add(editOption);
    }
    
    public List<MainMenuOption> listOptions() {
        return options;
    }

    public Map<String, List<FBMenuItem>> listItems() {
        return items;
    }

    public void save(FBMenuItem item) {
        Map<String, List<FBMenuItem>> items = listItems();
        List<FBMenuItem> customItems = items.get("Custom");
        if (customItems == null) {
            customItems = new ArrayList<FBMenuItem>();
        }
        customItems.add(item);
        items.put("Custom", customItems);
    }

    public void delete(FBMenuItem item) {
        Map<String, List<FBMenuItem>> items = listItems();
        List<FBMenuItem> customItems = items.get("Custom");
        if (customItems == null) {
            customItems = new ArrayList<FBMenuItem>();
        }
        customItems.remove(item);
        items.put("Custom", customItems);
    }

}
