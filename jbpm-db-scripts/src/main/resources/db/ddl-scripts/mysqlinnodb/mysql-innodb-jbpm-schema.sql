    create table Attachment (
        id bigint not null auto_increment,
        accessType integer,
        attachedAt datetime(6),
        attachmentContentId bigint not null,
        contentType varchar(255),
        name varchar(255),
        attachment_size integer,
        attachedBy_id varchar(255),
        TaskData_Attachments_Id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table AuditTaskImpl (
        id bigint not null auto_increment,
        activationTime datetime(6),
        actualOwner varchar(255),
        createdBy varchar(255),
        createdOn datetime(6),
        deploymentId varchar(255),
        description varchar(255),
        dueDate datetime(6),
        name varchar(255),
        parentId bigint not null,
        priority integer not null,
        processId varchar(255),
        processInstanceId bigint not null,
        processSessionId bigint not null,
        status varchar(255),
        taskId bigint,
        workItemId bigint,
        lastModificationDate datetime(6),
        primary key (id)
    ) ENGINE=InnoDB;

    create table BAMTaskSummary (
        pk bigint not null auto_increment,
        createdDate datetime(6),
        duration bigint,
        endDate datetime(6),
        processInstanceId bigint not null,
        startDate datetime(6),
        status varchar(255),
        taskId bigint not null,
        taskName varchar(255),
        userId varchar(255),
        OPTLOCK integer,
        primary key (pk)
    ) ENGINE=InnoDB;

    create table BooleanExpression (
        id bigint not null auto_increment,
        expression longtext,
        type varchar(255),
        Escalation_Constraints_Id bigint,
        primary key (id)
    ) ENGINE=InnoDB;
    
    create table CaseIdInfo (
        id bigint not null auto_increment,
        caseIdPrefix varchar(255),
        currentValue bigint,
        primary key (id)
    ) ENGINE=InnoDB;
    
    create table CaseFileDataLog (
        id bigint not null auto_increment,
        caseDefId varchar(255),
        caseId varchar(255),
        itemName varchar(255),
        itemType varchar(255),
        itemValue varchar(255),
        lastModified datetime(6),
        lastModifiedBy varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;

    create table CaseRoleAssignmentLog (
        id bigint not null auto_increment,
        caseId varchar(255),
        entityId varchar(255),
        processInstanceId bigint not null,
        roleName varchar(255),
        type integer not null,
        primary key (id)
    ) ENGINE=InnoDB;    

    create table Content (
        id bigint not null auto_increment,
        content longblob,
        primary key (id)
    ) ENGINE=InnoDB;

    create table ContextMappingInfo (
        mappingId bigint not null auto_increment,
        CONTEXT_ID varchar(255) not null,
        KSESSION_ID bigint not null,
        OWNER_ID varchar(255),
        OPTLOCK integer,
        primary key (mappingId)
    ) ENGINE=InnoDB;

    create table TimerMappingInfo (
    	id bigint not null auto_increment, 
    	externalTimerId varchar(255), 
    	kieSessionId bigint not null, 
    	processInstanceId bigint, 
    	timerId bigint, 
    	uuid varchar(255) not null, 
    	info longblob,
    	primary key (id)
    ) ENGINE=InnoDB;

    create table CorrelationKeyInfo (
        keyId bigint not null auto_increment,
        name varchar(255) not null,
        processInstanceId bigint not null,
        OPTLOCK integer,
        primary key (keyId)
    ) ENGINE=InnoDB;

    create table CorrelationPropertyInfo (
        propertyId bigint not null auto_increment,
        name varchar(255),
        value varchar(255),
        OPTLOCK integer,
        correlationKey_keyId bigint,
        primary key (propertyId)
    ) ENGINE=InnoDB;

    create table Deadline (
        id bigint not null auto_increment,
        deadline_date datetime(6),
        escalated smallint,
        Deadlines_StartDeadLine_Id bigint,
        Deadlines_EndDeadLine_Id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table Delegation_delegates (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table DeploymentStore (
        id bigint not null auto_increment,
        attributes varchar(255),
        DEPLOYMENT_ID varchar(255),
        deploymentUnit longtext,
        state integer,
        updateDate datetime(6),
        primary key (id)
    ) ENGINE=InnoDB;

    create table ErrorInfo (
        id bigint not null auto_increment,
        message varchar(255),
        stacktrace varchar(5000),
        timestamp datetime(6),
        REQUEST_ID bigint not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table Escalation (
        id bigint not null auto_increment,
        name varchar(255),
        Deadline_Escalation_Id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table EventTypes (
        InstanceId bigint not null,
        element varchar(255)
    ) ENGINE=InnoDB;
    
    create table ExecutionErrorInfo (
        id bigint not null auto_increment,
        ERROR_ACK smallint,
        ERROR_ACK_AT datetime(6),
        ERROR_ACK_BY varchar(255),
        ACTIVITY_ID bigint,
        ACTIVITY_NAME varchar(255),
        DEPLOYMENT_ID varchar(255),
        ERROR_INFO longtext,
        ERROR_DATE datetime(6),
        ERROR_ID varchar(255),
        ERROR_MSG varchar(255),
        INIT_ACTIVITY_ID bigint,
        JOB_ID bigint,
        PROCESS_ID varchar(255),
        PROCESS_INST_ID bigint,
        ERROR_TYPE varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;

    create table I18NText (
        id bigint not null auto_increment,
        language varchar(255),
        shortText varchar(255),
        text longtext,
        Task_Subjects_Id bigint,
        Task_Names_Id bigint,
        Task_Descriptions_Id bigint,
        Reassignment_Documentation_Id bigint,
        Notification_Subjects_Id bigint,
        Notification_Names_Id bigint,
        Notification_Documentation_Id bigint,
        Notification_Descriptions_Id bigint,
        Deadline_Documentation_Id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table NodeInstanceLog (
        id bigint not null auto_increment,
        connection varchar(255),
        log_date datetime(6),
        externalId varchar(255),
        nodeId varchar(255),
        nodeInstanceId varchar(255),
        nodeName varchar(255),
        nodeType varchar(255),
        processId varchar(255),
        processInstanceId bigint not null,
        sla_due_date datetime(6),
        slaCompliance integer,
        type integer not null,
        workItemId bigint,
        nodeContainerId varchar(255),
        referenceId bigint,
        observation varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;

    create table Notification (
        DTYPE varchar(31) not null,
        id bigint not null auto_increment,
        priority integer not null,
        Escalation_Notifications_Id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table Notification_BAs (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table Notification_Recipients (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table Notification_email_header (
        Notification_id bigint not null,
        emailHeaders_id bigint not null,
        mapkey varchar(255) not null,
        primary key (Notification_id, mapkey)
    ) ENGINE=InnoDB;

    create table OrganizationalEntity (
        DTYPE varchar(31) not null,
        id varchar(255) not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table PeopleAssignments_BAs (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table PeopleAssignments_ExclOwners (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table PeopleAssignments_PotOwners (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table PeopleAssignments_Recipients (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table PeopleAssignments_Stakeholders (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table ProcessInstanceInfo (
        InstanceId bigint not null auto_increment,
        lastModificationDate datetime(6),
        lastReadDate datetime(6),
        processId varchar(255),
        processInstanceByteArray longblob,
        startDate datetime(6),
        state integer not null,
        OPTLOCK integer,
        primary key (InstanceId)
    ) ENGINE=InnoDB;

    create table ProcessInstanceLog (
        id bigint not null auto_increment,
        correlationKey varchar(255),
        duration bigint,
        end_date datetime(6),
        externalId varchar(255),
        user_identity varchar(255),
        outcome varchar(255),
        parentProcessInstanceId bigint,
        processId varchar(255),
        processInstanceDescription varchar(255),
        processInstanceId bigint not null,
        processName varchar(255),
        processType integer,
        processVersion varchar(255),
        sla_due_date datetime(6),
        slaCompliance integer,
        start_date datetime(6),
        status integer,
        primary key (id)
    ) ENGINE=InnoDB;

    create table QueryDefinitionStore (
        id bigint not null auto_increment,
        qExpression longtext,
        qName varchar(255),
        qSource varchar(255),
        qTarget varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;

    create table Reassignment (
        id bigint not null auto_increment,
        Escalation_Reassignments_Id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table Reassignment_potentialOwners (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table RequestInfo (
        id bigint not null auto_increment,
        commandName varchar(255),
        deploymentId varchar(255),
        executions integer not null,
        businessKey varchar(255),
        message varchar(255),
        owner varchar(255),
        priority integer not null,
        processInstanceId bigint,
        requestData longblob,
        responseData longblob,
        retries integer not null,
        status varchar(255),
        timestamp datetime(6),
        primary key (id)
    ) ENGINE=InnoDB;

    create table SessionInfo (
        id bigint not null auto_increment,
        lastModificationDate datetime(6),
        rulesByteArray longblob,
        startDate datetime(6),
        OPTLOCK integer,
        primary key (id)
    ) ENGINE=InnoDB;

    create table Task (
        id bigint not null auto_increment,
        archived smallint,
        allowedToDelegate varchar(255),
        description varchar(255),
        formName varchar(255),
        name varchar(255),
        priority integer not null,
        subTaskStrategy varchar(255),
        subject varchar(255),
        activationTime datetime(6),
        createdOn datetime(6),
        deploymentId varchar(255),
        documentAccessType integer,
        documentContentId bigint not null,
        documentType varchar(255),
        expirationTime datetime(6),
        faultAccessType integer,
        faultContentId bigint not null,
        faultName varchar(255),
        faultType varchar(255),
        outputAccessType integer,
        outputContentId bigint not null,
        outputType varchar(255),
        parentId bigint not null,
        previousStatus integer,
        processId varchar(255),
        processInstanceId bigint not null,
        processSessionId bigint not null,
        skipable boolean not null,
        status varchar(255),
        workItemId bigint not null,
        taskType varchar(255),
        OPTLOCK integer,
        taskInitiator_id varchar(255),
        actualOwner_id varchar(255),
        createdBy_id varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;

    create table TaskDef (
        id bigint not null auto_increment,
        name varchar(255),
        priority integer not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table TaskEvent (
        id bigint not null auto_increment,
        logTime datetime(6),
        message varchar(255),
        processInstanceId bigint,
        taskId bigint,
        type varchar(255),
        userId varchar(255),
        OPTLOCK integer,
        workItemId bigint,
        correlationKey varchar(255),
        processType integer,
        currentOwner varchar(255),
        primary key (id)
    );

    create table TaskVariableImpl (
        id bigint not null auto_increment,
        modificationDate datetime(6),
        name varchar(255),
        processId varchar(255),
        processInstanceId bigint,
        taskId bigint,
        type integer,
        value varchar(4000),
        primary key (id)
    ) ENGINE=InnoDB;

    create table VariableInstanceLog (
        id bigint not null auto_increment,
        log_date datetime(6),
        externalId varchar(255),
        oldValue varchar(255),
        processId varchar(255),
        processInstanceId bigint not null,
        value varchar(255),
        variableId varchar(255),
        variableInstanceId varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;

    create table WorkItemInfo (
        workItemId bigint not null auto_increment,
        creationDate datetime(6),
        name varchar(255),
        processInstanceId bigint not null,
        state bigint not null,
        OPTLOCK integer,
        workItemByteArray longblob,
        primary key (workItemId)
    ) ENGINE=InnoDB;

    create table email_header (
        id bigint not null auto_increment,
        body longtext,
        fromAddress varchar(255),
        language varchar(255),
        replyToAddress varchar(255),
        subject varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;

    create table task_comment (
        id bigint not null auto_increment,
        addedAt datetime(6),
        text longtext,
        addedBy_id varchar(255),
        TaskData_Comments_Id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    alter table DeploymentStore 
        add constraint UK_85rgskt09thd8mkkfl3tb0y81 unique (DEPLOYMENT_ID);

    alter table QueryDefinitionStore 
        add constraint UK_4ry5gt77jvq0orfttsoghta2j unique (qName);

    alter table Attachment 
        add index IDX_Attachment_Id (attachedBy_id), 
        add constraint FK_7ndpfa311i50bq7hy18q05va3 
        foreign key (attachedBy_id) 
        references OrganizationalEntity (id);

    alter table Attachment 
        add index IDX_Attachment_DataId (TaskData_Attachments_Id), 
        add constraint FK_hqupx569krp0f0sgu9kh87513 
        foreign key (TaskData_Attachments_Id) 
        references Task (id);

    alter table BooleanExpression 
        add index IDX_BoolExpr_Id (Escalation_Constraints_Id), 
        add constraint FK_394nf2qoc0k9ok6omgd6jtpso 
        foreign key (Escalation_Constraints_Id) 
        references Escalation (id);
        
    alter table CaseIdInfo 
        add constraint UK_CaseIdInfo_1 unique (caseIdPrefix);        

    alter table CorrelationPropertyInfo 
        add index IDX_CorrPropInfo_Id (correlationKey_keyId), 
        add constraint FK_hrmx1m882cejwj9c04ixh50i4 
        foreign key (correlationKey_keyId) 
        references CorrelationKeyInfo (keyId);

    alter table CorrelationKeyInfo add constraint IDX_CorrelationKeyInfo_name unique (name);

    alter table Deadline 
        add index IDX_Deadline_StartId (Deadlines_StartDeadLine_Id), 
        add constraint FK_68w742sge00vco2cq3jhbvmgx 
        foreign key (Deadlines_StartDeadLine_Id) 
        references Task (id);

    alter table Deadline 
        add index IDX_Deadline_EndId (Deadlines_EndDeadLine_Id), 
        add constraint FK_euoohoelbqvv94d8a8rcg8s5n 
        foreign key (Deadlines_EndDeadLine_Id) 
        references Task (id);

    alter table Delegation_delegates 
        add index IDX_Delegation_EntityId (entity_id), 
        add constraint FK_gn7ula51sk55wj1o1m57guqxb 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table Delegation_delegates 
        add index IDX_Delegation_TaskId (task_id), 
        add constraint FK_fajq6kossbsqwr3opkrctxei3 
        foreign key (task_id) 
        references Task (id);

    alter table ErrorInfo 
        add index IDX_ErrorInfo_Id (REQUEST_ID), 
        add constraint FK_cms0met37ggfw5p5gci3otaq0 
        foreign key (REQUEST_ID) 
        references RequestInfo (id);

    alter table Escalation 
        add index IDX_Escalation_Id (Deadline_Escalation_Id), 
        add constraint FK_ay2gd4fvl9yaapviyxudwuvfg 
        foreign key (Deadline_Escalation_Id) 
        references Deadline (id);

    alter table I18NText 
        add index IDX_I18NText_SubjId (Task_Subjects_Id), 
        add constraint FK_k16jpgrh67ti9uedf6konsu1p 
        foreign key (Task_Subjects_Id) 
        references Task (id);

    alter table I18NText 
        add index IDX_I18NText_NameId (Task_Names_Id), 
        add constraint FK_fd9uk6hemv2dx1ojovo7ms3vp 
        foreign key (Task_Names_Id) 
        references Task (id);

    alter table I18NText 
        add index Task_Descriptions_Id (Task_Descriptions_Id), 
        add constraint FK_4eyfp69ucrron2hr7qx4np2fp 
        foreign key (Task_Descriptions_Id) 
        references Task (id);

    alter table I18NText 
        add index IDX_I18NText_ReassignId (Reassignment_Documentation_Id), 
        add constraint FK_pqarjvvnwfjpeyb87yd7m0bfi 
        foreign key (Reassignment_Documentation_Id) 
        references Reassignment (id);

    alter table I18NText 
        add index IDX_I18NText_NotSubjId (Notification_Subjects_Id), 
        add constraint FK_o84rkh69r47ti8uv4eyj7bmo2 
        foreign key (Notification_Subjects_Id) 
        references Notification (id);

    alter table I18NText 
        add index IDX_I18NText_NotNamId (Notification_Names_Id), 
        add constraint FK_g1trxri8w64enudw2t1qahhk5 
        foreign key (Notification_Names_Id) 
        references Notification (id);

    alter table I18NText 
        add index IDX_I18NText_NotDocId (Notification_Documentation_Id), 
        add constraint FK_qoce92c70adem3ccb3i7lec8x 
        foreign key (Notification_Documentation_Id) 
        references Notification (id);

    alter table I18NText 
        add index IDX_I18NText_NotDescrId (Notification_Descriptions_Id), 
        add constraint FK_bw8vmpekejxt1ei2ge26gdsry 
        foreign key (Notification_Descriptions_Id) 
        references Notification (id);

    alter table I18NText 
        add index IDX_I18NText_DeadDocId (Deadline_Documentation_Id), 
        add constraint FK_21qvifarxsvuxeaw5sxwh473w 
        foreign key (Deadline_Documentation_Id) 
        references Deadline (id);

    alter table Notification 
        add index IDX_Not_EscId (Escalation_Notifications_Id), 
        add constraint FK_bdbeml3768go5im41cgfpyso9 
        foreign key (Escalation_Notifications_Id) 
        references Escalation (id);

    alter table Notification_BAs 
        add index IDX_NotBAs_Entity (entity_id), 
        add constraint FK_mfbsnbrhth4rjhqc2ud338s4i 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table Notification_BAs 
        add index IDX_NotBAs_Task (task_id), 
        add constraint FK_fc0uuy76t2bvxaxqysoo8xts7 
        foreign key (task_id) 
        references Notification (id);

    alter table Notification_Recipients 
        add index IDX_NotRec_Entity (entity_id), 
        add constraint FK_blf9jsrumtrthdaqnpwxt25eu 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table Notification_Recipients 
        add index IDX_NotRec_Task (task_id), 
        add constraint FK_3l244pj8sh78vtn9imaymrg47 
        foreign key (task_id) 
        references Notification (id);

    alter table Notification_email_header 
        add constraint IDX_NotEmail_Header unique  (emailHeaders_id), 
        add constraint FK_ptaka5kost68h7l3wflv7w6y8 
        foreign key (emailHeaders_id) 
        references email_header (id);

    alter table Notification_email_header 
        add index IDX_NotEmail_Not (Notification_id), 
        add constraint FK_eth4nvxn21fk1vnju85vkjrai 
        foreign key (Notification_id) 
        references Notification (id);

    alter table PeopleAssignments_BAs 
        add index IDX_PAsBAs_Entity (entity_id), 
        add constraint FK_t38xbkrq6cppifnxequhvjsl2 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table PeopleAssignments_BAs 
        add index IDX_PAsBAs_Task (task_id), 
        add constraint FK_omjg5qh7uv8e9bolbaq7hv6oh 
        foreign key (task_id) 
        references Task (id);

    alter table PeopleAssignments_ExclOwners 
        add index IDX_PAsExcl_Entity (entity_id), 
        add constraint FK_pth28a73rj6bxtlfc69kmqo0a 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table PeopleAssignments_ExclOwners 
        add index IDX_PAsExcl_Task (task_id), 
        add constraint FK_b8owuxfrdng050ugpk0pdowa7 
        foreign key (task_id) 
        references Task (id);

    alter table PeopleAssignments_PotOwners 
        add constraint FK_tee3ftir7xs6eo3fdvi3xw026 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table PeopleAssignments_PotOwners 
        add constraint FK_4dv2oji7pr35ru0w45trix02x 
        foreign key (task_id) 
        references Task (id);

    alter table PeopleAssignments_Recipients 
        add index IDX_PAsRecip_Entity (entity_id), 
        add constraint FK_4g7y3wx6gnokf6vycgpxs83d6 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table PeopleAssignments_Recipients 
        add index IDX_PAsRecip_Task (task_id), 
        add constraint FK_enhk831fghf6akjilfn58okl4 
        foreign key (task_id) 
        references Task (id);

    alter table PeopleAssignments_Stakeholders 
        add index IDX_PAsStake_Entity (entity_id), 
        add constraint FK_met63inaep6cq4ofb3nnxi4tm 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table PeopleAssignments_Stakeholders 
        add index IDX_PAsStake_Task (task_id), 
        add constraint FK_4bh3ay74x6ql9usunubttfdf1 
        foreign key (task_id) 
        references Task (id);

    alter table Reassignment 
        add index IDX_Reassign_Esc (Escalation_Reassignments_Id), 
        add constraint FK_pnpeue9hs6kx2ep0sp16b6kfd 
        foreign key (Escalation_Reassignments_Id) 
        references Escalation (id);

    alter table Reassignment_potentialOwners 
        add index IDX_ReassignPO_Entity (entity_id), 
        add constraint FK_8frl6la7tgparlnukhp8xmody 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table Reassignment_potentialOwners 
        add index IDX_ReassignPO_Task (task_id), 
        add constraint FK_qbega5ncu6b9yigwlw55aeijn 
        foreign key (task_id) 
        references Reassignment (id);

    alter table Task 
        add index IDX_Task_Initiator (taskInitiator_id), 
        add constraint FK_dpk0f9ucm14c78bsxthh7h8yh 
        foreign key (taskInitiator_id) 
        references OrganizationalEntity (id);

    alter table Task 
        add index IDX_Task_ActualOwner (actualOwner_id), 
        add constraint FK_nh9nnt47f3l61qjlyedqt05rf 
        foreign key (actualOwner_id) 
        references OrganizationalEntity (id);

    alter table Task 
        add index IDX_Task_CreatedBy (createdBy_id), 
        add constraint FK_k02og0u71obf1uxgcdjx9rcjc 
        foreign key (createdBy_id) 
        references OrganizationalEntity (id);

    alter table task_comment 
        add index IDX_TaskComments_CreatedBy (addedBy_id), 
        add constraint FK_aax378yjnsmw9kb9vsu994jjv 
        foreign key (addedBy_id) 
        references OrganizationalEntity (id);

    alter table task_comment 
        add index IDX_TaskComments_Id (TaskData_Comments_Id), 
        add constraint FK_1ws9jdmhtey6mxu7jb0r0ufvs 
        foreign key (TaskData_Comments_Id) 
        references Task (id);

    create index IDX_Task_processInstanceId on Task(processInstanceId);
    create index IDX_Task_processId on Task(processId);
    create index IDX_Task_status on Task(status);
    create index IDX_Task_archived on Task(archived);
    create index IDX_Task_workItemId on Task(workItemId);

    create index IDX_TaskEvent_taskId on TaskEvent (taskId);
    create index IDX_TaskEvent_processInstanceId on TaskEvent (processInstanceId);

    create index IDX_EventTypes_Id ON EventTypes(InstanceId);
    create index IDX_EventTypes_IdElement ON EventTypes(InstanceId, element);

    create index IDX_CMI_Context ON ContextMappingInfo(CONTEXT_ID);    
    create index IDX_CMI_KSession ON ContextMappingInfo(KSESSION_ID);    
    create index IDX_CMI_Owner ON ContextMappingInfo(OWNER_ID);

    create unique index IDX_TMI_KSessionUUID on TimerMappingInfo (kieSessionId, uuid);

    create index IDX_RequestInfo_timestamp ON RequestInfo(timestamp);
    create index IDX_RequestInfo_owner ON RequestInfo(owner);
    
    create index IDX_BAMTaskSumm_createdDate on BAMTaskSummary(createdDate);
    create index IDX_BAMTaskSumm_duration on BAMTaskSummary(duration);
    create index IDX_BAMTaskSumm_endDate on BAMTaskSummary(endDate);
    create index IDX_BAMTaskSumm_pInstId on BAMTaskSummary(processInstanceId);
    create index IDX_BAMTaskSumm_startDate on BAMTaskSummary(startDate);
    create index IDX_BAMTaskSumm_status on BAMTaskSummary(status);
    create index IDX_BAMTaskSumm_taskId on BAMTaskSummary(taskId);
    create index IDX_BAMTaskSumm_taskName on BAMTaskSummary(taskName);
    create index IDX_BAMTaskSumm_userId on BAMTaskSummary(userId);
    
    create index IDX_PInstLog_duration on ProcessInstanceLog(duration);
    create index IDX_PInstLog_end_date on ProcessInstanceLog(end_date);
    create index IDX_PInstLog_extId on ProcessInstanceLog(externalId);
    create index IDX_PInstLog_user_identity on ProcessInstanceLog(user_identity);
    create index IDX_PInstLog_outcome on ProcessInstanceLog(outcome);
    create index IDX_PInstLog_parentPInstId on ProcessInstanceLog(parentProcessInstanceId);
    create index IDX_PInstLog_pId on ProcessInstanceLog(processId);
    create index IDX_PInstLog_pInsteDescr on ProcessInstanceLog(processInstanceDescription);
    create index IDX_PInstLog_pInstId on ProcessInstanceLog(processInstanceId);
    create index IDX_PInstLog_pName on ProcessInstanceLog(processName);
    create index IDX_PInstLog_pVersion on ProcessInstanceLog(processVersion);
    create index IDX_PInstLog_start_date on ProcessInstanceLog(start_date);
    create index IDX_PInstLog_status on ProcessInstanceLog(status);
    create index IDX_PInstLog_correlation on ProcessInstanceLog(correlationKey);

    create index IDX_VInstLog_pInstId on VariableInstanceLog(processInstanceId);
    create index IDX_VInstLog_varId on VariableInstanceLog(variableId);
    create index IDX_VInstLog_pId on VariableInstanceLog(processId);

    create index IDX_NInstLog_pInstId on NodeInstanceLog(processInstanceId);
    create index IDX_NInstLog_nodeType on NodeInstanceLog(nodeType);
    create index IDX_NInstLog_pId on NodeInstanceLog(processId);
    create index IDX_NInstLog_workItemId on NodeInstanceLog (workItemId);

    create index IDX_ErrorInfo_pInstId on ExecutionErrorInfo(PROCESS_INST_ID);
    create index IDX_ErrorInfo_errorAck on ExecutionErrorInfo(ERROR_ACK);

    create index IDX_AuditTaskImpl_taskId on AuditTaskImpl(taskId);
    create index IDX_AuditTaskImpl_pInstId on AuditTaskImpl(processInstanceId);
    create index IDX_AuditTaskImpl_workItemId on AuditTaskImpl(workItemId);
    create index IDX_AuditTaskImpl_name on AuditTaskImpl(name);
    create index IDX_AuditTaskImpl_processId on AuditTaskImpl(processId);
    create index IDX_AuditTaskImpl_status on AuditTaskImpl(status);

    create index IDX_TaskVariableImpl_taskId on TaskVariableImpl(taskId);
    create index IDX_TaskVariableImpl_pInstId on TaskVariableImpl(processInstanceId);
    create index IDX_TaskVariableImpl_processId on TaskVariableImpl(processId);

    create index IDX_PAsPot_TaskEntity ON PeopleAssignments_PotOwners (task_id, entity_id);

    create index IDX_CaseRoleAssignLog_caseId on CaseRoleAssignmentLog(caseId);
    create index IDX_CaseRoleAssignLog_processInstanceId on CaseRoleAssignmentLog(processInstanceId);

    create index IDX_CaseFileDataLog_caseId on CaseFileDataLog(caseId);
    create index IDX_CaseFileDataLog_itemName on CaseFileDataLog(itemName);
    
    
    