package org.jbpm.kie.services.impl.bpmn2.builder.dialect.java;

import org.drools.compiler.compiler.AnalysisResult;
import org.drools.compiler.compiler.ReturnValueDescr;
import org.drools.compiler.rule.builder.PackageBuildContext;
import org.jbpm.kie.services.impl.bpmn2.ProcessDescRepoHelper;
import org.jbpm.kie.services.impl.bpmn2.builder.DataServiceExpressionBuilder;
import org.jbpm.process.builder.dialect.java.JavaReturnValueEvaluatorBuilder;
import org.jbpm.process.core.ContextResolver;
import org.jbpm.process.instance.impl.ReturnValueConstraintEvaluator;

class DataServiceJavaReturnValueEvaluatorBuilder extends JavaReturnValueEvaluatorBuilder implements DataServiceExpressionBuilder {

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
    public void build( PackageBuildContext context, ReturnValueConstraintEvaluator constraintNode, ReturnValueDescr descr,
            ContextResolver contextResolver ) {
       
        String className = getClassName(context);
        AnalysisResult analysis = getAnalysis(context, descr);

        // TODO: extract info from analysis and put it in the thread local helper
       
        // no need to actually compile the return value expr here?
    }
}
