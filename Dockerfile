FROM maven:3-eclipse-temurin-8

ARG AGENCYID="stpt"
ARG AGENCYNAME="STPT"
ARG GTFS_URL="https://data.opentransport.ro/routing/gtfs/gtfs-stpt-test.zip"
ARG GTFSRTVEHICLEPOSITIONS="https://api.opentransport.ro/realtime/vehicle-positions/tm"
ARG TRANSITCLOCK_PROPERTIES="docker/config/transitclock.properties"

ENV AGENCYID ${AGENCYID}
ENV AGENCYNAME ${AGENCYNAME}
ENV GTFS_URL ${GTFS_URL}
ENV GTFSRTVEHICLEPOSITIONS ${GTFSRTVEHICLEPOSITIONS}
ENV TRANSITCLOCK_PROPERTIES ${TRANSITCLOCK_PROPERTIES}
ENV TRANSITCLOCK_CORE /transitclock-core

RUN apt-get update && apt-get install -y postgresql-client git-core vim jq

ENV CATALINA_HOME /usr/local/tomcat
ENV PATH $CATALINA_HOME/bin:$PATH
RUN mkdir -p "$CATALINA_HOME"
WORKDIR $CATALINA_HOME

ENV TOMCAT_MAJOR 8
ENV TOMCAT_VERSION 8.0.43
ENV TOMCAT_TGZ_URL https://archive.apache.org/dist/tomcat/tomcat-$TOMCAT_MAJOR/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz

RUN set -x \
	&& curl -fSL "$TOMCAT_TGZ_URL" -o tomcat.tar.gz \
	&& tar -xvf tomcat.tar.gz --strip-components=1 \
	&& rm bin/*.bat \
	&& rm tomcat.tar.gz*

EXPOSE 8080

WORKDIR /
RUN mkdir /usr/local/transitclock && mkdir /usr/local/transitclock/db && mkdir /usr/local/transitclock/config &&  \
    mkdir /usr/local/transitclock/logs && mkdir /usr/local/transitclock/cache && mkdir /usr/local/transitclock/data &&  \
    mkdir /usr/local/transitclock/test && mkdir /usr/local/transitclock/test/config

WORKDIR /usr/local/transitclock

ADD transitclockWebapp/target/web.war /usr/local/transitclock/
ADD transitclockApi/target/api.war /usr/local/transitclock/
ADD transitclock/target/Core.jar /usr/local/transitclock/

# Deploy API which talks to core using RMI calls.
RUN mv api.war  /usr/local/tomcat/webapps

# Deploy webapp which is a UI based on the API.
RUN mv web.war  /usr/local/tomcat/webapps

# Scripts required to start transiTime.
ADD docker/bin/check_db_up.sh /usr/local/transitclock/bin/check_db_up.sh
ADD docker/bin/generate_sql.sh /usr/local/transitclock/bin/generate_sql.sh
ADD docker/bin/create_tables.sh /usr/local/transitclock/bin/create_tables.sh
ADD docker/bin/create_api_key.sh /usr/local/transitclock/bin/create_api_key.sh
ADD docker/bin/create_webagency.sh /usr/local/transitclock/bin/create_webagency.sh
ADD docker/bin/import_gtfs.sh /usr/local/transitclock/bin/import_gtfs.sh
ADD docker/bin/start_transitclock.sh /usr/local/transitclock/bin/start_transitclock.sh
ADD docker/bin/get_api_key.sh /usr/local/transitclock/bin/get_api_key.sh
ADD docker/bin/import_avl.sh /usr/local/transitclock/bin/import_avl.sh
ADD docker/bin/process_avl.sh /usr/local/transitclock/bin/process_avl.sh
ADD docker/bin/update_traveltimes.sh /usr/local/transitclock/bin/update_traveltimes.sh
ADD docker/bin/set_config.sh /usr/local/transitclock/bin/set_config.sh

# Handy utility to allow you connect directly to database
ADD docker/bin/connect_to_db.sh /usr/local/transitclock/bin/connect_to_db.sh

ENV PATH="/usr/local/transitclock/bin:${PATH}"

# This is a way to copy in test data to run a regression test.
# ADD docker/data/avl.csv /usr/local/transitclock/data/avl.csv
# ADD docker/data/gtfs_hart_old.zip /usr/local/transitclock/data/gtfs_hart_old.zip

RUN \
	sed -i 's/\r//' /usr/local/transitclock/bin/*.sh &&\
 	chmod 777 /usr/local/transitclock/bin/*.sh

ADD docker/config/postgres_hibernate.cfg.xml /usr/local/transitclock/config/hibernate.cfg.xml
ADD ${TRANSITCLOCK_PROPERTIES} /usr/local/transitclock/config/transitclock.properties

# This adds the transitime configs to test.
ADD docker/config/test/* /usr/local/transitclock/config/test/

EXPOSE 8080

CMD ["/start_transitclock.sh"]