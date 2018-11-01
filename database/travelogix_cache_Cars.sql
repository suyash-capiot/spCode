--**************************************************************************************************
-- Create tables/indexes for Cars cache job definitions and individual jobs.
--**************************************************************************************************

drop table CacheJobsDefnCars cascade constraints purge;

create table CacheJobsDefnCars (
	SuppID				VARCHAR2(20) 	not null, 
	CredName			VARCHAR2(50) 	not null, 
	PickLoc				VARCHAR2(20) 	not null, 
	DropLoc				VARCHAR2(20) 	not null, 
	BookDaysRange		INTEGER		not null,
	RentDurRange		INTEGER		not null,
	PackageType			VARCHAR(20),
	PackageSubType		VARCHAR(20),
	OnewayIndicator		VARCHAR(1),
	constraint CacheJobsDefnCars_PK primary key ("SUPPID", "CREDNAME", "PICKLOC", "DROPLOC")
);

drop table CacheJobsCars cascade constraints purge;

create table CacheJobsCars (
	SuppID				VARCHAR2(20) 	not null, 
	CredName			VARCHAR2(50) 	not null, 
	PickLoc				VARCHAR2(20) 	not null, 
	DropLoc				VARCHAR2(20) 	not null, 
	JobSeq				INTEGER			not null,
	PickDate			DATE			not null,
	ReturnDate			DATE					 ,
	PackageType			VARCHAR(20),
	PackageSubType		VARCHAR(20),
	OnewayIndicator		VARCHAR(1),
	constraint CacheJobsCars_PK primary key("SUPPID", "CREDNAME", "PICKLOC", "DROPLOC", "JOBSEQ")
);

create unique index CacheJobsCars_JobSeq ON CacheJobsCars ("JOBSEQ");


--**************************************************************************************************
-- Stored procedure to generate Cars jobs from job definitions.
--**************************************************************************************************
create or replace procedure GenerateCacheJobsCars is
begin
	
	delete from CacheJobsCars;

	for jobCars in (select SuppID, CredName, PickLoc, DropLoc, BookDaysRange, RentDurRange, PackageType, PackageSubType, OnewayIndicator, (rownum * 1000000) JobDefSeq from CacheJobsDefnCars order by SuppID, CredName)
	loop
	
		for dataRg1 in (select (jobCars.JobDefSeq + (rownum * 10000)) JobDefSeq, PickDt, (PickDt + durDays) RetDt from
								( select (sysdate + rownum) PickDt from dual connect by level <= jobCars.BookDaysRange ),
								( select rownum durDays from dual connect by level < = jobCars.RentDurRange ) )
		loop

			insert into CacheJobsCars (SuppID, CredName, PickLoc, DropLoc, JobSeq, PickDate, ReturnDate, PackageType, PackageSubType, OnewayIndicator)
				values (jobCars.SuppID, jobCars.CredName, jobCars.PickLoc, jobCars.DropLoc, dataRg1.JobDefSeq, dataRg1.PickDt, dataRg1.RetDt, jobCars.PackageType, jobCars.PackageSubType, jobCars.OnewayIndicator);

		end loop;
	end loop;
end;
/
