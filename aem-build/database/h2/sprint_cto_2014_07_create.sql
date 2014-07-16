CREATE TABLE app (ID INTEGER NOT NULL IDENTITY, createBy VARCHAR(255), createDate TIMESTAMP, lastUpdateDate TIMESTAMP, name VARCHAR(255) NOT NULL, updateBy VARCHAR(255), documentRoot VARCHAR(255), warPath VARCHAR(255), webAppContext VARCHAR(255) NOT NULL, GROUP_ID BIGINT, CONSTRAINT U_APP_NAME UNIQUE (name));
CREATE TABLE current_jvm_state (id BIGINT NOT NULL, AS_OF TIMESTAMP NOT NULL, STATE VARCHAR(255) NOT NULL, PRIMARY KEY (id));
CREATE TABLE group_control_history (id INTEGER NOT NULL IDENTITY, completedDate TIMESTAMP, controlOperation VARCHAR(255), groupId BIGINT, requestedBy VARCHAR(255), requestedDate TIMESTAMP);
CREATE TABLE grp (ID INTEGER NOT NULL IDENTITY, createBy VARCHAR(255), createDate TIMESTAMP, lastUpdateDate TIMESTAMP, name VARCHAR(255) NOT NULL, updateBy VARCHAR(255), state VARCHAR(255), stateUpdated TIMESTAMP, CONSTRAINT U_GRP_NAME UNIQUE (name));
CREATE TABLE GRP_JVM (GROUP_ID BIGINT, JVM_ID BIGINT, CONSTRAINT U_GRP_JVM_GROUP_ID UNIQUE (GROUP_ID, JVM_ID));
CREATE TABLE jvm (id INTEGER NOT NULL IDENTITY, createBy VARCHAR(255), createDate TIMESTAMP, lastUpdateDate TIMESTAMP, name VARCHAR(255) NOT NULL, updateBy VARCHAR(255), ajpPort INTEGER NOT NULL, hostName VARCHAR(255), httpPort INTEGER NOT NULL, httpsPort INTEGER, redirectPort INTEGER NOT NULL, shutdownPort INTEGER NOT NULL, CONSTRAINT U_JVM_NAME UNIQUE (name));
CREATE TABLE jvm_control_history (id INTEGER NOT NULL IDENTITY, completedDate TIMESTAMP, controlOperation VARCHAR(255), jvmId BIGINT, requestedBy VARCHAR(255), requestedDate TIMESTAMP, returnCode INTEGER, returnErrorOutput VARCHAR(2048), returnOutput VARCHAR(2048));
CREATE TABLE webserver (id INTEGER NOT NULL IDENTITY, createBy VARCHAR(255), createDate TIMESTAMP, lastUpdateDate TIMESTAMP, name VARCHAR(255) NOT NULL, updateBy VARCHAR(255), host VARCHAR(255), httpsPort INTEGER, port INTEGER, CONSTRAINT U_WBSRVER_NAME UNIQUE (name));
CREATE TABLE webserver_control_history (id INTEGER NOT NULL IDENTITY, completedDate TIMESTAMP, controlOperation VARCHAR(255), requestedBy VARCHAR(255), requestedDate TIMESTAMP, returnCode INTEGER, returnErrorOutput VARCHAR(2048), returnOutput VARCHAR(2048), webServerId BIGINT);
CREATE TABLE WEBSERVER_GRP (WEBSERVER_ID BIGINT, GROUP_ID BIGINT, CONSTRAINT U_WBSRGRP_WEBSERVER_ID UNIQUE (WEBSERVER_ID, GROUP_ID));
ALTER TABLE app ADD FOREIGN KEY (GROUP_ID) REFERENCES grp (ID);
ALTER TABLE GRP_JVM ADD FOREIGN KEY (GROUP_ID) REFERENCES grp (ID);
ALTER TABLE GRP_JVM ADD FOREIGN KEY (JVM_ID) REFERENCES jvm (id);
ALTER TABLE WEBSERVER_GRP ADD FOREIGN KEY (WEBSERVER_ID) REFERENCES webserver (id);
ALTER TABLE WEBSERVER_GRP ADD FOREIGN KEY (GROUP_ID) REFERENCES grp (ID);
