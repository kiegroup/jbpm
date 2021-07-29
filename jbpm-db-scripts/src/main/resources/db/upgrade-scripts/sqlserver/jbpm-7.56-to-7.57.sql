alter table AuditTaskImpl add end_date datetime;
alter table BAMTaskSummary add end_date datetime;
alter table TaskEvent add end_date datetime;
alter table NodeInstanceLog add end_date datetime;
alter table VariableInstanceLog add end_date datetime;
create index IDX_TaskEvent_processInstanceId on TaskEvent (processInstanceId);



