package org.jbpm.kie.services.impl.bpmn2.builder.dialect.mvel;

import java.util.HashMap;
import java.util.Map;

import org.drools.compiler.compiler.AnalysisResult;
import org.drools.compiler.compiler.DescrBuildError;
import org.drools.compiler.compiler.ReturnValueDescr;
import org.drools.compiler.rule.builder.PackageBuildContext;
import org.drools.compiler.rule.builder.dialect.mvel.MVELAnalysisResult;
import org.drools.compiler.rule.builder.dialect.mvel.MVELDialect;
import org.jbpm.kie.services.impl.bpmn2.ProcessDescRepoHelper;
import org.jbpm.kie.services.impl.bpmn2.builder.DataServiceExpressionBuilder;
import org.jbpm.process.builder.dialect.java.JavaReturnValueEvaluatorBuilder;
import org.jbpm.process.builder.dialect.mvel.MVELReturnValueEvaluatorBuilder;
import org.jbpm.process.core.ContextResolver;
import org.jbpm.process.instance.impl.ReturnValueConstraintEvaluator;

public class DataServiceMvelReturnValueEvaluatorBuilder extends MVELReturnValueEvaluatorBuilder 
    implements DataServiceExpressionBuilder {

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
        
        String text = descr.getText();
        Map<String, Class<?>> variables = new HashMap<String,Class<?>>();

        try {
            MVELDialect dialect = (MVELDialect) context.getDialect( "mvel" );

            MVELAnalysisResult analysis = getAnalysis(context, descr, dialect, text, variables);

            // TODO: extract info from analysis and put it in the thread local helper
        
            // no need to actually compile the return value expr here
        } catch ( final Exception e ) {
            context.getErrors().add( new DescrBuildError( context.getParentDescr(),
                                                          descr,
                                                          null,
                                                          "Unable to build expression for 'constraint' " + descr.getText() + "': " + e ) );
        }
    }
}
