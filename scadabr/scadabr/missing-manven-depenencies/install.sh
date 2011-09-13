#!/bin/sh

mvn install:install-file -DgroupId=org.directwebremoting -DartifactId=dwr -Dversion=2.0.7 -Dpackaging=jar -Dfile=dwr-2.0.7.jar


mvn install:install-file -DgroupId=com.dalsemi -DartifactId=onewire -Dversion=1.10 -Dpackaging=jar -Dfile=onewire-1.10.jar

mvn install:install-file -DgroupId=org.rxtx -DartifactId=rxtx -Dversion=2.2 -Dpackaging=jar -Dfile=RXTX-2.2pre2.jar

mvn install:install-file -DgroupId=com.serotonin.mango -DartifactId=seroUtils-mango -Dversion=1.12.4 -Dpackaging=jar -Dfile=seroUtils-mango-1.12.4.jar
mvn install:install-file -DgroupId=com.serotonin.mango -DartifactId=spinwave-mango -Dversion=1.12.4 -Dpackaging=jar -Dfile=spinwave-mango-1.12.4.jar
mvn install:install-file -DgroupId=com.serotonin.mango -DartifactId=modbus4J-mango -Dversion=1.12.4 -Dpackaging=jar -Dfile=modbus4J-mango-1.12.4.jar
mvn install:install-file -DgroupId=com.serotonin.mango -DartifactId=bacnet4J-mango -Dversion=1.12.4 -Dpackaging=jar -Dfile=bacnet4J-mango-1.12.4.jar
mvn install:install-file -DgroupId=com.serotonin.mango -DartifactId=viconics-mango -Dversion=1.12.4 -Dpackaging=jar -Dfile=viconics-mango-1.12.4.jar
mvn install:install-file -DgroupId=com.serotonin.mango -DartifactId=viconics-ScadaBR -Dversion=20100917 -Dpackaging=jar -Dfile=viconics-ScadaBR-20100917.jar


mvn install:install-file -DgroupId=com.serotonin.mango -DartifactId=seroUtils-mango -Dversion=1.12.5 -Dpackaging=jar -Dfile=seroUtils-mango-1.12.5.jar
mvn install:install-file -DgroupId=com.serotonin.mango -DartifactId=spinwave-mango -Dversion=1.12.5 -Dpackaging=jar -Dfile=spinwave-mango-1.12.5.jar
mvn install:install-file -DgroupId=com.serotonin.mango -DartifactId=modbus4J-mango -Dversion=1.12.5 -Dpackaging=jar -Dfile=modbus4J-mango-1.12.5.jar
mvn install:install-file -DgroupId=com.serotonin.mango -DartifactId=bacnet4J-mango -Dversion=1.12.5 -Dpackaging=jar -Dfile=bacnet4J-mango-1.12.5.jar
mvn install:install-file -DgroupId=com.serotonin.mango -DartifactId=viconics-mango -Dversion=1.12.5 -Dpackaging=jar -Dfile=viconics-mango-1.12.5.jar



mvn install:install-file -DgroupId=net.sf.openv4j -DartifactId=openv4j-core -Dversion=0.1.4-SNAPSHOT -Dpackaging=jar -Dfile=openv4j-core-0.1.4-SNAPSHOT.jar



mvn install:install-file -DgroupId=com.i2msolucoes -DartifactId=alpha24j -Dversion=version-unknown -Dpackaging=jar -Dfile=alpha24j.jar

mvn install:install-file -DgroupId=br.org.scadabr -DartifactId=dnp34j -Dversion=version-unknown -Dpackaging=jar -Dfile=dnp34j.jar
mvn install:install-file -DgroupId=br.org.scadabr.protocol -DartifactId=iec101 -Dversion=version-unknown -Dpackaging=jar -Dfile=iec101.jar

mvn install:install-file -DgroupId=org.openscada -DartifactId=openscada-utils -Dversion=version-unknown -Dpackaging=jar -Dfile=openscada-utils.jar
mvn install:install-file -DgroupId=org.openscada -DartifactId=opc-dcom -Dversion=0.5 -Dpackaging=jar -Dfile=org.openscada.opc.dcom-0.5.0.jar
mvn install:install-file -DgroupId=org.openscada -DartifactId=opc-lib -Dversion=0.5 -Dpackaging=jar -Dfile=org.openscada.opc.lib-0.5.0.jar
mvn install:install-file -DgroupId=org.openscada -DartifactId=opc-driver -Dversion=version-unknown -Dpackaging=jar -Dfile=opc-driver.jar

