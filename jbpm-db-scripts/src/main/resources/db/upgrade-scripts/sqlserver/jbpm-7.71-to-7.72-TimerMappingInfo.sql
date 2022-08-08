ALTER TABLE TimerMappingInfo ADD processInstanceId numeric(19,0);
ALTER TABLE TimerMappingInfo ALTER COLUMN timerId numeric(19,0) NULL;