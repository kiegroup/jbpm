create index IDX_TaskEvent_processInstanceId on TaskEvent (processInstanceId)
go
create index IDX_CaseRoleAssignLog_caseId on CaseRoleAssignmentLog(caseId)
go
create index IDX_CaseRoleAssignLog_processInstanceId on CaseRoleAssignmentLog(processInstanceId)
go
create index IDX_CaseFileDataLog_caseId on CaseFileDataLog(caseId)
go
create index IDX_CaseFileDataLog_itemName on CaseFileDataLog(itemName)
go

