package org.jbpm.formbuilder.client.command;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuItem;

public interface BaseCommand extends Command {

    void setItem(MenuItem item);
}
