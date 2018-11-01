--**************************************************************************************************
-- Create tables/indexes for Rail cache job definitions and individual jobs.
--**************************************************************************************************

drop table CacheJobsDefnRail cascade constraints purge;

create table CacheJobsDefnRail (
	SuppID				VARCHAR2(20) 	not null, 
	CredName			VARCHAR2(50) 	not null, 
	Origin				VARCHAR2(20) 	not null, 
	Destination			VARCHAR2(20) 	not null, 
	BookDaysRange		INTEGER		not null,
	constraint CacheJobsDefnRail_PK primary key ("SUPPID", "CREDNAME", "ORIGIN", "DESTINATION")
);

drop table CacheJobsRail cascade constraints purge;

create table CacheJobsRail (
	SuppID			VARCHAR2(20) 	not null, 
	CredName		VARCHAR2(50) 	not null, 
	Origin			VARCHAR2(20) 	not null, 
	Destination		VARCHAR2(20) 	not null, 
	JobSeq			INTEGER			not null,
	JourneyDate		DATE			not null,
	constraint CacheJobsRail_PK primary key("SUPPID", "CREDNAME", "ORIGIN", "DESTINATION", "JOBSEQ")
);

create unique index CacheJobsRail_JobSeq ON CacheJobsRail ("JOBSEQ");


--**************************************************************************************************
-- Stored procedure to generate Rail jobs from job definitions.
--**************************************************************************************************
create or replace procedure GenerateCacheJobsRail is
begin
	
	delete from CacheJobsRail;

	for jobRail in (select SuppID, CredName, Origin, Destination, BookDaysRange, (rownum * 1000000) JobDefSeq from CacheJobsDefnRail order by SuppID, CredName)
	loop
	
		for dataRg1 in (select (jobRail.JobDefSeq + (rownum * 10000)) JobDefSeq, JrnyDt from
								( select (sysdate + rownum) JrnyDt from dual connect by level <= jobRail.BookDaysRange ) )
		loop

			insert into CacheJobsRail (SuppID, CredName, Origin, Destination, JobSeq, JourneyDate)
				values (jobRail.SuppID, jobRail.CredName, jobRail.Origin, jobRail.Destination, dataRg1.JobDefSeq, dataRg1.JrnyDt);

		end loop;
	end loop;
end;
/
