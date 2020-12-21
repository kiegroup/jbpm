alter table CorrelationKeyInfo alter column name set not null
go
alter table CorrelationKeyInfo add constraint IDX_CorrelationKeyInfo_name unique (name)
go