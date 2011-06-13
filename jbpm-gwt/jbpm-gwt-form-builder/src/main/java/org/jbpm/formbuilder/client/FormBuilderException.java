package org.jbpm.formbuilder.client;

public class FormBuilderException extends Exception {

    private static final long serialVersionUID = 3248011011993977193L;

    public FormBuilderException() {
        super();
    }

    public FormBuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormBuilderException(String message) {
        super(message);
    }

    public FormBuilderException(Throwable cause) {
        super(cause);
    }
}
