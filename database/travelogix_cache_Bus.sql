--**************************************************************************************************
-- Create tables/indexes for Bus cache job definitions and individual jobs.
--**************************************************************************************************

drop table CacheJobsDefnBus cascade constraints purge;

create table CacheJobsDefnBus (
	SuppID				VARCHAR2(20) 	not null, 
	CredName			VARCHAR2(50) 	not null, 
	Origin				VARCHAR2(20) 	not null, 
	Destination			VARCHAR2(20) 	not null, 
	BookDaysRange		INTEGER		not null,
	constraint CacheJobsDefnBus_PK primary key ("SUPPID", "CREDNAME", "ORIGIN", "DESTINATION")
);

drop table CacheJobsBus cascade constraints purge;

create table CacheJobsBus (
	SuppID			VARCHAR2(20) 	not null, 
	CredName		VARCHAR2(50) 	not null, 
	Origin			VARCHAR2(20) 	not null, 
	Destination		VARCHAR2(20) 	not null, 
	JobSeq			INTEGER			not null,
	JourneyDate		DATE			not null,
	constraint CacheJobsBus_PK primary key("SUPPID", "CREDNAME", "ORIGIN", "DESTINATION", "JOBSEQ")
);

create unique index CacheJobsBus_JobSeq ON CacheJobsBus ("JOBSEQ");


--**************************************************************************************************
-- Stored procedure to generate Bus jobs from job definitions.
--**************************************************************************************************
create or replace procedure GenerateCacheJobsBus is
begin
	
	delete from CacheJobsBus;

	for jobBus in (select SuppID, CredName, Origin, Destination, BookDaysRange, (rownum * 1000000) JobDefSeq from CacheJobsDefnBus order by SuppID, CredName)
	loop
	
		for dataRg1 in (select (jobBus.JobDefSeq + (rownum * 10000)) JobDefSeq, JrnyDt from
								( select (sysdate + rownum) JrnyDt from dual connect by level <= jobBus.BookDaysRange ) )
		loop

			insert into CacheJobsBus (SuppID, CredName, Origin, Destination, JobSeq, JourneyDate)
				values (jobBus.SuppID, jobBus.CredName, jobBus.Origin, jobBus.Destination, dataRg1.JobDefSeq, dataRg1.JrnyDt);

		end loop;
	end loop;
end;
/
