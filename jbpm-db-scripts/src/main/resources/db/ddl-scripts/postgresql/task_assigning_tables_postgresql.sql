    create table PlanningTask (
        taskId int8 not null,
        OPTLOCK int4,
        assignedUser varchar(255),
        taskIndex int4 not null,
        lastModificationDate timestamp,
        published int2 not null,
        primary key (taskId)
    );

    create index IDX_PlanningTask_assignedUser on PlanningTask(assignedUser);