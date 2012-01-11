package org.jbpm.formbuilder.client.effect.view;

import java.util.List;

import org.jbpm.formapi.client.CommonGlobals;
import org.jbpm.formapi.client.FormBuilderException;
import org.jbpm.formapi.client.bus.ui.NotificationEvent;
import org.jbpm.formapi.client.bus.ui.NotificationEvent.Level;
import org.jbpm.formapi.common.panels.ConfirmDialog;
import org.jbpm.formbuilder.client.FormBuilderGlobals;
import org.jbpm.formbuilder.client.FormBuilderService;
import org.jbpm.formbuilder.client.messages.I18NConstants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class FilesDataPanel extends ScrollPanel {

	private final EventBus bus = CommonGlobals.getInstance().getEventBus();
	private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
	private final FormBuilderService server = FormBuilderGlobals.getInstance().getService();
	private boolean isEmpty = true;
	
	FlexTable table = new FlexTable();
	
	public FilesDataPanel() {
		setHeight("200px");
		setWidget(table);
	}
	
	public void setFiles(List<String> files) {
		table.clear();
		if (files != null && !files.isEmpty()) {
			isEmpty = false;
			for (int row = 0; row < files.size(); row++) {
				final String url = files.get(row);
				final Label label = createLabel(url);
				table.setWidget(row, 0, label);
				Element rowElem = table.getRowFormatter().getElement(row);
				table.setWidget(row, 1, createDeleteButton(rowElem, url));
			}
		} else {
			table.setWidget(0, 0, new Label(i18n.NoFilesFound()));
		}
	}

	private Label createLabel(final String url) {
		final Label label = new Label(url);
		label.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (label.getStyleName().equals("fbFilesDataPanelSelected")) {
					label.setStyleName("fbFilesDataPanel");
					setSelection(null);
				} else {
					deselectAllLabels();
					label.setStyleName("fbFilesDataPanel");
					setSelection(url);
				}
			}
		});
		return label;
	}
	
	private void deselectAllLabels() {
		for (int row = 0; row < table.getRowCount(); row++) {
			Widget widget = table.getWidget(row, 0);
			if (widget != null && widget.getStyleName().equals("fbFilesDataPanelSelected")) {
				widget.setStyleName("fbFilesDataPanel");
			}
		}
	}
	
	private Button createDeleteButton(final Element rowElem, final String url) {
		return new Button(i18n.RemoveButton(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ConfirmDialog dialog = new ConfirmDialog(i18n.WarningDeleteFile());
				dialog.addOkButtonHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						try {
							server.deleteFile(url);
							RowFormatter formatter = table.getRowFormatter();
							int rowNumber = 0;
							for (; rowNumber < table.getRowCount(); rowNumber++) {
								if (formatter.getElement(rowNumber).equals(rowElem)) {
									break;
								}
							}
							table.removeRow(rowNumber);
						} catch (FormBuilderException e) {
							bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.Error(e.getMessage()), e));
						}
					}
				});
			}
		});
	}

	private String selection = null;
	
	public String getSelection() {
		return selection;
	}
	
	public void setSelection(String selection) {
		this.selection = selection;
	}

	public String toFileName(String url) {
		return url.substring(url.lastIndexOf('/'));
	}
	
	public void addNewFile(String url) {
		url = toFileName(url);
		final Label label = createLabel(url);
		if (isEmpty) {
			table.clear();
		}
		int row = table.getRowCount();
		table.setWidget(row, 0, label);
		Element rowElem = table.getRowFormatter().getElement(row);
		table.setWidget(row, 1, createDeleteButton(rowElem, url));
		setSelection(url);
	}
}
