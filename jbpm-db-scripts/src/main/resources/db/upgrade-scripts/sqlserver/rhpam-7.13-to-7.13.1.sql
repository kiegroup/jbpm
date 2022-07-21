ALTER TABLE TimerMappingInfo ADD processInstanceId numeric(19,0);
ALTER TABLE TimerMappingInfo ALTER timerId numeric(19,0);
DROP INDEX EventTypes.IDX_EventTypes_element;
CREATE INDEX IDX_EventTypes_IdElement ON EventTypes(InstanceId,element);
