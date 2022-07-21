DROP INDEX EventTypes.IDX_EventTypes_element;
go
CREATE INDEX IDX_EventTypes_IdElement ON EventTypes(InstanceId,element);
go