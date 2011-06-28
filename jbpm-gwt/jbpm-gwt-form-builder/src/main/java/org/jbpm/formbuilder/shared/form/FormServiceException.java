package org.jbpm.formbuilder.shared.form;

public class FormServiceException extends Exception {

    private static final long serialVersionUID = 1577813833697027184L;

    public FormServiceException() {
        super();
    }

    public FormServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormServiceException(String message) {
        super(message);
    }

    public FormServiceException(Throwable cause) {
        super(cause);
    }
}
