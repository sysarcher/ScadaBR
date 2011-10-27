--
--    Mango - Open Source M2M - http://mango.serotoninsoftware.com
--    Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
--    @author Matthew Lohbihler
--    
--    This program is free software: you can redistribute it and/or modify
--    it under the terms of the GNU General Public License as published by
--    the Free Software Foundation, either version 3 of the License, or
--    (at your option) any later version.
--
--    This program is distributed in the hope that it will be useful,
--    but WITHOUT ANY WARRANTY; without even the implied warranty of
--    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--    GNU General Public License for more details.
--
--    You should have received a copy of the GNU General Public License
--    along with this program.  If not, see <http://www.gnu.org/licenses/>.
--
--

-- Make sure that everything get created with utf8 as the charset.
alter database default character set utf8;

-- drop all existing tables ... copy from dropAllTables

drop table if exists maintenanceEvents;
drop table if exists pointLinks;
drop table if exists publishers;
drop table if exists reportInstanceUserComments;
drop table if exists reportInstanceEvents;
drop table if exists reportInstanceDataAnnotations;
drop table if exists reportInstanceData;
drop table if exists reportInstancePoints;
drop table if exists reportInstances;
drop table if exists reports;
drop table if exists compoundEventDetectors;
drop table if exists pointHierarchy;
drop table if exists scheduledEvents;
drop table if exists eventHandlers;
drop table if exists userEvents;
drop table if exists events;
drop table if exists pointEventDetectors;
drop table if exists watchListUsers;
drop table if exists watchListPoints;
drop table if exists watchLists;
drop table if exists pointValueAnnotations;
drop table if exists pointValues;
drop table if exists mangoViewUsers;
drop table if exists mangoViews;
drop table if exists dataPointUsers;
drop table if exists dataPoints;
drop table if exists flexProjects;
drop table if exists scripts;
drop table if exists dataSourceUsers;
drop table if exists dataSources;
drop table if exists mailingListMembers;
drop table if exists mailingListInactive;
drop table if exists mailingLists;
drop table if exists userComments;
drop table if exists users;
drop table if exists systemSettings;

--
-- System settings
create table systemSettings (
  settingName varchar(32) not null,
  settingValue longtext,
  primary key (settingName)
) type=InnoDB;


--
-- Users
create table users (
  id int not null auto_increment,
  mangoUsername varchar(40) not null,
  mangoUserPassword varchar(30) not null,
  email varchar(255) not null,
  phone varchar(40),
  mangoAdmin boolean not null,
  disabled boolean not null,
  lastLogin bigint,
  selectedWatchList int,
  homeUrl varchar(255),
  receiveAlarmEmails varchar(16) not null,
  receiveOwnAuditEvents boolean not null,
  primary key (id)
) type=InnoDB;
alter table users add constraint usersUni unique (mangoUsername);
create index userNameIdx on users (mangoUsername);

create table userComments (
  userId int,
  commentType int not null,
  typeKey int not null,
  ts bigint not null,
  commentText varchar(1024) not null
) type=InnoDB;
alter table userComments add constraint userCommentsFk1 foreign key (userId) references users(id);


--
-- Mailing lists
create table mailingLists (
  id int not null auto_increment,
  xid varchar(50) not null,
  name varchar(40) not null,
  primary key (id)
) type=InnoDB;
alter table mailingLists add constraint mailingListsUn1 unique (xid);

create table mailingListInactive (
  mailingListId int not null,
  inactiveInterval int not null
) type=InnoDB;
alter table mailingListInactive add constraint mailingListInactiveFk1 foreign key (mailingListId) 
  references mailingLists(id) on delete cascade;

create table mailingListMembers (
  mailingListId int not null,
  typeId int not null,
  userId int,
  address varchar(255)
) type=InnoDB;
alter table mailingListMembers add constraint mailingListMembersFk1 foreign key (mailingListId) 
  references mailingLists(id) on delete cascade;




--
--
-- Data Sources
--
create table dataSources (
  id int not null auto_increment,
  xid varchar(50) not null,
  dataSourceName varchar(40) not null,
  dataSourceType varchar(48) not null,
  jsonSerialized longtext not null,
  primary key (id)
) type=InnoDB;
alter table dataSources add constraint dataSourcesUn1 unique (xid);





