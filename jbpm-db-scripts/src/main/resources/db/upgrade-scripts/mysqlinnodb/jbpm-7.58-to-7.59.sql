alter table Attachment modify attachedAt DATETIME(6);

alter table AuditTaskImpl modify activationTime DATETIME(6);
alter table AuditTaskImpl modify createdOn DATETIME(6);
alter table AuditTaskImpl modify dueDate DATETIME(6);
alter table AuditTaskImpl modify lastModificationDate DATETIME(6);

alter table BAMTaskSummary modify createdDate DATETIME(6);
alter table BAMTaskSummary modify endDate DATETIME(6);
alter table BAMTaskSummary modify startDate DATETIME(6);

alter table CaseFileDataLog modify lastModified DATETIME(6);

alter table Deadline modify deadline_date DATETIME(6);

alter table DeploymentStore modify updateDate DATETIME(6);

alter table ErrorInfo modify timestamp DATETIME(6);

alter table ExecutionErrorInfo modify ERROR_ACK_AT DATETIME(6);
alter table ExecutionErrorInfo modify ERROR_DATE DATETIME(6);

alter table NodeInstanceLog modify log_date DATETIME(6);
alter table NodeInstanceLog modify sla_due_date DATETIME(6);

alter table ProcessInstanceInfo modify lastModificationDate DATETIME(6);
alter table ProcessInstanceInfo modify lastReadDate DATETIME(6);
alter table ProcessInstanceInfo modify startDate DATETIME(6);

alter table ProcessInstanceLog modify end_date DATETIME(6);
alter table ProcessInstanceLog modify sla_due_date DATETIME(6);
alter table ProcessInstanceLog modify start_date DATETIME(6);

alter table RequestInfo modify timestamp DATETIME(6);

alter table SessionInfo modify lastModificationDate DATETIME(6);
alter table SessionInfo modify startDate DATETIME(6);

alter table Task modify activationTime DATETIME(6);
alter table Task modify createdOn DATETIME(6);
alter table Task modify expirationTime DATETIME(6);

alter table TaskEvent modify logTime DATETIME(6);

alter table TaskVariableImpl modify modificationDate DATETIME(6);

alter table VariableInstanceLog modify log_date DATETIME(6);

alter table WorkItemInfo modify creationDate DATETIME(6);

alter table task_comment modify addedAt DATETIME(6);

create index IDX_CaseRoleAssignLog_caseId on CaseRoleAssignmentLog(caseId);
create index IDX_CaseRoleAssignLog_processInstanceId on CaseRoleAssignmentLog(processInstanceId);
create index IDX_CaseFileDataLog_caseId on CaseFileDataLog(caseId);
create index IDX_CaseFileDataLog_itemName on CaseFileDataLog(itemName);
