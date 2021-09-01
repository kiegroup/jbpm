alter table AuditTaskImpl drop column end_date;
alter table BAMTaskSummary drop column end_date;
alter table TaskEvent drop column end_date;
alter table NodeInstanceLog drop column end_date;
alter table VariableInstanceLog drop column end_date;
alter table NodeInstanceLog add observation varchar(255);
