    create table PlanningTask (
        taskId number(19,0) not null,
        OPTLOCK number(10,0),
        assignedUser varchar2(255 char),
        taskIndex number(10,0) not null,
        lastModificationDate timestamp,
        published number(5,0) not null,
        primary key (taskId)
    );

    create index IDX_PlanningTask_assignedUser on PlanningTask(assignedUser);