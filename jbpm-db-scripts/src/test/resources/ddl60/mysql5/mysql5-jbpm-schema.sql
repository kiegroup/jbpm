    create table Attachment (
        id bigint not null auto_increment,
        accessType integer,
        attachedAt datetime,
        attachmentContentId bigint not null,
        contentType varchar(255),
        name varchar(255),
        attachment_size integer,
        attachedBy_id varchar(255),
        TaskData_Attachments_Id bigint,
        primary key (id)
    );

    create table BAMTaskSummary (
        pk bigint not null auto_increment,
        createdDate datetime,
        duration bigint,
        endDate datetime,
        processInstanceId bigint not null,
        startDate datetime,
        status varchar(255),
        taskId bigint not null,
        taskName varchar(255),
        userId varchar(255),
        OPTLOCK integer,
        primary key (pk)
    );

    create table BooleanExpression (
        id bigint not null auto_increment,
        expression longtext,
        type varchar(255),
        Escalation_Constraints_Id bigint,
        primary key (id)
    );

    create table Content (
        id bigint not null auto_increment,
        content longblob,
        primary key (id)
    );

    create table ContextMappingInfo (
        mappingId bigint not null auto_increment,
        CONTEXT_ID varchar(255) not null,
        KSESSION_ID integer not null,
        OPTLOCK integer,
        primary key (mappingId)
    );

    create table CorrelationKeyInfo (
        keyId bigint not null auto_increment,
        name varchar(255),
        processInstanceId bigint not null,
        OPTLOCK integer,
        primary key (keyId)
    );

    create table CorrelationPropertyInfo (
        propertyId bigint not null auto_increment,
        name varchar(255),
        value varchar(255),
        OPTLOCK integer,
        correlationKey_keyId bigint,
        primary key (propertyId)
    );

    create table Deadline (
        id bigint not null auto_increment,
        deadline_date datetime,
        escalated smallint,
        Deadlines_StartDeadLine_Id bigint,
        Deadlines_EndDeadLine_Id bigint,
        primary key (id)
    );

    create table Delegation_delegates (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table ErrorInfo (
        id bigint not null auto_increment,
        message varchar(255),
        stacktrace varchar(5000),
        timestamp datetime,
        REQUEST_ID bigint not null,
        primary key (id)
    );

    create table Escalation (
        id bigint not null auto_increment,
        name varchar(255),
        Deadline_Escalation_Id bigint,
        primary key (id)
    );

    create table EventTypes (
        InstanceId bigint not null,
        element varchar(255)
    );

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
    );

    create table NodeInstanceLog (
        id bigint not null auto_increment,
        connection varchar(255),
        log_date datetime,
        externalId varchar(255),
        nodeId varchar(255),
        nodeInstanceId varchar(255),
        nodeName varchar(255),
        nodeType varchar(255),
        processId varchar(255),
        processInstanceId bigint not null,
        type integer not null,
        workItemId bigint,
        primary key (id)
    );

    create table Notification (
        DTYPE varchar(31) not null,
        id bigint not null auto_increment,
        priority integer not null,
        Escalation_Notifications_Id bigint,
        primary key (id)
    );

    create table Notification_BAs (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table Notification_Recipients (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table Notification_email_header (
        Notification_id bigint not null,
        emailHeaders_id bigint not null,
        mapkey varchar(255) not null,
        primary key (Notification_id, mapkey)
    );

    create table OrganizationalEntity (
        DTYPE varchar(31) not null,
        id varchar(255) not null,
        primary key (id)
    );

    create table PeopleAssignments_BAs (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table PeopleAssignments_ExclOwners (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table PeopleAssignments_PotOwners (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table PeopleAssignments_Recipients (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table PeopleAssignments_Stakeholders (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table ProcessInstanceInfo (
        InstanceId bigint not null auto_increment,
        lastModificationDate datetime,
        lastReadDate datetime,
        processId varchar(255),
        processInstanceByteArray longblob,
        startDate datetime,
        state integer not null,
        OPTLOCK integer,
        primary key (InstanceId)
    );

    create table ProcessInstanceLog (
        id bigint not null auto_increment,
        duration bigint,
        end_date datetime,
        externalId varchar(255),
        user_identity varchar(255),
        outcome varchar(255),
        parentProcessInstanceId bigint,
        processId varchar(255),
        processInstanceId bigint not null,
        processName varchar(255),
        processVersion varchar(255),
        start_date datetime,
        status integer,
        primary key (id)
    );

    create table Reassignment (
        id bigint not null auto_increment,
        Escalation_Reassignments_Id bigint,
        primary key (id)
    );

    create table Reassignment_potentialOwners (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table RequestInfo (
        id bigint not null auto_increment,
        commandName varchar(255),
        deploymentId varchar(255),
        executions integer not null,
        businessKey varchar(255),
        message varchar(255),
        requestData longblob,
        responseData longblob,
        retries integer not null,
        status varchar(255),
        timestamp datetime,
        primary key (id)
    );

    create table SessionInfo (
        id integer not null auto_increment,
        lastModificationDate datetime,
        rulesByteArray longblob,
        startDate datetime,
        OPTLOCK integer,
        primary key (id)
    );

    create table Task (
        id bigint not null auto_increment,
        archived smallint,
        allowedToDelegate varchar(255),
        formName varchar(255),
        priority integer not null,
        subTaskStrategy varchar(255),
        activationTime datetime,
        createdOn datetime,
        deploymentId varchar(255),
        documentAccessType integer,
        documentContentId bigint not null,
        documentType varchar(255),
        expirationTime datetime,
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
        processSessionId integer not null,
        skipable boolean not null,
        status varchar(255),
        workItemId bigint not null,
        taskType varchar(255),
        OPTLOCK integer,
        taskInitiator_id varchar(255),
        actualOwner_id varchar(255),
        createdBy_id varchar(255),
        primary key (id)
    );

    create table TaskDef (
        id bigint not null auto_increment,
        name varchar(255),
        priority integer not null,
        primary key (id)
    );

    create table TaskEvent (
        id bigint not null auto_increment,
        logTime datetime,
        taskId bigint,
        type varchar(255),
        userId varchar(255),
        OPTLOCK integer,
        primary key (id)
    );

    create table VariableInstanceLog (
        id bigint not null auto_increment,
        log_date datetime,
        externalId varchar(255),
        oldValue varchar(255),
        processId varchar(255),
        processInstanceId bigint not null,
        value varchar(255),
        variableId varchar(255),
        variableInstanceId varchar(255),
        primary key (id)
    );

    create table WorkItemInfo (
        workItemId bigint not null auto_increment,
        creationDate datetime,
        name varchar(255),
        processInstanceId bigint not null,
        state bigint not null,
        OPTLOCK integer,
        workItemByteArray longblob,
        primary key (workItemId)
    );

    create table email_header (
        id bigint not null auto_increment,
        body longtext,
        fromAddress varchar(255),
        language varchar(255),
        replyToAddress varchar(255),
        subject varchar(255),
        primary key (id)
    );

    create table task_comment (
        id bigint not null auto_increment,
        addedAt datetime,
        text longtext,
        addedBy_id varchar(255),
        TaskData_Comments_Id bigint,
        primary key (id)
    );

    alter table Attachment 
        add index FK1C93543D937BFB5 (attachedBy_id), 
        add constraint FK1C93543D937BFB5 
        foreign key (attachedBy_id) 
        references OrganizationalEntity (id);

    alter table Attachment 
        add index FK1C9354333CA892A (TaskData_Attachments_Id), 
        add constraint FK1C9354333CA892A 
        foreign key (TaskData_Attachments_Id) 
        references Task (id);

    alter table BooleanExpression 
        add index FKE3D208C06C97C90E (Escalation_Constraints_Id), 
        add constraint FKE3D208C06C97C90E 
        foreign key (Escalation_Constraints_Id) 
        references Escalation (id);

    alter table CorrelationPropertyInfo 
        add index FK761452A5D87156ED (correlationKey_keyId), 
        add constraint FK761452A5D87156ED 
        foreign key (correlationKey_keyId) 
        references CorrelationKeyInfo (keyId);

    alter table Deadline 
        add index FK21DF3E78A9FE0EF4 (Deadlines_StartDeadLine_Id), 
        add constraint FK21DF3E78A9FE0EF4 
        foreign key (Deadlines_StartDeadLine_Id) 
        references Task (id);

    alter table Deadline 
        add index FK21DF3E78695E4DDB (Deadlines_EndDeadLine_Id), 
        add constraint FK21DF3E78695E4DDB 
        foreign key (Deadlines_EndDeadLine_Id) 
        references Task (id);

    alter table Delegation_delegates 
        add index FK47485D5772B3A123 (entity_id), 
        add constraint FK47485D5772B3A123 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table Delegation_delegates 
        add index FK47485D57786553A5 (task_id), 
        add constraint FK47485D57786553A5 
        foreign key (task_id) 
        references Task (id);

    alter table ErrorInfo 
        add index FK8B1186B6724A467 (REQUEST_ID), 
        add constraint FK8B1186B6724A467 
        foreign key (REQUEST_ID) 
        references RequestInfo (id);

    alter table Escalation 
        add index FK67B2C6B5D1E5CC1 (Deadline_Escalation_Id), 
        add constraint FK67B2C6B5D1E5CC1 
        foreign key (Deadline_Escalation_Id) 
        references Deadline (id);

    alter table EventTypes 
        add index FKB0E5621F7665489A (InstanceId), 
        add constraint FKB0E5621F7665489A 
        foreign key (InstanceId) 
        references ProcessInstanceInfo (InstanceId);

    alter table I18NText 
        add index FK2349686BF4ACCD69 (Task_Subjects_Id), 
        add constraint FK2349686BF4ACCD69 
        foreign key (Task_Subjects_Id) 
        references Task (id);

    alter table I18NText 
        add index FK2349686B424B187C (Task_Names_Id), 
        add constraint FK2349686B424B187C 
        foreign key (Task_Names_Id) 
        references Task (id);

    alter table I18NText 
        add index FK2349686BAB648139 (Task_Descriptions_Id), 
        add constraint FK2349686BAB648139 
        foreign key (Task_Descriptions_Id) 
        references Task (id);

    alter table I18NText 
        add index FK2349686BB340A2AA (Reassignment_Documentation_Id), 
        add constraint FK2349686BB340A2AA 
        foreign key (Reassignment_Documentation_Id) 
        references Reassignment (id);

    alter table I18NText 
        add index FK2349686BF0CDED35 (Notification_Subjects_Id), 
        add constraint FK2349686BF0CDED35 
        foreign key (Notification_Subjects_Id) 
        references Notification (id);

    alter table I18NText 
        add index FK2349686BCC03ED3C (Notification_Names_Id), 
        add constraint FK2349686BCC03ED3C 
        foreign key (Notification_Names_Id) 
        references Notification (id);

    alter table I18NText 
        add index FK2349686B77C1C08A (Notification_Documentation_Id), 
        add constraint FK2349686B77C1C08A 
        foreign key (Notification_Documentation_Id) 
        references Notification (id);

    alter table I18NText 
        add index FK2349686B18DDFE05 (Notification_Descriptions_Id), 
        add constraint FK2349686B18DDFE05 
        foreign key (Notification_Descriptions_Id) 
        references Notification (id);

    alter table I18NText 
        add index FK2349686B78AF072A (Deadline_Documentation_Id), 
        add constraint FK2349686B78AF072A 
        foreign key (Deadline_Documentation_Id) 
        references Deadline (id);

    alter table Notification 
        add index FK2D45DD0BC0C0F29C (Escalation_Notifications_Id), 
        add constraint FK2D45DD0BC0C0F29C 
        foreign key (Escalation_Notifications_Id) 
        references Escalation (id);

    alter table Notification_BAs 
        add index FK2DD68EE072B3A123 (entity_id), 
        add constraint FK2DD68EE072B3A123 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table Notification_BAs 
        add index FK2DD68EE093F2090B (task_id), 
        add constraint FK2DD68EE093F2090B 
        foreign key (task_id) 
        references Notification (id);

    alter table Notification_Recipients 
        add index FK98FD214E72B3A123 (entity_id), 
        add constraint FK98FD214E72B3A123 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table Notification_Recipients 
        add index FK98FD214E93F2090B (task_id), 
        add constraint FK98FD214E93F2090B 
        foreign key (task_id) 
        references Notification (id);

    alter table Notification_email_header 
        add constraint UK_F30FE3446CEA0510 unique (emailHeaders_id);

    alter table Notification_email_header 
        add index FKF30FE3448BED1339 (emailHeaders_id), 
        add constraint FKF30FE3448BED1339 
        foreign key (emailHeaders_id) 
        references email_header (id);

    alter table Notification_email_header 
        add index FKF30FE3443E3E97EB (Notification_id), 
        add constraint FKF30FE3443E3E97EB 
        foreign key (Notification_id) 
        references Notification (id);

    alter table PeopleAssignments_BAs 
        add index FK9D8CF4EC72B3A123 (entity_id), 
        add constraint FK9D8CF4EC72B3A123 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table PeopleAssignments_BAs 
        add index FK9D8CF4EC786553A5 (task_id), 
        add constraint FK9D8CF4EC786553A5 
        foreign key (task_id) 
        references Task (id);

    alter table PeopleAssignments_ExclOwners 
        add index FKC77B97E472B3A123 (entity_id), 
        add constraint FKC77B97E472B3A123 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table PeopleAssignments_ExclOwners 
        add index FKC77B97E4786553A5 (task_id), 
        add constraint FKC77B97E4786553A5 
        foreign key (task_id) 
        references Task (id);

    alter table PeopleAssignments_PotOwners 
        add index FK1EE418D72B3A123 (entity_id), 
        add constraint FK1EE418D72B3A123 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table PeopleAssignments_PotOwners 
        add index FK1EE418D786553A5 (task_id), 
        add constraint FK1EE418D786553A5 
        foreign key (task_id) 
        references Task (id);

    alter table PeopleAssignments_Recipients 
        add index FKC6F615C272B3A123 (entity_id), 
        add constraint FKC6F615C272B3A123 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table PeopleAssignments_Recipients 
        add index FKC6F615C2786553A5 (task_id), 
        add constraint FKC6F615C2786553A5 
        foreign key (task_id) 
        references Task (id);

    alter table PeopleAssignments_Stakeholders 
        add index FK482F79D572B3A123 (entity_id), 
        add constraint FK482F79D572B3A123 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table PeopleAssignments_Stakeholders 
        add index FK482F79D5786553A5 (task_id), 
        add constraint FK482F79D5786553A5 
        foreign key (task_id) 
        references Task (id);

    alter table Reassignment 
        add index FK724D056062A1E871 (Escalation_Reassignments_Id), 
        add constraint FK724D056062A1E871 
        foreign key (Escalation_Reassignments_Id) 
        references Escalation (id);

    alter table Reassignment_potentialOwners 
        add index FK90B59CFF72B3A123 (entity_id), 
        add constraint FK90B59CFF72B3A123 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table Reassignment_potentialOwners 
        add index FK90B59CFF35D2FEE0 (task_id), 
        add constraint FK90B59CFF35D2FEE0 
        foreign key (task_id) 
        references Reassignment (id);

    alter table Task 
        add index FK27A9A53C55C806 (taskInitiator_id), 
        add constraint FK27A9A53C55C806 
        foreign key (taskInitiator_id) 
        references OrganizationalEntity (id);

    alter table Task 
        add index FK27A9A5B723BE8B (actualOwner_id), 
        add constraint FK27A9A5B723BE8B 
        foreign key (actualOwner_id) 
        references OrganizationalEntity (id);

    alter table Task 
        add index FK27A9A55427E8F1 (createdBy_id), 
        add constraint FK27A9A55427E8F1 
        foreign key (createdBy_id) 
        references OrganizationalEntity (id);

    alter table task_comment 
        add index FK61F475A57A3215D9 (addedBy_id), 
        add constraint FK61F475A57A3215D9 
        foreign key (addedBy_id) 
        references OrganizationalEntity (id);

    alter table task_comment 
        add index FK61F475A5F510CB46 (TaskData_Comments_Id), 
        add constraint FK61F475A5F510CB46 
        foreign key (TaskData_Comments_Id) 
        references Task (id);

    create index IDX_EventTypes_element ON EventTypes(element);
