package org.jbpm.formbuilder.shared.task;

public class TaskServiceException extends Exception {

    private static final long serialVersionUID = 5114462867591294337L;

    public TaskServiceException() {
        super();
    }

    public TaskServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskServiceException(String message) {
        super(message);
    }

    public TaskServiceException(Throwable cause) {
        super(cause);
    }
}
