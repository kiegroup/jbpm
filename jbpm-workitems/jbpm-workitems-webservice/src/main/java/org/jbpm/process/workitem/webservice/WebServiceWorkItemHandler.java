/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.process.workitem.webservice;

import java.io.File;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.apache.cxf.common.util.ProxyHelper;
import org.apache.cxf.common.util.ReflectionUtil;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientCallback;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.dynamic.DynamicClientFactory;
import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.jaxws.interceptors.HolderInInterceptor;
import org.apache.cxf.jaxws.interceptors.WrapperClassInInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.bpmn2.core.Bpmn2Import;
import org.jbpm.bpmn2.handler.WorkItemHandlerRuntimeException;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.WorkItemHeaderInfo;
import org.jbpm.process.workitem.core.util.WorkItemHeaderUtils;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.jbpm.workflow.core.impl.WorkflowProcessImpl;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.runtime.Cacheable;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web Service Work Item Handler that performs a WebService call.
 */
@Wid(widfile = "WebServiceDefinitions.wid", name = "WebService",
        displayName = "WebService",
        defaultHandler = "mvel: new org.jbpm.process.workitem.webservice.WebServiceWorkItemHandler()",
        documentation = "${artifactId}/index.html",
        category = "${artifactId}",
        icon = "defaultwebserviceicon.png",
        parameters = {
                @WidParameter(name = "Url"),
                @WidParameter(name = "Namespace"),
                @WidParameter(name = "Interface"),
                @WidParameter(name = "Operation"),
                @WidParameter(name = "Endpoint"),
                @WidParameter(name = "Parameter"),
                @WidParameter(name = "Mode"),
                @WidParameter(name = "Wrapped"),
                @WidParameter(name = "Username"),
                @WidParameter(name = "Password")
        },
        results = {
                @WidResult(name = "Result", runtimeType = "java.lang.Object")
        },
        mavenDepends = {
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${name}", description = "${description}",
                keywords = "webservice,call",
                action = @WidAction(title = "Perform a WebService call"),
                authinfo = @WidAuth
        ))
public class WebServiceWorkItemHandler extends AbstractLogOrThrowWorkItemHandler implements Cacheable {

    public static final String WSDL_IMPORT_TYPE = "http://schemas.xmlsoap.org/wsdl/";

    private static Logger logger = LoggerFactory.getLogger(WebServiceWorkItemHandler.class);

    private final Long defaultJbpmCxfClientConnectionTimeout = Long.parseLong(System.getProperty("org.jbpm.cxf.client.connectionTimeout", "30000"));
    private final Long defaultJbpmCxfClientReceiveTimeout = Long.parseLong(System.getProperty("org.jbpm.cxf.client.receiveTimeout", "60000"));

    private ConcurrentHashMap<String, Client> clients = new ConcurrentHashMap<>();
    private DynamicClientFactory dcf = null;
    private KieSession ksession;
    private int asyncTimeout = 10;
    private ClassLoader classLoader;
    private String username;
    private String password;

    enum WSMode {
        SYNC,
        ASYNC,
        ONEWAY;
    }

    /**
     * Default constructor - no authentication nor ksession
     */
    public WebServiceWorkItemHandler() {
        this.ksession = null;
        this.username = null;
        this.password = null;
    }

    /**
     * Used when no authentication is required
     * @param ksession - kie session
     */
    public WebServiceWorkItemHandler(KieSession ksession) {
        this(ksession, null, null);
    }

    /**
     * Dedicated constructor when BASIC authentication method shall be used
     * @param kieSession - kie session
     * @param username - basic auth username
     * @param password - basic auth password
     */
    public WebServiceWorkItemHandler(KieSession kieSession,
                                     String username,
                                     String password) {
        this.ksession = kieSession;
        this.username = username;
        this.password = password;
    }

    /**
     * Used when no authentication is required
     * @param ksession - kie session
     * @param classloader - classloader to use
     */
    public WebServiceWorkItemHandler(KieSession ksession,
                                     ClassLoader classloader) {
        this(ksession, classloader, null, null);
    }

    /**
     * Dedicated constructor when BASIC authentication method shall be used
     * @param ksession - kie session
     * @param classloader - classloader to use
     * @param username - basic auth username
     * @param password - basic auth password
     */
    public WebServiceWorkItemHandler(KieSession ksession,
                                     ClassLoader classloader,
                                     String username,
                                     String password) {
        this.ksession = ksession;
        this.classLoader = classloader;
        this.username = username;
        this.password = password;
    }

