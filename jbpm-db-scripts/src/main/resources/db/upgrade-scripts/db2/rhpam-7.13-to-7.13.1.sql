ALTER TABLE TimerMappingInfo ADD COLUMN info blob(2147483647);
DROP INDEX EventTypes.IDX_EventTypes_element;
CREATE INDEX IDX_EventTypes_IdElement ON EventTypes(InstanceId,element);
