alter table CorrelationKeyInfo alter column "name" not null
go
alter table CorrelationKeyInfo add constraint IDX_CorrelationKeyInfo_name unique (name)
go