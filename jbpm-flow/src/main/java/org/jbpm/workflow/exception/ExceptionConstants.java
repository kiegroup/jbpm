package org.jbpm.workflow.exception;

/**
 * These constants are used by the other classes in the org.jbpm.workflow.exception package. 
 */
public class ExceptionConstants {

    /**
     * The code of the error that is associated with the exception-handling (error-)event subprocess.
     */
    public final static String EXCEPTION_ERROR_CODE = "jbpm.exception.error.name";
    
    /**
     * The signal type used to retry a process that has thrown an exception.
     */
    public final static String RETRY_EVENT_TYPE = "jbpm.exception.signal.retry"; 
    
    /**
     * The name of the context variable used to store exceptions that have been thrown. 
     */
    public final static String THROWN_EXCEPTIONS = "jbpm.exception.exceptions";
    
}
