alter table AuditTaskImpl add end_date timestamp;
alter table BAMTaskSummary add end_date timestamp;
alter table TaskEvent add end_date timestamp;
alter table NodeInstanceLog add end_date timestamp;
alter table VariableInstanceLog add end_date timestamp;
create index IDX_TaskEvent_processInstanceId on TaskEvent (processInstanceId);

