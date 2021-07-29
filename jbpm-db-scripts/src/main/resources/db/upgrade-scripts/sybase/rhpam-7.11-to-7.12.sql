alter table AuditTaskImpl add column end_date datetime
go
alter table BAMTaskSummary add column end_date datetime
go
alter table TaskEvent add column end_date datetime
go
alter table NodeInstanceLog add column end_date datetime
go
alter table VariableInstanceLog add column end_date datetime
go
create index IDX_TaskEvent_processInstanceId on TaskEvent (processInstanceId)
go

