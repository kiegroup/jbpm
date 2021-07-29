alter table AuditTaskImpl add column end_date datetime;
alter table BAMTaskSummary add column end_date datetime;
alter table TaskEvent add column end_date datetime;
alter table NodeInstanceLog add column end_date datetime;
alter table VariableInstanceLog add column end_date datetime;
create index IDX_TaskEvent_processInstanceId on TaskEvent (processInstanceId);
