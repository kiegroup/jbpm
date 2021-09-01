alter table AuditTaskImpl drop column end_date
go
alter table BAMTaskSummary drop column end_date
go
alter table TaskEvent drop column end_date
go
alter table NodeInstanceLog drop column end_date
go
alter table VariableInstanceLog drop column end_date
go
alter table NodeInstanceLog add column observation varchar(255)
go
