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
import org.jbpm.formbuilder.client.validation.FBValidationItem;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.PopupPanel;

public class ValidationsEffect extends FBFormEffect {

    private EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private FormBuilderService server = FormBuilderGlobals.getInstance().getService();
    
    private List<FBValidationItem> availableValidations = new ArrayList<FBValidationItem>();
    private List<FBValidationItem> currentValidations = new ArrayList<FBValidationItem>();
    
    private final ValidationsEffectView effectView = new ValidationsEffectView();
    
    public ValidationsEffect() {
        super("Edit validations", true);
        bus.addHandler(ValidationSavedEvent.TYPE, new ValidationSavedHandler() {
            public void onEvent(ValidationSavedEvent event) {
                currentValidations.clear();
                currentValidations.addAll(event.getValidations());
                createStyles();
            }
        });
    }

    @Override
    protected void createStyles() {
        getItem().setValidations(currentValidations);
    }
    
    @Override
    public PopupPanel createPanel() {
        PopupPanel popup = new PopupPanel();
        popup.setWidget(this.effectView);
        this.effectView.setParentPopup(popup);
        try {
            this.availableValidations = server.getExistingValidations();
            this.effectView.setAvailableValidations(this.availableValidations);
        } catch (FormBuilderException e) {
            bus.fireEvent(new NotificationEvent(Level.WARN, "Couldn't communicate with server", e));
        }
        return popup;
    }
}
