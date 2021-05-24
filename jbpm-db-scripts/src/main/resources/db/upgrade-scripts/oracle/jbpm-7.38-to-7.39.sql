create table PlanningTask (
    taskId number(19,0) not null,
    OPTLOCK number(10,0),
    assignedUser varchar2(255 char),
    taskIndex number(10,0) not null,
    lastModificationDate timestamp,
    published number(5,0) not null,
    primary key (taskId)
);

CREATE INDEX IDX_PAsPot_TaskEntity ON PeopleAssignments_PotOwners(task_id,entity_id);
create index IDX_PlanningTask_assignedUser on PlanningTask(assignedUser);