    /**
     * Used when no authentication is required
     * @param ksession - kie session
     * @param timeout - connection timeout
     */
    public WebServiceWorkItemHandler(KieSession ksession,
                                     int timeout) {
        this(ksession, timeout, null, null);
    }

    /**
     * Dedicated constructor when BASIC authentication method shall be used
     * @param ksession - kie session
     * @param timeout - connection timeout
     * @param username - basic auth username
     * @param password - basic auth password
     */
    public WebServiceWorkItemHandler(KieSession ksession,
                                     int timeout,
                                     String username,
                                     String password) {
        this.ksession = ksession;
        this.asyncTimeout = timeout;
        this.username = username;
        this.password = password;
    }

    /**
     * Used when no authentication is required
     * @param handlingProcessId - process id to handle exception
     * @param handlingStrategy - strategy to be applied after handling exception process is completed
     * @param ksession - kie session
     */
    public WebServiceWorkItemHandler(String handlingProcessId,
                                     String handlingStrategy,
                                     KieSession ksession) {
        this(ksession, null, null);
        this.handlingProcessId = handlingProcessId;
        this.handlingStrategy = handlingStrategy;
    }

    /**
     * Dedicated constructor when BASIC authentication method shall be used
     * @param handlingProcessId - process id to handle exception
     * @param handlingStrategy - strategy to be applied after handling exception process is completed
     * @param kieSession - kie session
     * @param username - basic auth username
     * @param password - basic auth password
     */
    public WebServiceWorkItemHandler(String handlingProcessId,
                                     String handlingStrategy,
                                     KieSession kieSession,
                                     String username,
                                     String password) {
        this.ksession = kieSession;
        this.username = username;
        this.password = password;
        this.handlingProcessId = handlingProcessId;
        this.handlingStrategy = handlingStrategy;
    }

    /**
     * Used when no authentication is required
     * @param handlingProcessId - process id to handle exception
     * @param handlingStrategy - strategy to be applied after handling exception process is completed
     * @param ksession - kie session
     * @param classloader - classloader to use
     */
    public WebServiceWorkItemHandler(String handlingProcessId,
                                     String handlingStrategy,
                                     KieSession ksession,
                                     ClassLoader classloader) {
        this(ksession, classloader, null, null);
        this.handlingProcessId = handlingProcessId;
        this.handlingStrategy = handlingStrategy;
    }

    /**
     * Dedicated constructor when BASIC authentication method shall be used
     * @param handlingProcessId - process id to handle exception
     * @param handlingStrategy - strategy to be applied after handling exception process is completed
     * @param ksession - kie session
     * @param classloader - classloader to use
     * @param username - basic auth username
     * @param password - basic auth password
     */
    public WebServiceWorkItemHandler(String handlingProcessId,
                                     String handlingStrategy,
                                     KieSession ksession,
                                     ClassLoader classloader,
                                     String username,
                                     String password) {
        this.ksession = ksession;
        this.classLoader = classloader;
        this.username = username;
        this.password = password;
        this.handlingProcessId = handlingProcessId;
        this.handlingStrategy = handlingStrategy;
    }

    /**
     * Used when no authentication is required
     * @param handlingProcessId - process id to handle exception
     * @param handlingStrategy - strategy to be applied after handling exception process is completed
     * @param ksession - kie session
     * @param timeout - connection timeout
     */
    public WebServiceWorkItemHandler(String handlingProcessId,
                                     String handlingStrategy,
                                     KieSession ksession,
                                     int timeout) {
        this(ksession, timeout, null, null);
        this.handlingProcessId = handlingProcessId;
        this.handlingStrategy = handlingStrategy;
    }

    /**
     * Dedicated constructor when BASIC authentication method shall be used
     * @param handlingProcessId - process id to handle exception
     * @param handlingStrategy - strategy to be applied after handling exception process is completed
     * @param ksession - kie session
     * @param timeout - connection timeout
     * @param username - basic auth username
     * @param password - basic auth password
     */
    public WebServiceWorkItemHandler(String handlingProcessId,
                                     String handlingStrategy,
                                     KieSession ksession,
                                     int timeout,
                                     String username,
                                     String password) {
        this.ksession = ksession;
        this.asyncTimeout = timeout;
        this.username = username;
        this.password = password;
        this.handlingProcessId = handlingProcessId;
        this.handlingStrategy = handlingStrategy;
    }