-- Data source permissions
create table dataSourceUsers (
  dataSourceId int not null,
  userId int not null
) type=InnoDB;
alter table dataSourceUsers add constraint dataSourceUsersFk1 foreign key (dataSourceId) references dataSources(id) on delete cascade;
alter table dataSourceUsers add constraint dataSourceUsersFk2 foreign key (userId) references users(id) on delete cascade;

--
--
-- Scripts
--
create table scripts (
  id int not null auto_increment,
  userId int not null,
  xid varchar(50) not null,
  name varchar(40) not null,
  script varchar(16384) not null,
  data longblob not null,
  primary key (id)
) type=InnoDB;
alter table scripts add constraint scriptsUn1 unique (xid);
alter table scripts add constraint scriptsFk1 foreign key (userId) references users(id);

--
--
-- FlexProjects
--
create table flexProjects (
  id int not null auto_increment,
  name varchar(40) not null,
  description varchar(1024),
  xmlConfig varchar(16384) not null,
  primary key (id)
) type=InnoDB;
--
--
-- Data Points
--
create table dataPoints (
  id int not null auto_increment,
  xid varchar(50) not null,
  dataSourceId int not null,
  data longblob not null,
  primary key (id)
) type=InnoDB;
alter table dataPoints add constraint dataPointsUn1 unique (xid);
alter table dataPoints add constraint dataPointsFk1 foreign key (dataSourceId) references dataSources(id);


-- Data point permissions
create table dataPointUsers (
  dataPointId int not null,
  userId int not null,
  dataPointUserPermission int not null
) type=InnoDB;
alter table dataPointUsers add constraint dataPointUsersFk1 foreign key (dataPointId) references dataPoints(id);
alter table dataPointUsers add constraint dataPointUsersFk2 foreign key (userId) references users(id) on delete cascade;


create table dataPointComments (
  userId int,
  dataPointId int not null,
  ts bigint not null,
  commentText varchar(1024) not null
) type=InnoDB;
alter table dataPointComments add constraint dataPointCommentsFk1 foreign key (userId) references users(id);
alter table dataPointComments add constraint dataPointCommentsFk2 foreign key (dataPointId) references dataPoints(id) on delete cascade;

--
--
-- Views
--
create table mangoViews (
  id int not null auto_increment,
  xid varchar(50) not null,
  name varchar(100) not null,
  background varchar(255),
  userId int not null,
  anonymousAccess int not null,
  data longblob not null,
  primary key (id)
) type=InnoDB;
alter table mangoViews add constraint mangoViewsUn1 unique (xid);
alter table mangoViews add constraint mangoViewsFk1 foreign key (userId) references users(id) on delete cascade;

create table mangoViewUsers (
  mangoViewId int not null,
  userId int not null,
  accessType int not null,
  primary key (mangoViewId, userId)
) type=InnoDB;
alter table mangoViewUsers add constraint mangoViewUsersFk1 foreign key (mangoViewId) references mangoViews(id) on delete cascade;
alter table mangoViewUsers add constraint mangoViewUsersFk2 foreign key (userId) references users(id) on delete cascade;


--
--
-- Point Values (historical data)
--
create table pointValues (
  id bigint not null auto_increment,
  dataPointId int not null,
  dataType int not null,
  pointValue double,
  ts bigint not null,
  primary key (id)
) type=InnoDB;
alter table pointValues add constraint pointValuesFk1 foreign key (dataPointId) references dataPoints(id) on delete cascade;
create index pointValuesIdx1 on pointValues (ts, dataPointId);
create index pointValuesIdx2 on pointValues (dataPointId, ts);

create table pointValueAnnotations (
  pointValueId bigint not null,
  textPointValueShort varchar(128),
  textPointValueLong longtext,
  sourceType smallint,
  sourceId int
) type=InnoDB;
alter table pointValueAnnotations add constraint pointValueAnnotationsFk1 foreign key (pointValueId) 
  references pointValues(id) on delete cascade;


--
--
-- Watch list
--
create table watchLists (
  id int not null auto_increment,
  xid varchar(50) not null,
  userId int not null,
  name varchar(50),
  primary key (id)
) type=InnoDB;
alter table watchLists add constraint watchListsUn1 unique (xid);
alter table watchLists add constraint watchListsFk1 foreign key (userId) references users(id) on delete cascade;

create table watchListPoints (
  watchListId int not null,
  dataPointId int not null,
  sortOrder int not null
) type=InnoDB;
alter table watchListPoints add constraint watchListPointsFk1 foreign key (watchListId) references watchLists(id) on delete cascade;
alter table watchListPoints add constraint watchListPointsFk2 foreign key (dataPointId) references dataPoints(id);

