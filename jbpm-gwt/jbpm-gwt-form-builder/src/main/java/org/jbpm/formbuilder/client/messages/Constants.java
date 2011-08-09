package org.jbpm.formbuilder.client.messages;

import com.google.gwt.i18n.client.Messages;

public interface Constants extends Messages {

    String ErrorInTheUI();
    String CouldntLoadFromEmbeded();
    String ProblemLoadingRepresentationFactory();
    String ProblemCreatingMenuItems();
    String ProblemCreatingMenuOptions();
    String Error(String message);
    String NotOfType(String name, String type);
    String CouldntExportAsJson();
    String CouldntFindForm(String formName);
    String CouldntFindForm404(String formName);
    String CouldntFindForms404();
    String CouldntFindForms();
    String CouldntReadRepresentationMappings();
    String CouldntConnectServer();
    String CouldntCreateValidation();
    String CouldntPopulateAutocomplete();
    String FormItemShouldntBeNull();
    String Notifications();
    String CausedBy();
    String StackTraceLine(String className, String methodName, String fileName, String lineNumber);
    String UnexpectedWhilePreviewForm(String formType);
    String UnexpectedWhileExportForm(String formType);
    String ProblemRestoringForm();
    String ProblemDeletingForm();
    String FormWasNeverSaved();
    String CouldntLoadForm(String formName);
    String CouldntLoadAllForms();
    String SelectAFormLabel();
    String AddValidationButton();
    String ValidationTypeLabel();
    String ValidationRemove();
    String ValidationMoveUp();
    String ValidationModeDown();
    String CurrentValidations();
    String BorderLayoutPositionPopulated();
    String ConditionalBlockFull();
    String LoopBlockFull();
    String TableFull();
    String InputMapPopulation();
    String FormSavedSuccessfully(String formName);
    String ProblemSavingForm(String formName);
    String DefineFormNameFirst();
    String CouldntInstantiateClass(String className);
    String ExpectedJsonObject(String jsonThing);
    String CouldntPopulateWithForm();
    String CannotCastTo(String objClass, String castClass);
    String SelectIoConfig();
    String LabelInput();
    String LabelOutput();
    
    String MenuItemAbsoluteLayout();
    String MenuItemBorderLayout();
    String MenuItemCheckBox();
    String MenuItemComboBox();
    String MenuItemHorizontalLayout();
    String MenuItemCompleteButton();
    String MenuItemConditionalBlock();
    String MenuItemCSSLayout();
    String MenuItemFileInput();
    String MenuItemTextField();
    String MenuItemHiddenField();
    String MenuItemImage();
    String MenuItemHTMLScript();
    String MenuItemServerScript();
    String MenuItemTextArea();
    String MenuItemRadioButton();
    String MenuItemTableLayout();
    String MenuItemFlowLayout();
    String MenuItemHeader();
    String MenuItemLabel();
    String MenuItemPasswordField();
    String MenuItemLoopBlock();
    String MenuItemLineGraph();
    
    String NotEmptyValidationName();
    
    String WarningDeleteForm(String formName);
    String RepNotOfType(String repClass, String expectedClass);
    String EditionPropertyName();
    String EditionPropertyValue();
    String InputNameLabel();
    String InputExpressionLabel();
    String OutputNameLabel();
    String OutputExpressionLabel();
    String MetaDataNameLabel();
    String MetaDataValueLabel();
    String NoIoRefsFound();
    String SelectIOObjectCommand();
    String QuickFormIOObjectCommand();
    String QuickFormInputsToBeAdded();
    String QuickFormOutputsToBeAdded();
    String QuickFormWarning();
    
    String CompleteButton();
    String ConfirmButton();
    String CancelButton();
    String CloseButton();
    String OkButton();
    String LoadButton();
    String ResetButton();
    String SaveChangesButton();
    String ResetChangesButton();
    String SearchButton();
    String SimpleSearch();
    String AdvancedSearch();
    
    String SearchIOAssociations();
    String HTMLEditorHTML();
    String HTMLEditorText();
    
    String MenuOptionName();
    String MenuOptionGroup();
    String RemoveMenuItem();
    String NewItemLabel();
    String NewItemValue();

    String HorizontalAlignment();
    String Alignment();
    String AlignLeft();
    String AlignRight();
    String AlignCenter();
    String AlignJustify();
    
    String DoneEffectLabel();
    String IoBindingEffectLabel();
    String RemoveEffectLabel();
    String ResizeEffectLabel();
    String PasteFormEffectLabel();
    String CopyFormEffectLabel();
    String CutFormEffectLabel();
    String DeleteItemFormEffectLabel();
    String ValidationsEffectLabel();
    String AddItemFormEffectLabel();
    String LabelToDelete();
    
    String CheckInComment();
    String FormAction();
    String FormMethod();
    String FormEnctype();
    String FormProcessId();
    String FormTaskId();
    String FormName();
}