    @Override
    public void executeWorkItem(WorkItem workItem,
                                final WorkItemManager manager) {

        // since JaxWsDynamicClientFactory will change the TCCL we need to restore it after creating client
        ClassLoader origClassloader = Thread.currentThread().getContextClassLoader();

        Object[] parameters = null;
        String interfaceRef = (String) workItem.getParameter("Interface");
        String operationRef = (String) workItem.getParameter("Operation");
        String endpointAddress = (String) workItem.getParameter("Endpoint");
        if (workItem.getParameter("Parameter") instanceof Object[]) {
            parameters = (Object[]) workItem.getParameter("Parameter");
        } else if (workItem.getParameter("Parameter") != null && workItem.getParameter("Parameter").getClass().isArray()) {
            int length = Array.getLength(workItem.getParameter("Parameter"));
            parameters = new Object[length];
            for (int i = 0; i < length; i++) {
                parameters[i] = Array.get(workItem.getParameter("Parameter"),
                                          i);
            }
        } else {
            parameters = new Object[]{workItem.getParameter("Parameter")};
        }

        String modeParam = (String) workItem.getParameter("Mode");
        WSMode mode = WSMode.valueOf(modeParam == null ? "SYNC" : modeParam.toUpperCase());
        Boolean wrapped = Boolean.parseBoolean((String) workItem.getParameter("Wrapped"));

        try {
            Client client = getWSClient(workItem,
                                        interfaceRef);
            if (client == null) {
                throw new IllegalStateException("Unable to create client for web service " + interfaceRef + " - " + operationRef);
            }

            //Override endpoint address if configured.
            if (endpointAddress != null && !"".equals(endpointAddress)) {
                client.getRequestContext().put(Message.ENDPOINT_ADDRESS,
                                               endpointAddress);
            }

            // apply authorization if needed
            String u = (String) workItem.getParameter("Username");
            String p = (String) workItem.getParameter("Password");
            if (u == null || p == null) {
                u = this.username;
                p = this.password;
            }
            applyAuthorization(u, p, client);

            //Remove interceptors if using wrapped mode
            if (wrapped) {
                removeWrappingInterceptors(client);
            }

            switch (mode) {
                case SYNC:
                    Object[] result = wrapped ? client.invokeWrapped(operationRef, parameters) : client.invoke(operationRef, parameters);

                    Map<String, Object> output = new HashMap<String, Object>();

                    if (result == null || result.length == 0) {
                        output.put("Result",
                                   null);
                    } else {
                        output.put("Result",
                                   result[0]);
                    }
                    logger.debug("Received sync response {} completeing work item {}",
                                 result,
                                 workItem.getId());
                    manager.completeWorkItem(workItem.getId(),
                                             output);
                    break;
                case ASYNC:
                    final ClientCallback callback = new ClientCallback();
                    final long workItemId = workItem.getId();
                    final String deploymentId = nonNull(((WorkItemImpl) workItem).getDeploymentId());
                    final long processInstanceId = workItem.getProcessInstanceId();

                    if (wrapped) {
                        client.invokeWrapped(callback, operationRef, parameters);
                    } else {
                        client.invoke(callback, operationRef, parameters);
                    }
                    new Thread(new Runnable() {

                        @Override
                        public void run() {

                            try {

                                Object[] result = callback.get(asyncTimeout,
                                                               TimeUnit.SECONDS);
                                Map<String, Object> output = new HashMap<String, Object>();
                                if (callback.isDone()) {
                                    if (result == null) {
                                        output.put("Result",
                                                   null);
                                    } else {
                                        output.put("Result",
                                                   result[0]);
                                    }
                                }
                                logger.debug("Received async response {} completeing work item {}",
                                             result,
                                             workItemId);

                                RuntimeManager manager = RuntimeManagerRegistry.get().getManager(deploymentId);
                                if (manager != null) {
                                    RuntimeEngine engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));

                                    engine.getKieSession().getWorkItemManager().completeWorkItem(workItemId,
                                                                                                 output);

                                    manager.disposeRuntimeEngine(engine);
                                } else {
                                    // in case there is no RuntimeManager available use available ksession,
                                    // as it might be used without runtime manager at all
                                    ksession.getWorkItemManager().completeWorkItem(workItemId,
                                                                                   output);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException("Error encountered while invoking ws operation asynchronously",
                                                           e);
                            }
                        }
                    }).start();
                    break;
                case ONEWAY:
                    ClientCallback callbackFF = new ClientCallback();

                    if (wrapped) {
                        client.invokeWrapped(callbackFF, operationRef, parameters);
                    } else {
                        client.invoke(callbackFF, operationRef, parameters);
                    }
                    logger.debug("One way operation, not going to wait for response, completing work item {}",
                                 workItem.getId());
                    manager.completeWorkItem(workItem.getId(),
                                             new HashMap<String, Object>());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            handleException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(origClassloader);
        }
    }

