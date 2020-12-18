alter table CorrelationKeyInfo alter column "name" not null;
alter table CorrelationKeyInfo add constraint IDX_CorrelationKeyInfo_name unique (name);