package org.jbpm.bpmn2.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation should mark tests that can only be run (and can only succeed) using queue-based execution.
 */
@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface RequiresQueueBased {

    boolean value() default true;
    String comment() default "";
}
