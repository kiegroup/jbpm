alter table AuditTaskImpl add end_date datetime;
alter table BAMTaskSummary add end_date datetime;
alter table TaskEvent add end_date datetime;
alter table NodeInstanceLog add end_date datetime;
alter table VariableInstanceLog add end_date datetime;
create index IDX_TaskEvent_processInstanceId on TaskEvent (processInstanceId);
create index IDX_CaseRoleAssignLog_caseId on CaseRoleAssignmentLog(caseId);
create index IDX_CaseRoleAssignLog_processInstanceId on CaseRoleAssignmentLog(processInstanceId);
create index IDX_CaseFileDataLog_caseId on CaseFileDataLog(caseId);
create index IDX_CaseFileDataLog_itemName on CaseFileDataLog(itemName);



