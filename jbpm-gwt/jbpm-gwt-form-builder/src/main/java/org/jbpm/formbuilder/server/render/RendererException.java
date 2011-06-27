package org.jbpm.formbuilder.server.render;

public class RendererException extends Exception {

    private static final long serialVersionUID = -3668013785910609033L;

    public RendererException() {
    }

    public RendererException(String message) {
        super(message);
    }

    public RendererException(Throwable cause) {
        super(cause);
    }

    public RendererException(String message, Throwable cause) {
        super(message, cause);
    }

}
