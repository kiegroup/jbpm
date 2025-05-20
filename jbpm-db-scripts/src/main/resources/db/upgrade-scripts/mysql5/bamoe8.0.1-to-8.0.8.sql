    create index IDX_RequestInfo_status_pInstId ON RequestInfo (status, processInstanceId);
    create index IDX_PInstLog_pInstId_status ON ProcessInstanceLog (processInstanceId, status);