create table watchListUsers (
  watchListId int not null,
  userId int not null,
  accessType int not null,
  primary key (watchListId, userId)
) type=InnoDB;
alter table watchListUsers add constraint watchListUsersFk1 foreign key (watchListId) references watchLists(id) on delete cascade;
alter table watchListUsers add constraint watchListUsersFk2 foreign key (userId) references users(id) on delete cascade;


--
--
-- Point event detectors
--
create table pointEventDetectors (
  id int not null auto_increment,
  xid varchar(50) not null,
  alias varchar(255),
  dataPointId int not null,
  detectorType int not null,
  alarmLevel int not null,
  stateLimit double,
  duration int,
  durationType int,
  binaryState char(1),
  multistateState int,
  changeCount int,
  alphanumericState varchar(128),
  weight double,
  primary key (id)
) type=InnoDB;
alter table pointEventDetectors add constraint pointEventDetectorsUn1 unique (xid, dataPointId);
alter table pointEventDetectors add constraint pointEventDetectorsFk1 foreign key (dataPointId) 
  references dataPoints(id);


--
--
-- Events
--
create table events (
  id int not null auto_increment,
  typeId int not null,
  typeRef1 int not null,
  typeRef2 int not null,
  activeTs bigint not null,
  rtnApplicable char(1) not null,
  rtnTs bigint,
  rtnCause int,
  alarmLevel int not null,
  message longtext,
  ackTs bigint,
  ackUserId int,
  alternateAckSource int,
  primary key (id)
) type=InnoDB;
alter table events add constraint eventsFk1 foreign key (ackUserId) references users(id);

create table userEvents (
  eventId int not null,
  userId int not null,
  silenced char(1) not null,
  primary key (eventId, userId)
) type=InnoDB;
alter table userEvents add constraint userEventsFk1 foreign key (eventId) references events(id) on delete cascade;
alter table userEvents add constraint userEventsFk2 foreign key (userId) references users(id) on delete cascade;


--
--
-- Event handlers
--
create table eventHandlers (
  id int not null auto_increment,
  xid varchar(50) not null,
  alias varchar(255),
  eventTypeId int not null,
  eventTypeRef1 int not null,
  eventTypeRef2 int not null,
  data longblob not null,
  primary key (id)
) type=InnoDB;
alter table eventHandlers add constraint eventHandlersUn1 unique (xid);


--
--
-- Scheduled events
--
create table scheduledEvents (
  id int not null auto_increment,
  xid varchar(50) not null,
  alias varchar(255),
  alarmLevel int not null,
  scheduleType int not null,
  returnToNormal char(1) not null,
  disabled char(1) not null,
  activeYear int,
  activeMonth int,
  activeDay int,
  activeHour int,
  activeMinute int,
  activeSecond int,
  activeCron varchar(25),
  inactiveYear int,
  inactiveMonth int,
  inactiveDay int,
  inactiveHour int,
  inactiveMinute int,
  inactiveSecond int,
  inactiveCron varchar(25),
  primary key (id)
) type=InnoDB;
alter table scheduledEvents add constraint scheduledEventsUn1 unique (xid);


create table eventComments (
  userId int,
  eventId int not null,
  ts bigint not null,
  commentText varchar(1024) not null
);
alter table eventComments add constraint eventCommentsFk1 foreign key (userId) references users(id);
alter table eventComments add constraint eventCommentsFk2 foreign key (eventId) references events(id) on delete cascade;


--
--
-- Point Hierarchy
--
create table pointHierarchy (
  id int not null auto_increment,
  parentId int,
  name varchar(100),
  primary key (id)
) type=InnoDB;


--
--
-- Compound events detectors
--
create table compoundEventDetectors (
  id int not null auto_increment,
  xid varchar(50) not null,
  name varchar(100),
  alarmLevel int not null,
  returnToNormal char(1) not null,
  disabled char(1) not null,
  conditionText varchar(256) not null,
  primary key (id)
) type=InnoDB;
alter table compoundEventDetectors add constraint compoundEventDetectorsUn1 unique (xid);


--
--
-- Reports
--
create table reports (
  id int not null auto_increment,
  userId int not null,
  name varchar(100) not null,
  data longblob not null,
  primary key (id)
) type=InnoDB;
alter table reports add constraint reportsFk1 foreign key (userId) references users(id) on delete cascade;

