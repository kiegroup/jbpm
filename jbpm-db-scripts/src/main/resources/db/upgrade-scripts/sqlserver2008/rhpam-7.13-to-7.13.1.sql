ALTER TABLE TimerMappingInfo ADD processInstanceId bigint;
ALTER TABLE TimerMappingInfo ALTER timerId bigint;
DROP INDEX EventTypes.IDX_EventTypes_element;
CREATE INDEX IDX_EventTypes_IdElement ON EventTypes(InstanceId,element);
