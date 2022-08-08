ALTER TABLE TimerMappingInfo ADD processInstanceId number(19,0);
ALTER TABLE TimerMappingInfo MODIFY (timerId NULL);