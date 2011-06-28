package org.jbpm.formbuilder.shared.form;

public class FormEncodingException extends Exception {

    private static final long serialVersionUID = 5810473419363539681L;

    public FormEncodingException() {
        super();
    }

    public FormEncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormEncodingException(String message) {
        super(message);
    }

    public FormEncodingException(Throwable cause) {
        super(cause);
    }
}
