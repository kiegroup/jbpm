ALTER TABLE TimerMappingInfo ADD processInstanceId bigint;
ALTER TABLE TimerMappingInfo ALTER COLUMN timerId bigint NULL;
