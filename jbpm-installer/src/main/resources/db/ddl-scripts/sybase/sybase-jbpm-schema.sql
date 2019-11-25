    create table Attachment (
        id bigint identity not null,
        accessType int null,
        attachedAt datetime null,
        attachmentContentId bigint not null,
        contentType varchar(255) null,
        name varchar(255) null,
        attachment_size int null,
        attachedBy_id varchar(255) null,
        TaskData_Attachments_Id bigint null,
        primary key (id)
    ) lock datarows
    go

    create table AuditTaskImpl (
        id bigint identity not null,
        activationTime datetime null,
        actualOwner varchar(255) null,
        createdBy varchar(255) null,
        createdOn datetime null,
        deploymentId varchar(255) null,
        description varchar(255) null,
        dueDate datetime null,
        name varchar(255) null,
        parentId bigint not null,
        priority int not null,
        processId varchar(255) null,
        processInstanceId bigint not null,
        processSessionId bigint not null,
        status varchar(255) null,
        taskId bigint null,
        workItemId bigint null,
        lastModificationDate datetime,
        primary key (id)
    ) lock datarows
    go

    create table BAMTaskSummary (
        pk bigint identity not null,
        createdDate datetime null,
        duration bigint null,
        endDate datetime null,
        processInstanceId bigint not null,
        startDate datetime null,
        status varchar(255) null,
        taskId bigint not null,
        taskName varchar(255) null,
        userId varchar(255) null,
        OPTLOCK int null,
        primary key (pk)
    ) lock datarows
    go

    create table BooleanExpression (
        id bigint identity not null,
        expression text null,
        type varchar(255) null,
        Escalation_Constraints_Id bigint null,
        primary key (id)
    ) lock datarows
    go
    
    create table CaseIdInfo (
        id bigint identity not null,
        caseIdPrefix varchar(255) null,
        currentValue bigint null,
        primary key (id)
    ) lock datarows
    go

    create table CaseFileDataLog (
        id bigint identity not null,
        caseDefId varchar(255) null,
        caseId varchar(255) null,
        itemName varchar(255) null,
        itemType varchar(255) null,
        itemValue varchar(255) null,
        lastModified datetime null,
        lastModifiedBy varchar(255) null,
        primary key (id)
    ) lock datarows
    go
    create table CaseRoleAssignmentLog (
        id bigint identity not null,
        caseId varchar(255) null,
        entityId varchar(255) null,
        processInstanceId bigint not null,
        roleName varchar(255) null,
        type int not null,
        primary key (id)
    ) lock datarows
    go

    create table Content (
        id bigint identity not null,
        content image null,
        primary key (id)
    ) lock datarows
    go

    create table ContextMappingInfo (
        mappingId bigint identity not null,
        CONTEXT_ID varchar(255) not null,
        KSESSION_ID bigint not null,
        OWNER_ID varchar(255) null,
        OPTLOCK int null,
        primary key (mappingId)
    ) lock datarows
    go

    create table CorrelationKeyInfo (
        keyId bigint identity not null,
        name varchar(255) null,
        processInstanceId bigint not null,
        OPTLOCK int null,
        primary key (keyId)
    ) lock datarows
    go

    create table CorrelationPropertyInfo (
        propertyId bigint identity not null,
        name varchar(255) null,
        value varchar(255) null,
        OPTLOCK int null,
        correlationKey_keyId bigint null,
        primary key (propertyId)
    ) lock datarows
    go

    create table Deadline (
        id bigint identity not null,
        deadline_date datetime null,
        escalated smallint null,
        Deadlines_StartDeadLine_Id bigint null,
        Deadlines_EndDeadLine_Id bigint null,
        primary key (id)
    ) lock datarows
    go

    create table Delegation_delegates (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) lock datarows
    go

    create table DeploymentStore (
        id bigint identity not null,
        attributes varchar(255) null,
        DEPLOYMENT_ID varchar(255) null,
        deploymentUnit text null,
        state int null,
        updateDate datetime null,
        primary key (id)
    ) lock datarows
    go

    create table ErrorInfo (
        id bigint identity not null,
        message varchar(255) null,
        stacktrace varchar(5000) null,
        timestamp datetime null,
        REQUEST_ID bigint not null,
        primary key (id)
    ) lock datarows
    go

    create table Escalation (
        id bigint identity not null,
        name varchar(255) null,
        Deadline_Escalation_Id bigint null,
        primary key (id)
    ) lock datarows
    go

    create table EventTypes (
        InstanceId bigint not null,
        element varchar(255) null
    ) lock datarows
    go

    create table ExecutionErrorInfo (
        id bigint identity not null,
        ERROR_ACK smallint null,
        ERROR_ACK_AT datetime null,
        ERROR_ACK_BY varchar(255) null,
        ACTIVITY_ID bigint null,
        ACTIVITY_NAME varchar(255) null,
        DEPLOYMENT_ID varchar(255) null,
        ERROR_INFO text null,
        ERROR_DATE datetime null,
        ERROR_ID varchar(255) null,
        ERROR_MSG varchar(255) null,
        INIT_ACTIVITY_ID bigint null,
        JOB_ID bigint null,
        PROCESS_ID varchar(255) null,
        PROCESS_INST_ID bigint null,
        ERROR_TYPE varchar(255) null,
        primary key (id)
    ) lock datarows
    go
    
    create table I18NText (
        id bigint identity not null,
        language varchar(255) null,
        shortText varchar(255) null,
        text text null,
        Task_Subjects_Id bigint null,
        Task_Names_Id bigint null,
        Task_Descriptions_Id bigint null,
        Reassignment_Documentation_Id bigint null,
        Notification_Subjects_Id bigint null,
        Notification_Names_Id bigint null,
        Notification_Documentation_Id bigint null,
        Notification_Descriptions_Id bigint null,
        Deadline_Documentation_Id bigint null,
        primary key (id)
    ) lock datarows
    go

    create table NodeInstanceLog (
        id bigint identity not null,
        connection varchar(255) null,
        log_date datetime null,
        externalId varchar(255) null,
        nodeId varchar(255) null,
        nodeInstanceId varchar(255) null,
        nodeName varchar(255) null,
        nodeType varchar(255) null,
        processId varchar(255) null,
        processInstanceId bigint not null,
        sla_due_date datetime null,
        slaCompliance int null,
        type int not null,
        workItemId bigint null,
        nodeContainerId varchar(255) null,
        referenceId bigint null,
        primary key (id)
    ) lock datarows
    go

    create table Notification (
        DTYPE varchar(31) not null,
        id bigint identity not null,
        priority int not null,
        Escalation_Notifications_Id bigint null,
        primary key (id)
    ) lock datarows
    go

    create table Notification_BAs (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) lock datarows
    go

    create table Notification_Recipients (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) lock datarows
    go

    create table Notification_email_header (
        Notification_id bigint not null,
        emailHeaders_id bigint not null,
        mapkey varchar(255) not null,
        primary key (Notification_id, mapkey)
    ) lock datarows
    go

    create table OrganizationalEntity (
        DTYPE varchar(31) not null,
        id varchar(255) not null,
        primary key (id)
    ) lock datarows
    go

    create table PeopleAssignments_BAs (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) lock datarows
    go

    create table PeopleAssignments_ExclOwners (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) lock datarows
    go

    create table PeopleAssignments_PotOwners (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) lock datarows
    go

    create table PeopleAssignments_Recipients (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) lock datarows
    go

    create table PeopleAssignments_Stakeholders (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) lock datarows
    go

    create table ProcessInstanceInfo (
        InstanceId bigint identity not null,
        lastModificationDate datetime null,
        lastReadDate datetime null,
        processId varchar(255) null,
        processInstanceByteArray image null,
        startDate datetime null,
        state int not null,
        OPTLOCK int null,
        primary key (InstanceId)
    ) lock datarows
    go

    create table ProcessInstanceLog (
        id bigint identity not null,
        correlationKey varchar(255) null,
        duration bigint null,
        end_date datetime null,
        externalId varchar(255) null,
        user_identity varchar(255) null,
        outcome varchar(255) null,
        parentProcessInstanceId bigint null,
        processId varchar(255) null,
        processInstanceDescription varchar(255) null,
        processInstanceId bigint not null,
        processName varchar(255) null,
        processType int null,
        processVersion varchar(255) null,
        sla_due_date datetime null,
        slaCompliance int null,
        start_date datetime null,
        status int null,
        primary key (id)
    ) lock datarows
    go

    create table QueryDefinitionStore (
        id bigint identity not null,
        qExpression text null,
        qName varchar(255) null,
        qSource varchar(255) null,
        qTarget varchar(255) null,
        primary key (id)
    ) lock datarows
    go

    create table Reassignment (
        id bigint identity not null,
        Escalation_Reassignments_Id bigint null,
        primary key (id)
    ) lock datarows
    go

    create table Reassignment_potentialOwners (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) lock datarows
    go

    create table RequestInfo (
        id bigint identity not null,
        commandName varchar(255) null,
        deploymentId varchar(255) null,
        executions int not null,
        businessKey varchar(255) null,
        message varchar(255) null,
        owner varchar(255) null,
        priority int not null,
        processInstanceId bigint,
        requestData image null,
        responseData image null,
        retries int not null,
        status varchar(255) null,
        timestamp datetime null,
        primary key (id)
    ) lock datarows
    go

    create table SessionInfo (
        id bigint identity not null,
        lastModificationDate datetime null,
        rulesByteArray image null,
        startDate datetime null,
        OPTLOCK int null,
        primary key (id)
    ) lock datarows
    go

    create table Task (
        id bigint identity not null,
        archived smallint null,
        allowedToDelegate varchar(255) null,
        description varchar(255) null,
        formName varchar(255) null,
        name varchar(255) null,
        priority int not null,
        subTaskStrategy varchar(255) null,
        subject varchar(255) null,
        activationTime datetime null,
        createdOn datetime null,
        deploymentId varchar(255) null,
        documentAccessType int null,
        documentContentId bigint not null,
        documentType varchar(255) null,
        expirationTime datetime null,
        faultAccessType int null,
        faultContentId bigint not null,
        faultName varchar(255) null,
        faultType varchar(255) null,
        outputAccessType int null,
        outputContentId bigint not null,
        outputType varchar(255) null,
        parentId bigint not null,
        previousStatus int null,
        processId varchar(255) null,
        processInstanceId bigint not null,
        processSessionId bigint not null,
        skipable tinyint not null,
        status varchar(255) null,
        workItemId bigint not null,
        taskType varchar(255) null,
        OPTLOCK int null,
        taskInitiator_id varchar(255) null,
        actualOwner_id varchar(255) null,
        createdBy_id varchar(255) null,
        primary key (id)
    ) lock datarows
    go

    create table TaskDef (
        id bigint identity not null,
        name varchar(255) null,
        priority int not null,
        primary key (id)
    ) lock datarows
    go

    create table TaskEvent (
        id bigint identity not null,
        logTime datetime null,
        message varchar(255) null,
        processInstanceId bigint null,
        taskId bigint null,
        type varchar(255) null,
        userId varchar(255) null,
        OPTLOCK int null,
        workItemId bigint null,
        primary key (id)
    ) lock datarows
    go

    create table TaskVariableImpl (
        id bigint identity not null,
        modificationDate datetime null,
        name varchar(255) null,
        processId varchar(255) null,
        processInstanceId bigint null,
        taskId bigint null,
        type int null,
        value varchar(4000) null,
        primary key (id)
    ) lock datarows
    go

    create table VariableInstanceLog (
        id bigint identity not null,
        log_date datetime null,
        externalId varchar(255) null,
        oldValue varchar(255) null,
        processId varchar(255) null,
        processInstanceId bigint not null,
        value varchar(255) null,
        variableId varchar(255) null,
        variableInstanceId varchar(255) null,
        primary key (id)
    ) lock datarows
    go

    create table WorkItemInfo (
        workItemId bigint identity not null,
        creationDate datetime null,
        name varchar(255) null,
        processInstanceId bigint not null,
        state bigint not null,
        OPTLOCK int null,
        workItemByteArray image null,
        primary key (workItemId)
    ) lock datarows
    go

    create table email_header (
        id bigint identity not null,
        body text null,
        fromAddress varchar(255) null,
        language varchar(255) null,
        replyToAddress varchar(255) null,
        subject varchar(255) null,
        primary key (id)
    ) lock datarows
    go

    create table task_comment (
        id bigint identity not null,
        addedAt datetime null,
        text text null,
        addedBy_id varchar(255) null,
        TaskData_Comments_Id bigint null,
        primary key (id)
    ) lock datarows
    go

    alter table Attachment
        add constraint FKd5xpm81gxg8n40167lbu5rbfb
        foreign key (attachedBy_id)
        references OrganizationalEntity
    go

    alter table Attachment
        add constraint FKjj9psk52ifamilliyo16epwpc
        foreign key (TaskData_Attachments_Id)
        references Task
    go

    alter table BooleanExpression
        add constraint FKqth56a8k6d8pv6ngsu2vjp4kj
        foreign key (Escalation_Constraints_Id)
        references Escalation
    go
    
    alter table CaseIdInfo 
        add constraint UK_CaseIdInfo_1 unique (caseIdPrefix)
    go

    alter table CorrelationPropertyInfo
        add constraint FKbchyl7kb8i6ghvi3dbr86bgo0
        foreign key (correlationKey_keyId)
        references CorrelationKeyInfo
    go

    alter table Deadline
        add constraint FK361ggw230po88svgfasg36i2w
        foreign key (Deadlines_StartDeadLine_Id)
        references Task
    go

    alter table Deadline
        add constraint FKpeiadnoy228t35213t63c3imm
        foreign key (Deadlines_EndDeadLine_Id)
        references Task
    go

    alter table Delegation_delegates
        add constraint FKewkdyi0wrgy9byp6abyglpcxq
        foreign key (entity_id)
        references OrganizationalEntity
    go

    alter table Delegation_delegates
        add constraint FK85x3trafk3wfbrv719cafr591
        foreign key (task_id)
        references Task
    go

    alter table DeploymentStore
        add constraint UK_DeploymentStore_1 unique (DEPLOYMENT_ID)
    go

    alter table ErrorInfo
        add constraint FKdarp6ushq06q39jmij3fdpdbs
        foreign key (REQUEST_ID)
        references RequestInfo
    go

    alter table Escalation
        add constraint FK37v8ova8ti6jiblda7n6j298e
        foreign key (Deadline_Escalation_Id)
        references Deadline
    go

    alter table EventTypes
        add constraint FKj0o3uve2nqo5yrjwrkc9jfttq
        foreign key (InstanceId)
        references ProcessInstanceInfo
    go

    alter table I18NText
        add constraint FKcd6eb4q62d9ab8p0di8pb99ch
        foreign key (Task_Subjects_Id)
        references Task
    go

    alter table I18NText
        add constraint FKiogka67sji8fk4cp7a369895i
        foreign key (Task_Names_Id)
        references Task
    go

    alter table I18NText
        add constraint FKrisdlmalotmk64mdeqpo4s5m0
        foreign key (Task_Descriptions_Id)
        references Task
    go

    alter table I18NText
        add constraint FKqxgws3fnukyqlaet11tivqg5u
        foreign key (Reassignment_Documentation_Id)
        references Reassignment
    go

    alter table I18NText
        add constraint FKthf8ix3t3opf9hya1s04hwsx8
        foreign key (Notification_Subjects_Id)
        references Notification
    go

    alter table I18NText
        add constraint FKg2jsybeuc8pbj8ek8xwxutuyo
        foreign key (Notification_Names_Id)
        references Notification
    go

    alter table I18NText
        add constraint FKp0m7uhipskrljktvfeubdgfid
        foreign key (Notification_Documentation_Id)
        references Notification
    go

    alter table I18NText
        add constraint FK6k8hmfvhko069970eghiy2ifp
        foreign key (Notification_Descriptions_Id)
        references Notification
    go

    alter table I18NText
        add constraint FK8wn7sw34q6bifsi1pvl2b1yyb
        foreign key (Deadline_Documentation_Id)
        references Deadline
    go

    alter table Notification
        add constraint FKoxq5uqfg4ylwyijsg2ubyflna
        foreign key (Escalation_Notifications_Id)
        references Escalation
    go

    alter table Notification_BAs
        add constraint FK378pb1cvjv54w4ljqpw99s3wr
        foreign key (entity_id)
        references OrganizationalEntity
    go

    alter table Notification_BAs
        add constraint FKb123fgeomc741s9yc014421yy
        foreign key (task_id)
        references Notification
    go

    alter table Notification_Recipients
        add constraint FKot769nimyq1jvw0m61pgsq5g3
        foreign key (entity_id)
        references OrganizationalEntity
    go

    alter table Notification_Recipients
        add constraint FKn7v944d0hw37bh0auv4gr3hsf
        foreign key (task_id)
        references Notification
    go

    alter table Notification_email_header
        add constraint UK_F30FE3446CEA0510 unique (emailHeaders_id)
    go

    alter table Notification_email_header
        add constraint FKd74pdu41avy2f7a8qyp7wn2n
        foreign key (emailHeaders_id)
        references email_header
    go

    alter table Notification_email_header
        add constraint FKfdnoyp8rl0kxu29l4pyaa5566
        foreign key (Notification_id)
        references Notification
    go

    alter table PeopleAssignments_BAs
        add constraint FKa90cdfgc4km384n1ataqigq67
        foreign key (entity_id)
        references OrganizationalEntity
    go

    alter table PeopleAssignments_BAs
        add constraint FKt4xs0glwhbsa0xwg69r6xduv9
        foreign key (task_id)
        references Task
    go

    alter table PeopleAssignments_ExclOwners
        add constraint FK5ituvd6t8uvp63hsx6282xo6h
        foreign key (entity_id)
        references OrganizationalEntity
    go

    alter table PeopleAssignments_ExclOwners
        add constraint FKqxbjm1b3dl7w8w2f2y6sk0fv8
        foreign key (task_id)
        references Task
    go

    alter table PeopleAssignments_PotOwners
        add constraint FKsa3rrrjsm1qw98ajbbu2s7cjr
        foreign key (entity_id)
        references OrganizationalEntity
    go

    alter table PeopleAssignments_PotOwners
        add constraint FKh8oqmk4iuh2pmpgby6g8r3jd1
        foreign key (task_id)
        references Task
    go

    alter table PeopleAssignments_Recipients
        add constraint FKrd0h9ud1bhs9waf2mdmiv6j2r
        foreign key (entity_id)
        references OrganizationalEntity
    go

    alter table PeopleAssignments_Recipients
        add constraint FK9gnbv6bplxkxoedj35vg8n7ir
        foreign key (task_id)
        references Task
    go

    alter table PeopleAssignments_Stakeholders
        add constraint FK9uy76cu650rg1nnkrtjwj1e9t
        foreign key (entity_id)
        references OrganizationalEntity
    go

    alter table PeopleAssignments_Stakeholders
        add constraint FKaeyk4nwslvx0jywjomjq7083i
        foreign key (task_id)
        references Task
    go

    alter table QueryDefinitionStore
        add constraint UK_4ry5gt77jvq0orfttsoghta2j unique (qName)
    go

    alter table Reassignment
        add constraint FKessy30safh44b30f1cfoujv2k
        foreign key (Escalation_Reassignments_Id)
        references Escalation
    go

    alter table Reassignment_potentialOwners
        add constraint FKsqrmpvehlc4qe9i0km22nmkjl
        foreign key (entity_id)
        references OrganizationalEntity
    go

    alter table Reassignment_potentialOwners
        add constraint FKftegfexshix752bh2jfxf6bnh
        foreign key (task_id)
        references Reassignment
    go

    alter table Task
        add constraint FK48d1bfgwf0jqow1yk8ku4xcpi
        foreign key (taskInitiator_id)
        references OrganizationalEntity
    go

    alter table Task
        add constraint FKpmkxvqq63aed2y2boruu53a0s
        foreign key (actualOwner_id)
        references OrganizationalEntity
    go

    alter table Task
        add constraint FKexuboqnbla7jfyyesyo61ucmb
        foreign key (createdBy_id)
        references OrganizationalEntity
    go

    alter table task_comment
        add constraint FKqb4mkarf209y9546w7n75lb7a
        foreign key (addedBy_id)
        references OrganizationalEntity
    go

    alter table task_comment
        add constraint FKm2mwc1ukcpdsiqwgkoroy6ise
        foreign key (TaskData_Comments_Id)
        references Task
    go

    create index IDX_Attachment_Id ON Attachment(attachedBy_id)
	go
    create index IDX_Attachment_DataId ON Attachment(TaskData_Attachments_Id)
	go
    create index IDX_BoolExpr_Id ON BooleanExpression(Escalation_Constraints_Id)
	go
    create index IDX_CorrPropInfo_Id ON CorrelationPropertyInfo(correlationKey_keyId)
	go
    create index IDX_Deadline_StartId ON Deadline(Deadlines_StartDeadLine_Id)
	go
    create index IDX_Deadline_EndId ON Deadline(Deadlines_EndDeadLine_Id)
	go
    create index IDX_Delegation_EntityId ON Delegation_delegates(entity_id)
	go
    create index IDX_Delegation_TaskId ON Delegation_delegates(task_id)
	go
    create index IDX_ErrorInfo_Id ON ErrorInfo(REQUEST_ID)
	go
    create index IDX_Escalation_Id ON Escalation(Deadline_Escalation_Id)
	go
    create index IDX_EventTypes_Id ON EventTypes(InstanceId)
	go
    create index IDX_I18NText_SubjId ON I18NText(Task_Subjects_Id)
	go
    create index IDX_I18NText_NameId ON I18NText(Task_Names_Id)
	go
    create index IDX_I18NText_DescrId ON I18NText(Task_Descriptions_Id)
	go
    create index IDX_I18NText_ReassignId ON I18NText(Reassignment_Documentation_Id)
	go
    create index IDX_I18NText_NotSubjId ON I18NText(Notification_Subjects_Id)
	go
    create index IDX_I18NText_NotNamId ON I18NText(Notification_Names_Id)
	go
    create index IDX_I18NText_NotDocId ON I18NText(Notification_Documentation_Id)
	go
    create index IDX_I18NText_NotDescrId ON I18NText(Notification_Descriptions_Id)
	go
    create index IDX_I18NText_DeadDocId ON I18NText(Deadline_Documentation_Id)
	go
    create index IDX_Not_EscId ON Notification(Escalation_Notifications_Id)
	go
    create index IDX_NotBAs_Entity ON Notification_BAs(entity_id)
	go
    create index IDX_NotBAs_Task ON Notification_BAs(task_id)
	go
    create index IDX_NotRec_Entity ON Notification_Recipients(entity_id)
	go
    create index IDX_NotRec_Task ON Notification_Recipients(task_id)
	go
    create index IDX_NotEmail_Header ON Notification_email_header(emailHeaders_id)
	go
    create index IDX_NotEmail_Not ON Notification_email_header(Notification_id)
	go
    create index IDX_PAsBAs_Entity ON PeopleAssignments_BAs(entity_id)
	go
    create index IDX_PAsBAs_Task ON PeopleAssignments_BAs(task_id)
	go
    create index IDX_PAsExcl_Entity ON PeopleAssignments_ExclOwners(entity_id)
	go
    create index IDX_PAsExcl_Task ON PeopleAssignments_ExclOwners(task_id)
	go
    create index IDX_PAsPot_Entity ON PeopleAssignments_PotOwners(entity_id)
	go
    create index IDX_PAsPot_Task ON PeopleAssignments_PotOwners(task_id)
	go
    create index IDX_PAsRecip_Entity ON PeopleAssignments_Recipients(entity_id)
	go
    create index IDX_PAsRecip_Task ON PeopleAssignments_Recipients(task_id)
	go
    create index IDX_PAsStake_Entity ON PeopleAssignments_Stakeholders(entity_id)
	go
    create index IDX_PAsStake_Task ON PeopleAssignments_Stakeholders(task_id)
	go
    create index IDX_Reassign_Esc ON Reassignment(Escalation_Reassignments_Id)
	go
    create index IDX_ReassignPO_Entity ON Reassignment_potentialOwners(entity_id)
	go
    create index IDX_ReassignPO_Task ON Reassignment_potentialOwners(task_id)
	go
    create index IDX_Task_Initiator ON Task(taskInitiator_id)
	go
    create index IDX_Task_ActualOwner ON Task(actualOwner_id)
	go
    create index IDX_Task_CreatedBy ON Task(createdBy_id)
	go
    create index IDX_TaskComments_CreatedBy ON task_comment(addedBy_id)
	go
    create index IDX_TaskComments_Id ON task_comment(TaskData_Comments_Id)
	go

    create index IDX_Task_processInstanceId on Task(processInstanceId)
	go
    create index IDX_Task_processId on Task(processId)
	go
    create index IDX_Task_status on Task(status)
	go
    create index IDX_Task_archived on Task(archived)
	go
    create index IDX_Task_workItemId on Task(workItemId)
	go

    create index IDX_EventTypes_element ON EventTypes(element)
	go

    create index IDX_CMI_Context ON ContextMappingInfo(CONTEXT_ID)
	go
    create index IDX_CMI_KSession ON ContextMappingInfo(KSESSION_ID)
	go
    create index IDX_CMI_Owner ON ContextMappingInfo(OWNER_ID)
	go

    create index IDX_RequestInfo_status ON RequestInfo(status)
	go
    create index IDX_RequestInfo_timestamp ON RequestInfo(timestamp)
	go
    create index IDX_RequestInfo_owner ON RequestInfo(owner)
	go

    create index IDX_BAMTaskSumm_createdDate on BAMTaskSummary(createdDate)
	go
    create index IDX_BAMTaskSumm_duration on BAMTaskSummary(duration)
	go
    create index IDX_BAMTaskSumm_endDate on BAMTaskSummary(endDate)
	go
    create index IDX_BAMTaskSumm_pInstId on BAMTaskSummary(processInstanceId)
	go
    create index IDX_BAMTaskSumm_startDate on BAMTaskSummary(startDate)
	go
    create index IDX_BAMTaskSumm_status on BAMTaskSummary(status)
	go
    create index IDX_BAMTaskSumm_taskId on BAMTaskSummary(taskId)
	go
    create index IDX_BAMTaskSumm_taskName on BAMTaskSummary(taskName)
	go
    create index IDX_BAMTaskSumm_userId on BAMTaskSummary(userId)
	go

    create index IDX_PInstLog_duration on ProcessInstanceLog(duration)
	go
    create index IDX_PInstLog_end_date on ProcessInstanceLog(end_date)
	go
    create index IDX_PInstLog_extId on ProcessInstanceLog(externalId)
	go
    create index IDX_PInstLog_user_identity on ProcessInstanceLog(user_identity)
	go
    create index IDX_PInstLog_outcome on ProcessInstanceLog(outcome)
	go
    create index IDX_PInstLog_parentPInstId on ProcessInstanceLog(parentProcessInstanceId)
	go
    create index IDX_PInstLog_pId on ProcessInstanceLog(processId)
	go
    create index IDX_PInstLog_pInsteDescr on ProcessInstanceLog(processInstanceDescription)
	go
    create index IDX_PInstLog_pInstId on ProcessInstanceLog(processInstanceId)
	go
    create index IDX_PInstLog_pName on ProcessInstanceLog(processName)
	go
    create index IDX_PInstLog_pVersion on ProcessInstanceLog(processVersion)
	go
    create index IDX_PInstLog_start_date on ProcessInstanceLog(start_date)
	go
    create index IDX_PInstLog_status on ProcessInstanceLog(status)
	go
    create index IDX_PInstLog_correlation on ProcessInstanceLog(correlationKey)
	go

    create index IDX_VInstLog_pInstId on VariableInstanceLog(processInstanceId)
	go
    create index IDX_VInstLog_varId on VariableInstanceLog(variableId)
	go
    create index IDX_VInstLog_pId on VariableInstanceLog(processId)
	go

    create index IDX_NInstLog_pInstId on NodeInstanceLog(processInstanceId)
	go
    create index IDX_NInstLog_nodeType on NodeInstanceLog(nodeType)
	go
    create index IDX_NInstLog_pId on NodeInstanceLog(processId)
	go

    create index IDX_ErrorInfo_pInstId on ExecutionErrorInfo(PROCESS_INST_ID)
	go
    create index IDX_ErrorInfo_errorAck on ExecutionErrorInfo(ERROR_ACK)
	go

    create index IDX_AuditTaskImpl_taskId on AuditTaskImpl(taskId)
	go
    create index IDX_AuditTaskImpl_pInstId on AuditTaskImpl(processInstanceId)
	go
    create index IDX_AuditTaskImpl_workItemId on AuditTaskImpl(workItemId)
	go
    create index IDX_AuditTaskImpl_name on AuditTaskImpl(name)
	go
    create index IDX_AuditTaskImpl_processId on AuditTaskImpl(processId)
	go
    create index IDX_AuditTaskImpl_status on AuditTaskImpl(status)
	go

    create index IDX_TaskVariableImpl_taskId on TaskVariableImpl(taskId)
	go
    create index IDX_TaskVariableImpl_pInstId on TaskVariableImpl(processInstanceId)
	go
    create index IDX_TaskVariableImpl_processId on TaskVariableImpl(processId)
	go