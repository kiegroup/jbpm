package org.jbpm.services.cdi.test.humantaskservice;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.inject.Alternative;
import javax.inject.Qualifier;

/**
 * Used to inject a custom HumanTaskServiceProducer.
 * 
 */
@Alternative
@Target({TYPE, FIELD, METHOD, PARAMETER})
@Retention(RUNTIME)
@Qualifier
public @interface CustomHumanTaskService {

}
