package org.jbpm.formbuilder.client;

import org.gwt.mosaic.ui.client.DesktopPanel;
import org.gwt.mosaic.ui.client.WindowPanel;

public class UIUtils {

    public static WindowPanel createWindow(String title) {
        return new WindowPanel(new DesktopPanel() {
            @Override
            public void makeDraggable(WindowPanel w) {
                //do nothing to avoid gwt-dnd issue 43
            }
        }, title, false, true);
    }
}
