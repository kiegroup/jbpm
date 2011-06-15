package org.jbpm.formbuilder.server.menu;

import java.io.File; 
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.command.BaseCommand;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.options.MainMenuOption;
import org.jbpm.formbuilder.shared.menu.MenuService;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class GuvnorMenuService implements MenuService {

    public List<MainMenuOption> listOptions() {
        Gson gson = new Gson();
        URL url = getClass().getResource("/menuOptions.json");
        List<MainMenuOption> retval = new ArrayList<MainMenuOption>();
        try {
            File menuOptionsFile = new File(url.toURI());
            Collection<MenuOptionDescription> options = gson.fromJson(
                    new FileReader(menuOptionsFile), 
                new TypeToken<Collection<MenuOptionDescription>>() {}.getType());
            retval.addAll(jsonToOption(options));
        } catch (URISyntaxException e) {
            //TODO throw error
        } catch (FileNotFoundException e) {
            //TODO throw error
        } catch (FormBuilderException e) {
            //TODO throw error
        }
        return retval;
    }

    private List<MainMenuOption> jsonToOption(Collection<MenuOptionDescription> options) throws FormBuilderException {
        List<MainMenuOption> retval = new ArrayList<MainMenuOption>(options.size());
        for (MenuOptionDescription desc : options) {
            MainMenuOption option = new MainMenuOption();
            option.setHtml(desc.getHtml());
            if (desc.getSubMenu() != null && !desc.getSubMenu().isEmpty()) {
                option.setSubMenu(jsonToOption(desc.getSubMenu()));
            }
            if (desc.getCommandClass() != null && !"".equals(desc.getCommandClass())) {
                try {
                    Class<?> klass = Class.forName(desc.getCommandClass());
                    Object obj = klass.newInstance();
                    if (obj instanceof BaseCommand) {
                        option.setCommand((BaseCommand) obj);
                    } else {
                        throw new FormBuilderException("Command class " + desc.getCommandClass() + " not of type " + BaseCommand.class.getName());
                    }
                } catch (ClassNotFoundException e) {
                    throw new FormBuilderException("Couldn't find command class " + desc.getCommandClass(), e);
                } catch (IllegalAccessException e) {
                    throw new FormBuilderException("Couldn't access constructor for command class " + desc.getCommandClass(), e);
                } catch (InstantiationException e) {
                    throw new FormBuilderException("Couldn't instantiate command class " + desc.getCommandClass(), e);
                }
            }
            retval.add(option);
        }
        return retval;
    }

    public Map<String, List<FBMenuItem>> listItems() {
        Gson gson = new Gson();
        URL url = getClass().getResource("/menuItems.json");
        Map<String, List<FBMenuItem>> retval = new HashMap<String, List<FBMenuItem>>();
        try {
            File menuItemsFile = new File(url.toURI());
            Map<String, List<MenuItemDescription>> items = gson.fromJson(
                    new FileReader(menuItemsFile), 
                new TypeToken<Map<String, List<MenuItemDescription>>>() {}.getType());
            for (Map.Entry<String, List<MenuItemDescription>> item : items.entrySet()) {
                List<FBMenuItem> listItems = new ArrayList<FBMenuItem>();
                List<MenuItemDescription> descs = item.getValue();
                for (MenuItemDescription desc : descs) {
                    List<FBFormEffect> effects = new ArrayList<FBFormEffect>();
                    for (FormEffectDescription effDesc : desc.getEffects()) {
                        Class<?> klass = Class.forName(effDesc.getClassName());
                        Object obj = klass.newInstance();
                        if (obj instanceof FBFormEffect) {
                            effects.add((FBFormEffect) obj);
                        }
                    }
                    Class<?> klass = Class.forName(desc.getClassName());
                    Constructor<?> cons = klass.getConstructor(List.class);
                    Object obj = cons.newInstance(effects);
                    if (obj instanceof FBMenuItem) {
                        listItems.add((FBMenuItem) obj);
                    } else {
                        //TODO throw error
                    }
                }
                retval.put(item.getKey(), listItems);
            }
        } catch (URISyntaxException e) {
            //TODO throw error
        } catch (FileNotFoundException e) {
            //TODO throw error
        } catch (ClassNotFoundException e) {
            //TODO throw error
        } catch (InstantiationException e) {
            //TODO throw error
        } catch (IllegalAccessException e) {
            //TODO throw error
        } catch (NoSuchMethodException e) {
            //TODO throw error
        } catch (InvocationTargetException e) {
            //TODO throw error
        }
        return retval;
    }

    public void save(FBMenuItem item) {
        Map<String, List<FBMenuItem>> items = listItems();
        List<FBMenuItem> customItems = items.get("Custom");
        if (customItems == null) {
            customItems = new ArrayList<FBMenuItem>();
        }
        customItems.add(item);
        items.put("Custom", customItems);
        try {
            saveToFile(items);
        } catch (URISyntaxException e) {
            //TODO throw error
        } catch (IOException e) {
            //TODO throw error
        }
    }

    private void saveToFile(Map<String, List<FBMenuItem>> items)
            throws URISyntaxException, IOException {
        Map<String, List<MenuItemDescription>> store = toDescription(items);
        Gson gson = new Gson();
        URL url = getClass().getResource("/menuItems.json");
        String json = gson.toJson(store, new TypeToken<Map<String, List<MenuItemDescription>>>() {}.getType());
        File menuItemsFile = new File(url.toURI());
        FileWriter writer = new FileWriter(menuItemsFile);
        writer.write(json);
        writer.flush();
        writer.close();
    }

    private Map<String, List<MenuItemDescription>> toDescription(
            Map<String, List<FBMenuItem>> items) {
        Map<String, List<MenuItemDescription>> store = new HashMap<String, List<MenuItemDescription>>();
        for (Map.Entry<String, List<FBMenuItem>> entry : items.entrySet()) {
            List<MenuItemDescription> descs = new ArrayList<MenuItemDescription>();
            for (FBMenuItem item : entry.getValue()) {
                MenuItemDescription desc = new MenuItemDescription();
                desc.setClassName(item.getClass().getName());
                List<FormEffectDescription> effects = new ArrayList<FormEffectDescription>();
                for (FBFormEffect effect : item.getFormEffects()) {
                    FormEffectDescription descEffect = new FormEffectDescription();
                    descEffect.setClassName(effect.getClass().getName());
                    effects.add(descEffect);
                }
                desc.setEffects(effects);
                descs.add(desc);
            }
            store.put(entry.getKey(), descs);
        }
        return store;
    }

    public void delete(FBMenuItem item) {
        Map<String, List<FBMenuItem>> items = listItems();
        List<FBMenuItem> customItems = items.get("Custom");
        if (customItems == null) {
            customItems = new ArrayList<FBMenuItem>();
        }
        customItems.remove(item);
        items.put("Custom", customItems);
        try {
            saveToFile(items);
        } catch (URISyntaxException e) {
            //TODO throw error
        } catch (IOException e) {
            //TODO throw error
        }
    }

}
