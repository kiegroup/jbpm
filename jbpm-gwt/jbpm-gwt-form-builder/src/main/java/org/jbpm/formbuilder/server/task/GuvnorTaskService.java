package org.jbpm.formbuilder.server.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.process.Node;
import org.drools.definition.process.Process;
import org.drools.definition.process.WorkflowProcess;
import org.drools.io.impl.ByteArrayResource;
import org.drools.repository.AssetItem;
import org.drools.repository.AssetItemIterator;
import org.drools.repository.PackageItem;
import org.drools.repository.RulesRepository;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.task.TaskDefinitionService;
import org.jbpm.formbuilder.shared.task.TaskPropertyRef;
import org.jbpm.formbuilder.shared.task.TaskRef;

public class GuvnorTaskService implements TaskDefinitionService {

    RulesRepository repo = null;
    
    private final Map<String /*itemName*/, Long /*lastModified*/> processAssets = new HashMap<String, Long>();
    private final Map<String /*itemName*/, Collection<Process>> processIndex = new HashMap<String, Collection<Process>>();
    private final Map<Process, List<TaskRef>> processTasks = new HashMap<Process, List<TaskRef>>();
    
    public List<TaskRef> query(String filter) {
        PackageItem pkg = repo.loadPackage("");
        AssetItemIterator items = pkg.listAssetsByFormat(ResourceType.BPMN2.getName(), ResourceType.DRF.getName());
        validateProcessTasks(items);
        return queryProcessTasks(filter);
    }

    private void validateProcessTasks(AssetItemIterator items) {
        List<String> keySet = new ArrayList<String>(processAssets.keySet());
        while (items.hasNext()) {
            AssetItem item = items.next();
            validateItem(item, keySet);
        }
        cleanOldItems(keySet);
    }

    private void cleanOldItems(List<String> keySet) {
        if (!keySet.isEmpty()) {
            //clean all items that are no longer there
            for (String key : keySet) {
                processAssets.remove(key);
                Collection<Process> oldProcs = processIndex.remove(key);
                for (Process oldProc : oldProcs) {
                    processTasks.remove(oldProc);
                }
            }
        }
    }
    
    private void validateItem(AssetItem item, List<String> keySet) {
        String itemName = item.getName();
        long lastModified = item.getLastModified().getTime().getTime();
        if (processAssets.get(itemName) == null || processAssets.get(itemName) < lastModified) {
            //clean processAssets, processIndex and processTasks for this particular item
            cleanCaches(keySet, itemName);
            //repopulate processAssets
            processAssets.put(itemName, lastModified);
            //repopulate processIndex
            Collection<Process> processes = getPackageProcesses(item);
            processIndex.put(itemName, processes);
            //repopulate processTasks
            for (Process process : processes) {
                if (process instanceof WorkflowProcess) {
                    processTasks.put(process, getTasks((WorkflowProcess) process));
                }
            }
        }
    }

    private void cleanCaches(List<String> keySet, String itemName) {
        keySet.remove(itemName);
        Collection<Process> procs = processIndex.remove(itemName);
        processIndex.remove(itemName);
        for (Process proc : procs) {
            processTasks.remove(proc);
        }
    }

    private List<TaskRef> getTasks(WorkflowProcess wproc) {
        List<TaskRef> tasks = new ArrayList<TaskRef>();
        Node[] nodes = wproc.getNodes();
        for (Node node : nodes) {
            Map<String, Object> metaData = node.getMetaData();
            Object taskType = metaData.get("taskType"); //TODO arrange with krisv what this data should be
            if (taskType != null && "HumanTask".equals(taskType)) {
                tasks.add(createTaskRef(wproc, metaData));
            }
        }
        return tasks;
    }

    private TaskRef createTaskRef(Process process, Map<String, Object> metaData) {
        //TODO define all other data a task should contain
        TaskRef ref = new TaskRef();
        ref.setProcessId(process.getId());
        String taskName = (String) metaData.get("TaskName");
        List<TaskPropertyRef> inputs = toTaskPropertyList(metaData.get("DataInputs"));
        List<TaskPropertyRef> outputs = toTaskPropertyList(metaData.get("DataOutputs"));
        ref.setInputs(inputs);
        ref.setOutputs(outputs);
        ref.setTaskId(taskName);
        return ref;
    }
    
    private List<TaskRef> queryProcessTasks(String filter) {
        List<TaskRef> retval = new ArrayList<TaskRef>();
        for (List<TaskRef> tasks : processTasks.values()) {
            for (TaskRef task : tasks) {
                if (task.getTaskName().contains(filter)) {
                    retval.add(task);
                }
            }
        }
        return retval;
    }
    
    private Collection<Process> getPackageProcesses(AssetItem item) {
        //TODO validate if needed
        String content = item.getContent().toString();
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        ResourceType type = item.getType().equals("BPMN2") ? ResourceType.BPMN2 : ResourceType.DRF;
        kbuilder.add(new ByteArrayResource(content.getBytes()), type);
        if (!kbuilder.hasErrors()) {
            KnowledgeBase kbase0 = kbuilder.newKnowledgeBase();
            Collection<Process> procs = kbase0.getProcesses();
            return procs;
        } else {
            //TODO notify errors
            return new ArrayList<Process>();
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<TaskPropertyRef> toTaskPropertyList(Object object) {
        List<TaskPropertyRef> retval = new ArrayList<TaskPropertyRef>();
        if (object instanceof Map) {
            Map<String, String> map = (Map<String, String>) object;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                TaskPropertyRef prop = new TaskPropertyRef();
                prop.setName(entry.getKey());
                prop.setSourceExpresion(entry.getValue());
                retval.add(prop);
            }
        }
        return retval;
    }

    public void update(TaskRef task) {
        // TODO Auto-generated method stub
        
    }

    public FormRepresentation getAssociatedForm(TaskRef task) {
        // TODO Auto-generated method stub
        return null;
    }

    
}
