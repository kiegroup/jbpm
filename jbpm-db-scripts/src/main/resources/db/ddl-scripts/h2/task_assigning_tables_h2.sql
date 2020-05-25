    create table PlanningTask (
        taskId bigint not null,
        OPTLOCK integer,
        assignedUser varchar(255),
        taskIndex integer not null,
        lastModificationDate timestamp,
        published smallint not null,
        primary key (taskId)
    );

    alter table PlanningTask
        add constraint FK_PlanningTask_Task
        foreign key (taskId)
        references Task;

    create index IDX_PlanningTask_assignedUser on PlanningTask(assignedUser);