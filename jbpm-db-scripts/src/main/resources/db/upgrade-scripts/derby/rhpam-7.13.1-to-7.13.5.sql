create index IDX_PInstLog_pInstId_status ON ProcessInstanceLog (processInstanceId, status)  WHERE status IN (0,1,4);