create table reportInstances (
  id int not null auto_increment,
  userId int not null,
  name varchar(100) not null,
  includeEvents int not null,
  includeUserComments char(1) not null,
  reportStartTime bigint not null,
  reportEndTime bigint not null,
  runStartTime bigint,
  runEndTime bigint,
  recordCount int,
  preventPurge char(1),
  primary key (id)
) type=InnoDB;
alter table reportInstances add constraint reportInstancesFk1 foreign key (userId) references users(id) on delete cascade;

create table reportInstancePoints (
  id int not null auto_increment,
  reportInstanceId int not null,
  dataSourceName varchar(40) not null,
  pointName varchar(100) not null,
  dataType int not null,
  startValue varchar(4096),
  textRenderer longblob,
  colour varchar(6),
  consolidatedChart char(1),
  primary key (id)
) type=InnoDB;
alter table reportInstancePoints add constraint reportInstancePointsFk1 foreign key (reportInstanceId) 
  references reportInstances(id) on delete cascade;

create table reportInstanceData (
  pointValueId bigint not null,
  reportInstancePointId int not null,
  pointValue double,
  ts bigint not null,
  primary key (pointValueId, reportInstancePointId)
) type=InnoDB;
alter table reportInstanceData add constraint reportInstanceDataFk1 foreign key (reportInstancePointId) 
  references reportInstancePoints(id) on delete cascade;

create table reportInstanceDataAnnotations (
  pointValueId bigint not null,
  reportInstancePointId int not null,
  textPointValueShort varchar(128),
  textPointValueLong longtext,
  sourceValue varchar(128),
  primary key (pointValueId, reportInstancePointId)
) type=InnoDB;
alter table reportInstanceDataAnnotations add constraint reportInstanceDataAnnotationsFk1 
  foreign key (pointValueId, reportInstancePointId) references reportInstanceData(pointValueId, reportInstancePointId) 
  on delete cascade;

create table reportInstanceEvents (
  eventId int not null,
  reportInstanceId int not null,
  typeId int not null,
  typeRef1 int not null,
  typeRef2 int not null,
  activeTs bigint not null,
  rtnApplicable char(1) not null,
  rtnTs bigint,
  rtnCause int,
  alarmLevel int not null,
  message longtext,
  ackTs bigint,
  ackUsername varchar(40),
  alternateAckSource int,
  primary key (eventId, reportInstanceId)
) type=InnoDB;
alter table reportInstanceEvents add constraint reportInstanceEventsFk1 foreign key (reportInstanceId)
  references reportInstances(id) on delete cascade;

create table reportInstanceUserComments (
  reportInstanceId int not null,
  username varchar(40),
  commentType int not null,
  typeKey int not null,
  ts bigint not null,
  commentText varchar(1024) not null
) type=InnoDB;
alter table reportInstanceUserComments add constraint reportInstanceUserCommentsFk1 foreign key (reportInstanceId)
  references reportInstances(id) on delete cascade;


--
--
-- Publishers
--
create table publishers (
  id int not null auto_increment,
  xid varchar(50) not null,
  data longblob not null,
  primary key (id)
) type=InnoDB;
alter table publishers add constraint publishersUn1 unique (xid);


--
--
-- Point links
--
create table pointLinks (
  id int not null auto_increment,
  xid varchar(50) not null,
  sourcePointId int not null,
  targetPointId int not null,
  script longtext,
  eventType int not null,
  disabled char(1) not null,
  primary key (id)
) type=InnoDB;
alter table pointLinks add constraint pointLinksUn1 unique (xid);


--
--
-- Maintenance events
--
create table maintenanceEvents (
  id int not null auto_increment,
  xid varchar(50) not null,
  dataSourceId int not null,
  alias varchar(255),
  alarmLevel int not null,
  scheduleType int not null,
  disabled char(1) not null,
  activeYear int,
  activeMonth int,
  activeDay int,
  activeHour int,
  activeMinute int,
  activeSecond int,
  activeCron varchar(25),
  inactiveYear int,
  inactiveMonth int,
  inactiveDay int,
  inactiveHour int,
  inactiveMinute int,
  inactiveSecond int,
  inactiveCron varchar(25),
  primary key (id)
) type=InnoDB;
alter table maintenanceEvents add constraint maintenanceEventsUn1 unique (xid);
alter table maintenanceEvents add constraint maintenanceEventsFk1 foreign key (dataSourceId) references dataSources(id);
