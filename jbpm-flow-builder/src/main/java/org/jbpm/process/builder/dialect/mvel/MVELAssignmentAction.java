/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.process.builder.dialect.mvel;

import java.util.function.BiFunction;
import java.util.regex.Matcher;

import org.drools.mvel.MVELSafeHelper;
import org.jbpm.process.instance.impl.AssignmentAction;
import org.jbpm.process.instance.impl.AssignmentProducer;
import org.jbpm.util.PatternConstants;
import org.jbpm.workflow.core.node.Assignment;
import org.jbpm.workflow.instance.impl.NodeInstanceResolverFactory;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessContext;

public class MVELAssignmentAction implements AssignmentAction {

    private String to;
    private String from;
    private String srcExpr;
    private String targetExpr;
    private AssignmentProducer producer;
    private BiFunction<ProcessContext, NodeInstance, Object> src;
    private BiFunction<ProcessContext, NodeInstance, Object> target;

    private static final String THIS = "this";

    public MVELAssignmentAction(Assignment assignment, String sourceExpr, String targetExpr,
                                BiFunction<ProcessContext, NodeInstance, Object> source,
                                BiFunction<ProcessContext, NodeInstance, Object> target, AssignmentProducer producer) {
        Matcher fromMatcher = PatternConstants.PARAMETER_MATCHER.matcher(assignment.getFrom());
        Matcher toMatcher = PatternConstants.PARAMETER_MATCHER.matcher(assignment.getTo());
        this.from = fromMatcher.find() ? fromMatcher.group(1) : assignment.getFrom();
        this.to = toMatcher.find() ? toMatcher.group(1) : assignment.getTo();
        this.src = source;
        this.target = target;
        this.srcExpr = sourceExpr;
        this.targetExpr = targetExpr;
        this.producer = producer;
    }

    @Override
    public void execute(NodeInstance nodeInstance, ProcessContext context) {
        Object targetObject = targetExpr != null ? target.apply(context, nodeInstance) : null;
        Object srcObject = srcExpr != null ? src.apply(context, nodeInstance) : null;
        NodeInstanceResolverFactory resolver = new NodeInstanceResolverFactory(
                (org.jbpm.workflow.instance.NodeInstance) nodeInstance);
        // just evaluating, not assignment
        if (targetExpr != null && targetObject == null || notEvalTarget()) {
            targetObject = notEvalSrc() ? srcObject : MVELSafeHelper.getEvaluator().eval(from, srcObject, resolver);
        } else {
            if (srcExpr != null) {
                resolver.addExtraParameter(srcExpr, srcObject);
            }
            if (targetExpr != null) {
                resolver.addExtraParameter(targetExpr, targetObject);
            }
            MVELSafeHelper.getEvaluator().eval(ensureLocated(targetExpr, to).concat("=").concat(ensureLocated(srcExpr,
                    from)), resolver);
        }
        if (targetExpr != null) {
            producer.accept(context, nodeInstance, targetObject);
        }
    }

    private static boolean isThis(String assignmentExpr) {
        return assignmentExpr.equals(".") || assignmentExpr.equals(THIS);
    }

    private static boolean notEval(String assignmentExpr, String expr) {
        return isThis(assignmentExpr) || assignmentExpr.equals(expr);
    }

    private static String ensureLocated(String prefix, String suffix) {
        if (prefix == null) {
            return suffix;
        }
        if (isThis(suffix)) {
            return prefix;
        }
        if (suffix.startsWith(prefix)) {
            return suffix;
        }

        StringBuilder sb = new StringBuilder(prefix);

        if (suffix.startsWith(THIS)) {
            sb.append(suffix.substring(THIS.length()));
        } else {
            if (!suffix.startsWith("[")) {
                sb.append(".");
            }
            sb.append(suffix);
        }
        return sb.toString();
    }

    private boolean notEvalSrc() {
        return notEval(from, srcExpr);
    }

    private boolean notEvalTarget() {
        return notEval(to, targetExpr);
    }

}