    private void removeWrappingInterceptors(Client client){
        Endpoint endpoint = client.getEndpoint();
        endpoint.getInInterceptors().stream().filter(i -> i instanceof WrapperClassInInterceptor).findFirst().ifPresent(i -> {
            endpoint.getInInterceptors().remove(i);
        });
        endpoint.getInInterceptors().stream().filter(i -> i instanceof HolderInInterceptor).findFirst().ifPresent(i -> {
            endpoint.getInInterceptors().remove(i);
        });
    }

    protected Client getWSClient(WorkItem workItem, String interfaceRef) {
        return clients.computeIfAbsent(interfaceRef, k -> createClient(workItem, interfaceRef));
    }

    private Client createClient(WorkItem workItem, String interfaceRef) {
        String importLocation = (String) workItem.getParameter("Url");
        String importNamespace = (String) workItem.getParameter("Namespace");
        if (importLocation != null && importLocation.trim().length() > 0 && importNamespace != null && importNamespace.trim().length() > 0) {
            return createClient(workItem, importLocation, importNamespace, interfaceRef);
        }

        long processInstanceId = ((WorkItemImpl) workItem).getProcessInstanceId();
        WorkflowProcessImpl process = ((WorkflowProcessImpl) ksession.getProcessInstance(processInstanceId).getProcess());
        @SuppressWarnings("unchecked")
        List<Bpmn2Import> typedImports = (List<Bpmn2Import>) process.getMetaData("Bpmn2Imports");

        if (typedImports != null) {
            for (Bpmn2Import importObj : typedImports) {
                if (WSDL_IMPORT_TYPE.equalsIgnoreCase(importObj.getType())) {
                    try {
                        return createClient(workItem, importObj.getLocation(), importObj.getNamespace(), interfaceRef);
                    } catch (Exception e) {
                        logger.error("Error when creating WS Client", e);
                    }
                }
            }
        }
        return null;
    }
    
    private Client createClient(WorkItem workItem, String location, String namespace, String interfaceRef) {
        Client client = getDynamicClientFactory().createClient(location, new QName(namespace, interfaceRef),
                                                               getInternalClassLoader(), null);
        setClientTimeout(workItem, client);
        setEscapeHandler(workItem, client);
        addHeaders(workItem, client);
        addCDataWriterInterceptor(workItem, client);
        return client;
    }

    private void addHeaders(WorkItem workItem, Client client) {
        Collection<WorkItemHeaderInfo> headers = WorkItemHeaderUtils.getHeaderInfo(workItem);
        if (!headers.isEmpty()) {
            client.getRequestContext().put(Header.HEADER_LIST,
                                           headers.stream().map(h -> buildHeader(h, client)).collect(Collectors.toList()));
        }
    }

    private Header buildHeader(WorkItemHeaderInfo header, Client client) {
        String namespace = (String) header.getParam("NS");
        QName name = namespace == null ? new QName(header.getName()) : new QName(namespace, header.getName());
        JAXBDataBinding binding = (JAXBDataBinding) client.getConduitSelector().getEndpoint().getService().getDataBinding();
        String escapeHandler = (String) header.getParam("ESCAPE");
        if (escapeHandler != null)
            try {
                binding = new JAXBDataBinding(binding.getContext());
                setEscapeHandler(binding, escapeHandler);
            } catch (Exception ex) {
                logger.warn("Error creating binding for escapeHandler {}", escapeHandler, ex);
            }
        return new Header(name, header.getContent(), binding);
    }

