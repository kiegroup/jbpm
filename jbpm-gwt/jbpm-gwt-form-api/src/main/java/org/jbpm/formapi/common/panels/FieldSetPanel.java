package org.jbpm.formapi.common.panels;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * TODO: Just in case, FieldSetPanel is implemented using
 * HTML4 components.
 */
public class FieldSetPanel extends FlowPanel {

	private HeadingElement legend = Document.get().createHElement(1);
	private final Label header;
	
	public FieldSetPanel(FocusPanel legendPanel) {
		this.header = (Label) legendPanel.getWidget();
		legend.appendChild(legendPanel.getElement());
		
		Style divStyle = getElement().getStyle();
		Style legendStyle = legend.getStyle();
		
		divStyle.setBorderWidth(2, Unit.PX);
		divStyle.setBorderStyle(BorderStyle.SOLID);
		divStyle.setMarginTop(0.5, Unit.EM);
		divStyle.setMarginBottom(0.5, Unit.EM);
		divStyle.setMarginRight(0, Unit.PX);
		divStyle.setMarginLeft(0, Unit.PX);
		divStyle.setPaddingTop(0, Unit.PX);
		divStyle.setPaddingBottom(0, Unit.PX);
		divStyle.setPaddingRight(0.5, Unit.EM);
		divStyle.setPaddingLeft(0.5, Unit.EM);

		legendStyle.setFontSize(100.0, Unit.PCT);
		legendStyle.setFontWeight(FontWeight.NORMAL);
		legendStyle.setMarginTop(-0.5, Unit.EM);
		legendStyle.setMarginRight(0, Unit.PX);
		legendStyle.setMarginLeft(0, Unit.PX);
		legendStyle.setMarginBottom(0, Unit.PX);
		legendStyle.setBackgroundColor("white");
		legendStyle.setColor("black");
		legendStyle.setFloat(Style.Float.LEFT);
		legendStyle.setPaddingTop(0, Unit.PX);
		legendStyle.setPaddingBottom(0, Unit.PX);
		legendStyle.setPaddingRight(2, Unit.PX);
		legendStyle.setPaddingLeft(2, Unit.PX);
		
		getElement().appendChild(legend);
	}
	
	public void setLegend(String legend) {
		this.header.setText(legend);
	}
	
	public String getLegend() {
		return header.getText();
	}
	
	public void setId(String id) {
		getElement().setId(id);
	}
	
	public String getId() {
		return getElement().getId();
	}
}
