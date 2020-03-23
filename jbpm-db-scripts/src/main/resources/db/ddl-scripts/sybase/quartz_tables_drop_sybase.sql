use [enter_db_name_here]
go

if exists (select 1 from sysobjects where name = 'QRTZ_FIRED_TRIGGERS' and type = 'U')
    begin
        alter table QRTZ_TRIGGERS
        drop constraint FK_triggers_job_details
    end
go

if exists (select 1 from sysobjects where name = 'QRTZ_CRON_TRIGGERS' and type = 'U')
    begin
        alter table QRTZ_CRON_TRIGGERS
        drop constraint FK_cron_triggers_triggers
    end
go

if exists(select 1 from sysobjects where name = 'QRTZ_SIMPLE_TRIGGERS' and type = 'U')
    begin
        alter table QRTZ_SIMPLE_TRIGGERS
        drop constraint FK_simple_triggers_triggers
    end
go

if exists(select 1 from sysobjects where name = 'QRTZ_SIMPROP_TRIGGERS' and type = 'U')
    begin
        alter table QRTZ_SIMPROP_TRIGGERS
        drop constraint FK_simprop_triggers_triggers
    end
go

if exists(select 1 from sysobjects where name = 'QRTZ_BLOB_TRIGGERS' and type = 'U')
    begin
        alter table QRTZ_BLOB_TRIGGERS
        drop constraint FK_blob_triggers_triggers
    end
go

/*==============================================================================*/
/* Drop tables: */
/*==============================================================================*/

if exists(select 1 from sysobjects where name = 'QRTZ_FIRED_TRIGGERS' and type = 'U')
    begin
        drop table QRTZ_TRIGGER_LISTENERS
    end
go

if exists(select 1 from sysobjects where name = 'QRTZ_FIRED_TRIGGERS' and type = 'U')
    begin
        drop table QRTZ_JOB_LISTENERS
    end
go

if exists(select 1 from sysobjects where name = 'QRTZ_FIRED_TRIGGERS' and type = 'U')
    begin
        drop table QRTZ_FIRED_TRIGGERS
    end
go

if exists(select 1 from sysobjects where name = 'QRTZ_PAUSED_TRIGGER_GRPS' and type = 'U')
    begin
        drop table QRTZ_PAUSED_TRIGGER_GRPS
    end
go

if exists(select 1 from sysobjects where name = 'QRTZ_SCHEDULER_STATE' and type = 'U')
    begin
        drop table QRTZ_SCHEDULER_STATE
    end
go

if exists(select 1 from sysobjects where name = 'QRTZ_LOCKS' and type = 'U')
    begin
        drop table QRTZ_LOCKS
    end
go

if exists(select 1 from sysobjects where name = 'QRTZ_SIMPLE_TRIGGERS' and type = 'U')
    begin
        drop table QRTZ_SIMPLE_TRIGGERS
    end
go

if exists(select 1 from sysobjects where name = 'QRTZ_SIMPROP_TRIGGERS' and type = 'U')
    begin
        drop table QRTZ_SIMPROP_TRIGGERS
    end
go

if exists(select 1 from sysobjects where name = 'QRTZ_CRON_TRIGGERS' and type = 'U')
    begin
        drop table QRTZ_CRON_TRIGGERS
    end
go

if exists(select 1 from sysobjects where name = 'QRTZ_BLOB_TRIGGERS' and type = 'U')
    begin
        drop table QRTZ_BLOB_TRIGGERS
    end
go

if exists(select 1 from sysobjects where name = 'QRTZ_TRIGGERS' and type = 'U')
    begin
        drop table QRTZ_TRIGGERS
    end
go

if exists(select 1 from sysobjects where name = 'QRTZ_JOB_DETAILS' and type = 'U')
    begin
        drop table QRTZ_JOB_DETAILS
    end
go

if exists(select 1 from sysobjects where name = 'QRTZ_CALENDARS' and type = 'U')
    begin
        drop table QRTZ_CALENDARS
    end
go