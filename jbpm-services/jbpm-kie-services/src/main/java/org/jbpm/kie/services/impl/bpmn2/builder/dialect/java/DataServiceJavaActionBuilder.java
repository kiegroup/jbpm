package org.jbpm.kie.services.impl.bpmn2.builder.dialect.java;

import org.drools.compiler.compiler.AnalysisResult;
import org.drools.compiler.lang.descr.ActionDescr;
import org.drools.compiler.rule.builder.PackageBuildContext;
import org.jbpm.kie.services.impl.bpmn2.ProcessDescRepoHelper;
import org.jbpm.kie.services.impl.bpmn2.builder.DataServiceExpressionBuilder;
import org.jbpm.process.builder.dialect.java.JavaActionBuilder;
import org.jbpm.process.core.ContextResolver;
import org.jbpm.workflow.core.DroolsAction;

class DataServiceJavaActionBuilder extends JavaActionBuilder implements DataServiceExpressionBuilder {

    private static final ThreadLocal<ProcessDescRepoHelper> threadLocalHelper 
        = new ThreadLocal<ProcessDescRepoHelper>();

    @Override
    public void setThreadLocalHelper( ProcessDescRepoHelper helper ) {
       threadLocalHelper.set(helper);
    }

    @Override
    public ProcessDescRepoHelper getThreadLocalHelper() {
       return threadLocalHelper.get();
    }
    
    @Override
    public void build( PackageBuildContext context, 
                       DroolsAction action, 
                       ActionDescr actionDescr, 
                       ContextResolver contextResolver ) {
       
        String className = getClassName(context);
        AnalysisResult analysis = getAnalysis(context, actionDescr);
       
        // TODO: retrieve info from AnalysiResult instance 
        //       and put it in the helper.. 
       
        // no need to actually compile the return value expr here
    }
  
}
