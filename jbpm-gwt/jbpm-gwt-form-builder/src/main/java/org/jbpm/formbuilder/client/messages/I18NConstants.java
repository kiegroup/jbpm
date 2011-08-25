/**
 * Copyright 2011 JBoss Inc 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.formbuilder.client.messages;

import com.google.gwt.i18n.client.Messages;

public interface I18NConstants extends Messages {

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
    
    String CouldntFindMenuItems404();
    String CouldntFindMenuItems();
    String CouldntReadMenuItems();
    String CouldntReadMenuOptions();
    String FormItemAlreadyUpdated();
    String SaveFormItemUnknownStatus(String statusCode);
    String FormItemSaved(String name);
    String CouldntSaveFormItem();
    String CouldntSendFormItem(String formItemName);
    String CouldntDecodeFormItem(String formItemName);
    String FormAlreadyUpdated();
    String SaveFormUnkwnownStatus(String statusCode);
    String CouldntSaveForm();
    String CouldntSendForm();
    String CouldntDecodeForm();
    String CouldntPreviewForm();
    String CouldntExportTemplate();
    String MenuItemSaved(String menuItemId);
    String SaveMenuItemInvalidStatus(String statusCode);
    String CouldntFindMenuOptions();
    String CouldntGenerateMenuItem();
    String CouldntSaveMenuItem();
    String DeleteMenuItemUnkownStatus(String statusCode);
    String MenuItemDeleted();
    String ErrorDeletingMenuItem();
    String ErrorDeletingForm(String statusCode);
    String FormDeleted();
    String ErrorDeletingFormItem(String statusCode);
    String FormItemDeleted();
    String CouldntReadTasks();
    String CouldntReadSingleIO();
    String CouldntDecodeValidations();
    String CouldntReadValidations();
    
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
    String IfBlockLabel();
    String ElseBlockLabel();
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
    String BPMN2IOReferences();
    String FileIOReferences();
    String QueryLabel();
    String TypeLabel();
    String ProcessesLabel();
    
    String MenuItemTabbedLayout();
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
    String EqualToValidationName();
    String SmallerThanValidationName();
    String BiggerThanValidationName();
    
    String RemoveTabWarning();
    String RemoveRowWarning();
    String RemoveColumnWarning();
    String WarningDeleteForm(String formName);
    String WarningLocaleReload();
    String LocaleDefault();
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
    
    String AddLocaleButton();
    String CompleteButton();
    String ConfirmButton();
    String CancelButton();
    String CloseButton();
    String OkButton();
    String LoadButton();
    String ResetButton();
    String SaveChangesButton();
    String ResetChangesButton();
    String RefreshFromServerButton();
    String RefreshButtonWarning();
    String PackageLabel();
    String ProcessLabel();
    String TaskNameLabel();
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
    
    String LocalesLabel();
    String MessagesLabel();
    String ApplyLocaleFormattingLabel();
    String InternationalizeEffectLabel();
    String AddTabEffectLabel();
    String RemoveTabEffectLabel();
    String AddColumnEffectLabel();
    String RemoveColumnEffectLabel();
    String MoveItemEffectLabel();
    String AddRowEffectLabel();
    String RemoveRowEffectLabel();
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
    
    String FormatAsLabel();
    String CurrencyFormatLabel();
    String NumberFormatLabel();
    String DateFormatLabel();
    String PercentFormatLabel();
    String IntegerFormatLabel();
}
