package org.jbpm.bpmn2.xpath;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.drools.process.instance.WorkItem;
import org.drools.runtime.process.ProcessContext;
import org.jbpm.process.instance.impl.AssignmentAction;
import org.jbpm.workflow.core.node.Assignment;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XPATHAssignmentAction implements AssignmentAction, Externalizable {

	private static final long serialVersionUID = 5L;

	private String sourceExpr;
	private String targetExpr;
	private Assignment assignment;
	private boolean isInput;
	
	public String getSourceExpr() {
		return sourceExpr;
	}

	public String getTargetExpr() {
		return targetExpr;
	}

	public boolean isInput() {
		return isInput;
	}
	
	public void readExternal(ObjectInput in) throws IOException,
	ClassNotFoundException {
		sourceExpr = (String) in.readObject();
		targetExpr = (String) in.readObject();
		assignment = (Assignment) in.readObject();
		isInput = in.readBoolean();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(sourceExpr);
		out.writeObject(targetExpr);
		out.writeObject(assignment);
		out.writeBoolean(isInput);
	}

	public XPATHAssignmentAction() {
	}

	public XPATHAssignmentAction(Assignment assignment, String sourceExpr, String targetExpr, boolean isInput) {
		this.assignment = assignment;
		this.sourceExpr = sourceExpr;
		this.targetExpr = targetExpr;
		this.isInput = isInput;
	}

	public void execute(final WorkItem workItem, final ProcessContext context)
			throws Exception {
		if (isInput) {
			executeInput(workItem, context);
		} else {
			executeOutput(workItem, context);
		}
	}

	private void executeInput(final WorkItem workItem,
			final ProcessContext context) throws XPathExpressionException,
			ParserConfigurationException, FactoryConfigurationError,
			TransformerException {
		String from = assignment.getFrom();
		String to = assignment.getTo();

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpathFrom = factory.newXPath();

		XPathExpression exprFrom = xpathFrom.compile(from);

		XPath xpathTo = factory.newXPath();

		XPathExpression exprTo = xpathTo.compile(to);

		Object target = null;
		Object source = null;

		source = context.getVariable(sourceExpr);
		target = ((WorkItem) workItem).getParameter(targetExpr);

		XPathVariableResolver xPathVariableResolver = new XPathVariableResolver() {
			public Object resolveVariable(QName variableName) {
				String localPart = variableName.getLocalPart();

				Object variable = context.getVariable(localPart);
				return variable;

			}
		};

		// this is the only way to change the reference itself,
		// otherwise change only in whatever is being pointed to

		if (".".equals(from) && ".".equals(to)) {
			target = source;

			((WorkItem) workItem).setParameter(targetExpr, target);

			return;
		}

		Object targetElem = null;

		// create temp as per bpm-1534,
		// as the target may be target of many assignments and need a container
		// to hold nodelists etc

		if (!(source instanceof String) && target == null) {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.newDocument();
			// quirky
			Element temp = doc.createElementNS(null, "temp");
			target = temp;
		}

		if (target instanceof org.w3c.dom.Node) {
			XPATHExpressionModifier modifier = new XPATHExpressionModifier();
			// modify the tree, returning the root node
			target = modifier.insertMissingData(to, (org.w3c.dom.Node) target);

			// now pick the leaf for this operation
			if (target != null) {
				targetElem = exprTo.evaluate(target, XPathConstants.NODE);
				if (targetElem == null) {
					throw new RuntimeException(
							"Nothing was selected by the to expression " + to
									+ " on " + targetExpr);
				}
			}
		} else {
			targetElem = target;
		}

		NodeList nl = null;
		if (source instanceof org.w3c.dom.Node) {
			XPath xpathEvaluator = factory.newXPath();
			xpathEvaluator.setXPathVariableResolver(xPathVariableResolver);

			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			nl = (NodeList) xpathEvaluator.evaluate("$" + sourceExpr + "/"
					+ from, builder.newDocument(), XPathConstants.NODESET);
		} else if (source instanceof String) {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.newDocument();
			// quirky: create a temporary element, use its nodelist
			Element temp = doc.createElementNS(null, "temp");
			temp.appendChild(doc.createTextNode((String) source));
			nl = temp.getChildNodes();
		} else if (source == null) {
			// don't throw errors yet ?
			// throw new RuntimeException("Source value was null for source " +
			// sourceExpr);
			return;
		}

		if (nl.getLength() == 0) {
			throw new RuntimeException(
					"Nothing was selected by the from expression " + from
							+ " on " + sourceExpr);
		}
		for (int i = 0; i < nl.getLength(); i++) {
			if (!(targetElem instanceof org.w3c.dom.Node)) {
				if (nl.item(i) instanceof Attr) {
					targetElem = ((Attr) nl.item(i)).getValue();
				} else if (nl.item(i) instanceof Text) {
					targetElem = ((Text) nl.item(i)).getWholeText();
				} else {
					DocumentBuilder builder = DocumentBuilderFactory
							.newInstance().newDocumentBuilder();
					Document doc = builder.newDocument();

					targetElem = doc.importNode(nl.item(i), true);
				}
				target = targetElem;
			} else {
				org.w3c.dom.Node n = ((org.w3c.dom.Node) targetElem)
						.getOwnerDocument().importNode(nl.item(i), true);
				if (n instanceof Attr) {
					((Element) targetElem).setAttributeNode((Attr) n);
				} else {
					((org.w3c.dom.Node) targetElem).appendChild(n);
				}
			}
		}

		((WorkItem) workItem).setParameter(targetExpr, target);

	}

	private void executeOutput(final WorkItem workItem,
			final ProcessContext context) throws XPathExpressionException,
			ParserConfigurationException, FactoryConfigurationError,
			TransformerException {

		Object target = context.getVariable(targetExpr);
		Object source = ((WorkItem) workItem).getResult(sourceExpr);

		XPathVariableResolver xPathVariableResolver = new XPathVariableResolver() {
			public Object resolveVariable(QName variableName) {
				String localPart = variableName.getLocalPart();
				Object result = ((WorkItem) workItem).getResult(localPart);
				return result;

			}
		};

		Object result = executeXPATHInterpreter(target, source,
				xPathVariableResolver);
		context.setVariable(targetExpr, result);
	}

	private Object executeXPATHInterpreter(Object target, Object source,
			XPathVariableResolver xPathVariableResolver)
			throws XPathExpressionException, ParserConfigurationException,
			FactoryConfigurationError, TransformerException {

		String from = assignment.getFrom();
		String to = assignment.getTo();

		// this is the only way to change the reference itself,
		// otherwise change only in whatever is being pointed to

		if (".".equals(from) && ".".equals(to)) {
			target = source;
			return target;
		}

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpathTo = factory.newXPath();
		XPathExpression exprTo = xpathTo.compile(to);

		Object targetElem = null;

		// create temp as per bpm-1534,
		// as the target may be target of many assignments and need a container
		// to hold nodelists etc

		if (!(source instanceof String) && target == null) {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.newDocument();
			// quirky
			Element temp = doc.createElementNS(null, "temp");
			target = temp;
		}

		if (target instanceof org.w3c.dom.Node) {
			XPATHExpressionModifier modifier = new XPATHExpressionModifier();
			// modify the tree, returning the root node
			target = modifier.insertMissingData(to, (org.w3c.dom.Node) target);

			// now pick the leaf for this operation
			if (target != null) {
				targetElem = exprTo.evaluate(target, XPathConstants.NODE);
				if (targetElem == null) {
					throw new RuntimeException(
							"Nothing was selected by the to expression " + to
									+ " on " + targetExpr);
				}
			}
		} else {
			targetElem = target;
		}

		NodeList nl = null;
		if (source instanceof org.w3c.dom.Node) {
			XPath xpathEvaluator = factory.newXPath();
			xpathEvaluator.setXPathVariableResolver(xPathVariableResolver);

			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			nl = (NodeList) xpathEvaluator.evaluate("$" + sourceExpr + "/"
					+ from, builder.newDocument(), XPathConstants.NODESET);
		} else if (source instanceof String) {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.newDocument();
			// quirky: create a temporary element, use its nodelist
			Element temp = doc.createElementNS(null, "temp");
			temp.appendChild(doc.createTextNode((String) source));
			nl = temp.getChildNodes();
		} else if (source == null) {
			// don't throw errors yet ?
			// throw new RuntimeException("Source value was null for source " +
			// sourceExpr);
			return null;
		}

		if (nl.getLength() == 0) {
			throw new RuntimeException(
					"Nothing was selected by the from expression " + from
							+ " on " + sourceExpr);
		}
		for (int i = 0; i < nl.getLength(); i++) {
			if (!(targetElem instanceof org.w3c.dom.Node)) {
				if (nl.item(i) instanceof Attr) {
					targetElem = ((Attr) nl.item(i)).getValue();
				} else if (nl.item(i) instanceof Text) {
					targetElem = ((Text) nl.item(i)).getWholeText();
				} else {
					DocumentBuilder builder = DocumentBuilderFactory
							.newInstance().newDocumentBuilder();
					Document doc = builder.newDocument();

					targetElem = doc.importNode(nl.item(i), true);
				}
				target = targetElem;
			} else {
				org.w3c.dom.Node n = ((org.w3c.dom.Node) targetElem)
						.getOwnerDocument().importNode(nl.item(i), true);
				if (n instanceof Attr) {
					((Element) targetElem).setAttributeNode((Attr) n);
				} else {
					((org.w3c.dom.Node) targetElem).appendChild(n);
				}
			}
		}

		return target;
	}

	public void execute(Map<String, Object> metadata, ProcessContext context)
			throws Exception {

		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = builder.newDocument();
		Element target = doc.createElement("var" + targetExpr.toUpperCase());

		final Object source = metadata.get(AssignmentAction.START_MESSAGE);

		XPathVariableResolver xPathVariableResolver = new XPathVariableResolver() {
			public Object resolveVariable(QName variableName) {
				// This give the variables to the Interpreter
				return source;
			}
		};

		Object result = executeXPATHInterpreter(target, source,
				xPathVariableResolver);
		context.setVariable(targetExpr, result);
	}}
