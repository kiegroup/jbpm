package org.jbpm.formbuilder.client.effect;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.FormBuilderService;
import org.jbpm.formbuilder.client.bus.NotificationEvent;
import org.jbpm.formbuilder.client.bus.NotificationEvent.Level;
import org.jbpm.formbuilder.client.bus.ui.ValidationSavedEvent;
import org.jbpm.formbuilder.client.bus.ui.ValidationSavedHandler;
import org.jbpm.formbuilder.client.effect.view.ValidationsEffectView;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;
import org.jbpm.formbuilder.shared.rep.FBValidation;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

public class ValidationsEffect extends FBFormEffect {

    private EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private FormBuilderService server = FormBuilderGlobals.getInstance().getService();
    
    private List<FBValidation> availableValidations = new ArrayList<FBValidation>();
    private List<FBValidation> currentValidations = new ArrayList<FBValidation>();
    
    private final ValidationsEffectView effectView = new ValidationsEffectView();
    
    public ValidationsEffect() {
        super(createImage(), true);
        bus.addHandler(ValidationSavedEvent.TYPE, new ValidationSavedHandler() {
            public void onEvent(ValidationSavedEvent event) {
                currentValidations.clear();
                currentValidations.addAll(event.getValidations());
                createStyles();
            }
        });
        try {
            this.availableValidations = server.getExistingValidations();
            this.effectView.setAvailableValidations(this.availableValidations);
        } catch (FormBuilderException e) {
            bus.fireEvent(new NotificationEvent(Level.WARN, "Couldn't communicate with server", e));
        }
    }

    public static Image createImage() {
        Image img = new Image(FormBuilderResources.INSTANCE.validationsIcon());
        img.setAltText("Edit validations");
        img.setTitle("Edit validations");
        return img;
    }
    
    @Override
    protected void createStyles() {
        getItem().setValidations(currentValidations);
    }
    
    @Override
    public PopupPanel createPanel() {
        PopupPanel popup = new PopupPanel();
        popup.setWidget(effectView);
        effectView.setParentPopup(popup);
        return popup;
    }
}
