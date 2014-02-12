#!/bin/sh
#fetch the sources from serotoninsoftware unpack and install in local dir
wget -c http://mango.serotoninsoftware.com/downloads/mango-1.12.4.zip
unzip mango-1.12.4.zip  WEB-INF/lib/seroUtils.jar -d /tmp/
mv /tmp/WEB-INF/lib/seroUtils.jar lib/br/org/scadabr/legacy/seroUtils-mango/1.12.4/seroUtils-mango-1.12.4.jar
rm -r /tmp/WEB-INF

#make sure there are get installed in ~/.m2/
mvn install
