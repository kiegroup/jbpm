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
                            if (filter == null) {
                                tasks.add(ref);
                            } else {
                                if (ref.getProcessId().contains(assetId) && ref.getTaskName().contains(filter)) {
                                    tasks.add(ref);
                                }
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

    private void validateAsset(String packageName, String itemName, String dateLastModified) throws TaskServiceException {
        String name = packageName + ":" + itemName;
        if (assets.get(name) == null || !assets.get(name).equals(dateLastModified)) {
            //clean processAssets, processIndex and processTasks for this particular item
            tasksIndex.remove(itemName);
            //repopulate processAssets
            assets.put(name, dateLastModified);
            //repopulate processIndex
            List<TaskRef> tasks = getProcessTasks(packageName, itemName);
            tasksIndex.put(itemName, tasks);
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
    
    private List<TaskRef> getProcessTasks(String pkgName, String processName) throws TaskServiceException {
        String content = getTaskDefinitionContent(pkgName, processName);
        repo.clear();
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        ResourceType type = processName.toLowerCase().endsWith("bpmn2") ? ResourceType.BPMN2 : ResourceType.DRF;
        kbuilder.add(new ByteArrayResource(content.getBytes()), type);
        if (!kbuilder.hasErrors()) {
            List<TaskRef> tasks = repo.getTasks();
            for (TaskRef task : tasks) {
                task.setProcessId(processName);
            }
            return tasks;
        } else {
            return new ArrayList<TaskRef>();
        }
    }
}
