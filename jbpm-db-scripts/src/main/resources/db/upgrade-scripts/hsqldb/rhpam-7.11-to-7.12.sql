alter table AuditTaskImpl add column end_date timestamp;
alter table BAMTaskSummary add column end_date timestamp;
alter table TaskEvent add column end_date timestamp;
alter table NodeInstanceLog add column end_date timestamp;
alter table VariableInstanceLog add column end_date timestamp;
create index IDX_TaskEvent_processInstanceId on TaskEvent (processInstanceId);