    private void setEscapeHandler(WorkItem workItem, Client client) {
        String escapeHandler = (String) workItem.getParameter("ESCAPE_HANDLER");
        if (escapeHandler == null) {
            escapeHandler = System.getProperty("org.jbpm.cxf.client.escapeHandler");
        }
        if (escapeHandler != null) {
            DataBinding binding = client.getConduitSelector().getEndpoint().getService().getDataBinding();
            if (binding instanceof JAXBDataBinding) {
                setEscapeHandler((JAXBDataBinding) binding, escapeHandler);
            }
        }
    }

    private static void setEscapeHandler(JAXBDataBinding dataBinding, String escapeHandler) {
        Object escapeHandlerObj = createEscapeHandler(dataBinding, escapeHandler);
        logger.debug("Escape handler {} created. Object is {}", escapeHandler, escapeHandlerObj);
        Map<String, Object> map = dataBinding.getMarshallerProperties();
        if (map == null) {
            map = new HashMap<>();
        }
        // if implementation is not reference one, this should be ignored. 
        map.put("com.sun.xml.bind.characterEscapeHandler", escapeHandlerObj);
        map.put("com.sun.xml.bind.marshaller.CharacterEscapeHandler", escapeHandlerObj);
        logger.debug("Marshalling properties {}", map);
        dataBinding.setMarshallerProperties(map);
    }

    /* this code is a modified version of cxf that uses the class loader of context, no current thread context */
    public static Object createEscapeHandler(JAXBDataBinding binding, String escapeHandler) {
        Class<?> cls = binding.getContext().getClass();
        ClassLoader classLoader = cls.getClassLoader();
        String className = cls.getName();
        String postFix = className.contains("com.sun.xml.internal") || className.contains("eclipse") ? ".internal" : "";
        try {
            Class<?> handlerInterface = classLoader.loadClass("com.sun.xml" + postFix + ".bind.marshaller.CharacterEscapeHandler");
            Class<?> handlerClass = classLoader.loadClass("com.sun.xml" + postFix + ".bind.marshaller." + escapeHandler);
            Object targetHandler = ReflectionUtil.getDeclaredField(handlerClass, "theInstance").get(null);
            return ProxyHelper.getProxy(classLoader, new Class[]{handlerInterface}, new LoggingEscapeHandlerInvocationHandler(targetHandler));
        } catch (Exception e) {
            logger.warn("Error instantiating escape handler, characters will be escaped", e);
        }
        return null;
    }
    
    private void addCDataWriterInterceptor(WorkItem workItem, Client client) {
        String cdataElements = (String) workItem.getParameter("CDataElements");
        if (cdataElements != null) {
            logger.debug("Adding CData Interceptor for elements {}", cdataElements);
            client.getOutInterceptors().add(new CDataWriterInterceptor(cdataElements));
        }
    }

    private static final class LoggingEscapeHandlerInvocationHandler implements InvocationHandler {

        private Object target;

        public LoggingEscapeHandlerInvocationHandler(Object obj) {
            target = obj;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            logger.debug("Escape handler invoked with  args {}", args);
            Object result = null;
            if (method.getName().equals("escape") && args.length == 5) {
                if ((Integer) args[1] == 0 && (Integer) args[2] == 0) {
                    Writer writer = (Writer) args[4];
                    writer.write("");
                    return null;
                }
                result = method.invoke(target, args);
            }
            logger.debug("Escape handler result {}", result);
            return result;
        }
    }

    private void setClientTimeout(WorkItem workItem, Client client) {
        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        HTTPClientPolicy policy = conduit.getClient();

        long connectionTimeout = defaultJbpmCxfClientConnectionTimeout;
        String connectionTimeoutStr = (String) workItem.getParameter("ConnectionTimeout");
        if (connectionTimeoutStr != null && !connectionTimeoutStr.trim().isEmpty()) {
            connectionTimeout = Long.valueOf(connectionTimeoutStr);
        }
        long receiveTimeout = defaultJbpmCxfClientReceiveTimeout;
        String receiveTimeoutStr = (String) workItem.getParameter("ReceiveTimeout");
        if (receiveTimeoutStr != null && !receiveTimeoutStr.trim().isEmpty()) {
            receiveTimeout = Long.valueOf(receiveTimeoutStr);
        }

        logger.debug("connectionTimeout = {}, receiveTimeout = {}", connectionTimeout, receiveTimeout);
        policy.setConnectionTimeout(connectionTimeout);
        policy.setReceiveTimeout(receiveTimeout);
    }

