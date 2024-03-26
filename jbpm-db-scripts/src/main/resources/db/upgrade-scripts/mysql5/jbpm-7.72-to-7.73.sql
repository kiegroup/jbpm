DROP INDEX IDX_EventTypes_element;
DROP INDEX IDX_EventTypes_compound;
CREATE INDEX IDX_EventTypes_IdElement ON EventTypes(InstanceId,element);