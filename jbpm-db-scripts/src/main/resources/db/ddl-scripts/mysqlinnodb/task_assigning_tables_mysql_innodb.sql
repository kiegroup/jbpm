    create table PlanningTask (
        taskId bigint not null,
        OPTLOCK integer,
        assignedUser varchar(255),
        taskIndex integer not null,
        lastModificationDate datetime(6),
        published smallint not null,
        primary key (taskId)
    ) ENGINE=InnoDB;

    create index IDX_PlanningTask_assignedUser on PlanningTask(assignedUser);