    protected synchronized DynamicClientFactory getDynamicClientFactory() {
        if (this.dcf == null) {
            this.dcf = JaxWsDynamicClientFactory.newInstance();
        }
        return this.dcf;
    }

    @Override
    public void abortWorkItem(WorkItem workItem,
                              WorkItemManager manager) {
        // Do nothing, cannot be aborted
    }

    /* CXF builds compiler classpath assuming that the hierarchy of ClassLoader is composed of URLClassLoader instances.
     * Since ModuleClassLoader does not implement URLClassLoader, we need to provide an alternative way of retrieving these URLS
     * so CXF can build a proper classpath, avoiding the issue mentioned below. 
     * @see https://issues.apache.org/jira/browse/CXF-7925
     */
    @SuppressWarnings("squid:S1872")
    private ClassLoader getInternalClassLoader() {
        ClassLoader cl = this.classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader(), parent = cl;
        Collection<URL> uris = new HashSet<>();
        do {
            if (parent.getClass().getSimpleName().equals("ModuleClassLoader")) {
                try {
                    getJarsFromModuleClassLoader(parent, uris);
                } catch (ReflectiveOperationException | MalformedURLException e) {
                    throw new WorkItemHandlerRuntimeException(e, "Problem calculating list of URLs from ModuleClassLoader");
                }
            }
            parent = parent.getParent();
        } while (parent != null);
        if (!uris.isEmpty()) {
            cl = new CXFJavaCompileClassLoader(uris, cl);
        }
        return cl;
    }

    private static class CXFJavaCompileClassLoader extends URLClassLoader {

        private URL[] jarUrls;

        public CXFJavaCompileClassLoader(Collection<URL> files, ClassLoader parent) {
            super(new URL[0], parent);
            logger.trace("Loaded urls are {}", files);
            this.jarUrls = files.toArray(new URL[0]);
        }

        @Override
        public URL[] getURLs() {
            return jarUrls;
        }
    }

    /* This method uses public methods of ModuleClassLoader through introspection in order to avoid a compile time
     * dependency with jboss module classes.
     */
    private void getJarsFromModuleClassLoader(ClassLoader cl,
                                              Collection<URL> collector) throws ReflectiveOperationException, MalformedURLException {
        Object resourceLoaders = methodInvoke(cl, "getResourceLoaders", Object.class);
        for (int i = 0; i < Array.getLength(resourceLoaders); i++) {
            Object resourceLoader = Array.get(resourceLoaders, i);
            switch (resourceLoader.getClass().getSimpleName()) {
                case "JarFileResourceLoader":
                    collector.add(fieldGet(resourceLoader, "rootUrl", URL.class));
                    break;
                case "VFSResourceLoader":
                    String rootName = fieldGet(resourceLoader, "rootName", String.class);
                    if (rootName.endsWith("jar")) {
                        collector.add(new File(methodInvoke(fieldGet(resourceLoader, "root", Object.class),
                                "getPhysicalFile", File.class).getParentFile(), rootName).toURI().toURL());
                    }
                    break;
                default:
                    // ignore other resources

            }
        }
    }

    @SuppressWarnings("squid:S3011")
    private <T> T methodInvoke(Object obj,
                               String methodName,
                               Class<T> resultClass) throws ReflectiveOperationException {
        Method m = obj.getClass().getDeclaredMethod(methodName);
        m.setAccessible(true);
        return resultClass.cast(m.invoke(obj));
    }

    @SuppressWarnings("squid:S3011")
    private <T> T fieldGet(Object obj,
                           String fieldName,
                           Class<T> resultClass) throws ReflectiveOperationException {
        Field f = obj.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return resultClass.cast(f.get(obj));
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    protected String nonNull(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    @Override
    public void close() {
        if (clients != null) {
            for (Client client : clients.values()) {
                client.destroy();
            }
        }
    }

    protected void applyAuthorization(String userName, String password, Client client) {
        if(userName != null && password != null) {
            HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
            AuthorizationPolicy authorizationPolicy = new AuthorizationPolicy();
            authorizationPolicy.setUserName(userName);
            authorizationPolicy.setPassword(password);

            authorizationPolicy.setAuthorizationType("Basic");
            httpConduit.setAuthorization(authorizationPolicy);
        } else {
            logger.warn("UserName and Password must be provided to set the authorization policy.");
        }
    }

    public void setClients(ConcurrentHashMap<String, Client> clients) {
        this.clients = clients;
    }
}