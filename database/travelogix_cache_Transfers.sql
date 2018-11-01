--**************************************************************************************************
-- Create tables/indexes for Transfers cache job definitions and individual jobs.
--**************************************************************************************************

drop table CacheJobsDefnTransfers cascade constraints purge;

create table CacheJobsDefnTransfers (
	SuppID				VARCHAR2(20) 	not null, 
	CredName			VARCHAR2(50) 	not null, 
	PickLocType			VARCHAR2(20) 	not null, 
	PickLocCode			VARCHAR2(20), 
	PickLong			VARCHAR2(10),
	PickLat				VARCHAR2(10),
	PickDateTime		DATE			not null,
	DropLocType			VARCHAR2(20)	not null,
	DropLocCode			VARCHAR2(20),
	VehicleType			VARCHAR2(20),
	AdultPsgrsRange		INTEGER		not null,
	ChildPsgrsRange		INTEGER		not null,
	InfantPsgrsRange	INTEGER		not null,
	constraint CacheJobsDefnTransfers_PK primary key ("SUPPID", "CREDNAME", "PICKLOCTYPE", "PICKLOCCODE", "PICKLONG", "PICKLAT")
);

drop table CacheJobsTransfers cascade constraints purge;

create table CacheJobsTransfers (
	SuppID			VARCHAR2(20) 	not null, 
	CredName		VARCHAR2(50) 	not null, 
	PickLocType		VARCHAR2(20) 	not null, 
	PickLocCode		VARCHAR2(20), 
	PickLong		VARCHAR2(10),
	PickLat			VARCHAR2(10),
	JobSeq			INTEGER			not null,
	PickDateTime	DATE			not null,
	DropLocType		VARCHAR2(20)	not null,
	DropLocCode		VARCHAR2(20),
	VehicleType		VARCHAR2(20),
	AdultPsgrs		INTEGER		not null,
	ChildPsgrs		INTEGER		not null,
	InfantPsgrs		INTEGER		not null,
	constraint CacheJobsTransfers_PK primary key("SUPPID", "CREDNAME", "PICKLOCTYPE", "PICKLOCCODE", "PICKLONG", "PICKLAT", "JOBSEQ")
);

create unique index CacheJobsTransfers_JobSeq ON CacheJobsTransfers ("JOBSEQ");


--**************************************************************************************************
-- Stored procedure to generate Transfers jobs from job definitions.
--**************************************************************************************************
create or replace procedure GenerateCacheJobsTransfers is
begin
	
	delete from CacheJobsTransfers;

	for jobTransfers in (select SuppID, CredName, PickLocType, PickLocCode, PickLong, PickLat, PickDateTime, DropLocType, DropLocCode, VehicleType, AdultPsgrsRange, ChildPsgrsRange, InfantPsgrsRange, (rownum * 1000000) JobDefSeq from CacheJobsDefnTransfers order by SuppID, CredName)
	loop
	
		for dataRg1 in (select (jobTransfers.JobDefSeq + (rownum * 10000)) JobDefSeq, AdultPassNo from
								( select rownum AdultPassNo from dual connect by level <= jobTransfers.AdultPsgrsRange ) )
		loop

			for dataRg2 in (select (dataRg1.JobDefSeq + (rownum * 100)) JobDefSeq, (level - 1) ChildPassNo from dual connect by level <= (jobTransfers.ChildPsgrsRange + 1))
			loop

				for dataRg3 in (select (dataRg2.JobDefSeq + rownum) JobDefSeq, (level - 1) InfantPassNo from dual connect by level <= (jobTransfers.InfantPsgrsRange + 1))
				loop
					insert into CacheJobsTransfers (SuppID, CredName, PickLocType, PickLocCode, PickLong, PickLat, JobSeq, PickDateTime, DropLocType, DropLocCode, VehicleType, AdultPsgrs, ChildPsgrs, InfantPsgrs)
						values (jobTransfers.SuppID, jobTransfers.CredName, jobTransfers.PickLocType, jobTransfers.PickLocCode, jobTransfers.PickLong, jobTransfers.PickLat, dataRg3.JobDefSeq, jobTransfers.PickDateTime, jobTransfers.DropLocType, jobTransfers.DropLocCode, jobTransfers.VehicleType, dataRg1.AdultPassNo, dataRg2.ChildPassNo, dataRg3.InfantPassNo);
				end loop;
				
			end loop;

		end loop;
	end loop;
end;
/
