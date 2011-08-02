/**
 * Copyright 2011 JBoss Inc 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.formbuilder.server.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.compiler.BPMN2ProcessFactory;
import org.drools.compiler.BPMN2ProcessProvider;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.io.impl.ByteArrayResource;
import org.jbpm.formbuilder.server.GuvnorHelper;
import org.jbpm.formbuilder.shared.task.TaskDefinitionService;
import org.jbpm.formbuilder.shared.task.TaskRef;
import org.jbpm.formbuilder.shared.task.TaskServiceException;

public class GuvnorTaskDefinitionService implements TaskDefinitionService {
    
    private final Map<String /*itemName*/, String /*lastModified*/> assets = new HashMap<String, String>();
    private final Map<String /*itemName*/, List<TaskRef>> tasksIndex = new HashMap<String, List<TaskRef>>();

    private final TaskRepoHelper repo = new TaskRepoHelper();
    private final TaskDefinitionsSemanticModule module = new TaskDefinitionsSemanticModule(repo);

    private final GuvnorHelper helper;
    
    public GuvnorTaskDefinitionService(String baseUrl, String user, String password) {
        super();
        this.helper = new GuvnorHelper(baseUrl, user, password);
        
        BPMN2ProcessFactory.setBPMN2ProcessProvider(new BPMN2ProcessProvider() {
            public void configurePackageBuilder(PackageBuilder packageBuilder) {
                PackageBuilderConfiguration conf = packageBuilder.getPackageBuilderConfiguration();
                if (conf.getSemanticModules().getSemanticModule(TaskDefinitionsSemanticModule.URI) == null) {
                    conf.addSemanticModule(module);
                }
            }
        });
    }
    
    public List<TaskRef> query(String pkgName, String filter) throws TaskServiceException {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(helper.getApiSearchUrl(pkgName));
        try {
            method.setRequestHeader("Authorization", helper.getAuth());
            client.executeMethod(method);
            Properties props = new Properties();
            props.load(method.getResponseBodyAsStream());
            List<TaskRef> tasks = new ArrayList<TaskRef>();
            for (Object key : props.keySet()) {
                String assetId = key.toString();
                if (assetId.endsWith(ResourceType.BPMN2.getDefaultExtension()) || 
                        assetId.endsWith(ResourceType.DRF.getDefaultExtension()) ||
                        assetId.endsWith("bpmn2")) {
                    validateAsset(pkgName, assetId, props.getProperty(assetId));
                    for (Map.Entry<String, List<TaskRef>> entry : tasksIndex.entrySet()) {
                        for (TaskRef ref : entry.getValue()) {
                            if (filter == null || "".equals(filter)) {
                                tasks.add(ref);
                            } else if (ref.getProcessId().contains(assetId) && ref.getTaskName().contains(filter)) {
                                tasks.add(ref);
                            }
                        }
                    }
                }
            }
            return tasks;
        } catch (IOException e) {
            throw new TaskServiceException("Couldn't read task definitions", e);
        } finally {
            method.releaseConnection();
        }
    }
    
    public String getContainingPackage(final String uuid) throws TaskServiceException {
        try {
            return helper.getPackageNameByContentUUID(uuid);
        } catch (JAXBException e) {
            throw new TaskServiceException("problem querying package", e);
        } catch (IOException e) {
            throw new TaskServiceException("problem querying package", e);
        }
    }

    public TaskRef getTaskByUUID(final String packageName, final String userTask, final String uuid) throws TaskServiceException {
        HttpClient client = new HttpClient();
        if (packageName != null) {
            GetMethod call = new GetMethod(helper.getRestBaseUrl());
            try {
                String auth = helper.getAuth();
                call.addRequestHeader("Accept", "application/xml");
                call.addRequestHeader("Authorization", auth);
                client.executeMethod(call);
                PackageListDTO dto = helper.jaxbTransformation(PackageListDTO.class, call.getResponseBodyAsStream(), PackageListDTO.class, PackageDTO.class);
                String processUrl = null;
                String format = null;
                PackageDTO pkg = dto.getSelectedPackage(packageName);
                for (String url : pkg.getAssets()) {
                    GetMethod subCall = new GetMethod(url);
                    try {
                        subCall.setRequestHeader("Authorization", auth);
                        subCall.addRequestHeader("Accept", "application/xml");
                        client.executeMethod(subCall);
                        AssetDTO subDto = helper.jaxbTransformation(AssetDTO.class, subCall.getResponseBodyAsStream(), AssetDTO.class, MetaDataDTO.class);
                        if (subDto.getMetadata().getUuid().equals(uuid)) {
                            processUrl = subDto.getSourceLink();
                            format = subDto.getMetadata().getFormat();
                            break;
                        }
                    } finally {
                        subCall.releaseConnection();
                    }
                }
                if (format != null && "bpmn2".equals(format)) {
                    //download the process in processUrl and get the right task
                    GetMethod processCall = new GetMethod(processUrl);
                    try {
                        processCall.setRequestHeader("Authorization", auth);
                        client.executeMethod(processCall);
                        String processContent = processCall.getResponseBodyAsString();
                        repo.clear();
                        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
                        kbuilder.add(new ByteArrayResource(processContent.getBytes()), ResourceType.BPMN2);
                        if (!kbuilder.hasErrors()) {
                            List<TaskRef> tasks = repo.getTasks();
                            for (TaskRef task : tasks) {
                                if (isReferencedTask(userTask, task)) {
                                    return task;
                                }
                            }
                        }
                    } finally {
                        processCall.releaseConnection();
                    }
                }
            } catch (JAXBException e) {
                throw new TaskServiceException("Couldn't read task definition" + uuid + " : " + userTask, e);
            } catch (IOException e) {
                throw new TaskServiceException("Couldn't read task definition " + uuid + " : " + userTask, e);
            } finally {
                call.releaseConnection();
            }
        }
        return null;
    }

    public TaskRef getBPMN2Task(String bpmn2ProcessContent, String processName, String userTask)
            throws TaskServiceException {
        TaskRef retval = null;
        List<TaskRef> tasks = getProcessTasks(bpmn2ProcessContent, processName);
        if (tasks != null) {
            for (TaskRef task : tasks) {
                if (task.getTaskName().equals(userTask)) {
                    retval = task;
                    break;
                }
            }
        }
        return retval;
    }
    
    private boolean isReferencedTask(String userTask, TaskRef task) {
        boolean emptyUserTask = userTask == null || "".equals(userTask);
        boolean taskIsStartProcess = task.getTaskId().equals(ProcessGetInputHandler.PROCESS_INPUT_NAME);
        boolean taskIsSearchedTask = userTask != null && task.getTaskId().equals(userTask);
        
        return (emptyUserTask && taskIsStartProcess) || taskIsSearchedTask;
    }
    
    private void validateAsset(String packageName, String itemName, String dateLastModified) throws TaskServiceException {
        String name = packageName + ":" + itemName;
        if (assets.get(name) == null || !assets.get(name).equals(dateLastModified)) {
            //clean processAssets, processIndex and processTasks for this particular item
            tasksIndex.remove(itemName);
            //repopulate processAssets
            assets.put(name, dateLastModified);
            //repopulate processIndex
            String content = getTaskDefinitionContent(packageName, itemName);
            if (content != null && !"".equals(content)) {
                List<TaskRef> tasks = getProcessTasks(content, itemName);
                tasksIndex.put(itemName, tasks);
            }
        }
    }

    private String getTaskDefinitionContent(String pkgName, String itemName) throws TaskServiceException {
        HttpClient client = new HttpClient();
        if (itemName != null && !"".equals(itemName)) {
            GetMethod method = new GetMethod(helper.getApiSearchUrl(pkgName) + itemName);
            try {
                method.setRequestHeader("Authorization", helper.getAuth());
                client.executeMethod(method);
                return method.getResponseBodyAsString();
            } catch (IOException e) {
                throw new TaskServiceException(e);
            } finally {
                method.releaseConnection();
            }
        }
        return "";
    }
    
    protected List<TaskRef> getProcessTasks(String bpmn2Content, String processName) throws TaskServiceException {
        repo.clear();
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        ResourceType type = processName.toLowerCase().endsWith("bpmn2") ? ResourceType.BPMN2 : ResourceType.DRF;
        kbuilder.add(new ByteArrayResource(bpmn2Content.getBytes()), type);
        if (!kbuilder.hasErrors()) {
            return new ArrayList<TaskRef>(repo.getTasks());
        } else {
            return new ArrayList<TaskRef>();
        }
    }
}
