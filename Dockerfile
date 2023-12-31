FROM maven:3-eclipse-temurin-17

ARG AGENCYID="ro.stpt"
ARG AGENCYNAME="STPT"
ARG GTFS_URL="https://data.opentransport.ro/routing/gtfs/gtfs-stpt.zip"
ARG GTFSRTVEHICLEPOSITIONS="https://api.opentransport.ro/realtime/vehicle-positions/tm"
ARG TRANSITCLOCK_PROPERTIES="docker/config/transitclock.properties"

RUN apt-get update && apt-get install -y postgresql-client git-core vim jq

ENV AGENCYID ${AGENCYID}
ENV AGENCYNAME ${AGENCYNAME}
ENV GTFS_URL ${GTFS_URL}
ENV GTFSRTVEHICLEPOSITIONS ${GTFSRTVEHICLEPOSITIONS}
ENV TRANSITCLOCK_PROPERTIES ${TRANSITCLOCK_PROPERTIES}
ENV CATALINA_HOME /usr/local/tomcat
ENV PATH $CATALINA_HOME/bin:$PATH

ENV TOMCAT_MAJOR 9
ENV TOMCAT_VERSION 9.0.84
ENV TOMCAT_TGZ_URL https://dlcdn.apache.org/tomcat/tomcat-$TOMCAT_MAJOR/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz

EXPOSE 8080
RUN mkdir -p "$CATALINA_HOME"
WORKDIR $CATALINA_HOME

RUN set -x \
	&& curl -fSL "$TOMCAT_TGZ_URL" -o tomcat.tar.gz \
	&& tar -xvf tomcat.tar.gz --strip-components=1 \
	&& rm bin/*.bat \
	&& rm tomcat.tar.gz*


RUN mkdir /usr/local/transitclock &&  \
    mkdir /usr/local/transitclock/db &&  \
    mkdir /usr/local/transitclock/config &&  \
    mkdir /usr/local/transitclock/logs &&  \
    mkdir /usr/local/transitclock/cache &&  \
    mkdir /usr/local/transitclock/data &&  \
    mkdir /usr/local/transitclock/test &&  \
    mkdir /usr/local/transitclock/test/config

WORKDIR /usr/local/transitclock

COPY webapp/target/web.war /usr/local/tomcat/webapps/web.war
COPY api/target/api.war /usr/local/tomcat/webapps/api.war
COPY core/target/Core.jar /usr/local/transitclock/core.jar

# Scripts required to start transiTime.
COPY docker/bin/check_db_up.sh /usr/local/transitclock/bin/check_db_up.sh
COPY docker/bin/generate_sql.sh /usr/local/transitclock/bin/generate_sql.sh
COPY docker/bin/create_tables.sh /usr/local/transitclock/bin/create_tables.sh
COPY docker/bin/create_api_key.sh /usr/local/transitclock/bin/create_api_key.sh
COPY docker/bin/create_webagency.sh /usr/local/transitclock/bin/create_webagency.sh
COPY docker/bin/import_gtfs.sh /usr/local/transitclock/bin/import_gtfs.sh
COPY docker/bin/start_transitclock.sh /usr/local/transitclock/bin/start_transitclock.sh
COPY docker/bin/get_api_key.sh /usr/local/transitclock/bin/get_api_key.sh
COPY docker/bin/import_avl.sh /usr/local/transitclock/bin/import_avl.sh
COPY docker/bin/process_avl.sh /usr/local/transitclock/bin/process_avl.sh
COPY docker/bin/update_traveltimes.sh /usr/local/transitclock/bin/update_traveltimes.sh
COPY docker/bin/set_config.sh /usr/local/transitclock/bin/set_config.sh

# Handy utility to allow you connect directly to database
COPY docker/bin/connect_to_db.sh /usr/local/transitclock/bin/connect_to_db.sh

ENV PATH="/usr/local/transitclock/bin:${PATH}"

# This is a way to copy in test data to run a regression test.
# COPY docker/data/avl.csv /usr/local/transitclock/data/avl.csv
# COPY docker/data/gtfs_hart_old.zip /usr/local/transitclock/data/gtfs_hart_old.zip

RUN \
	sed -i 's/\r//' /usr/local/transitclock/bin/*.sh &&\
 	chmod 777 /usr/local/transitclock/bin/*.sh

COPY docker/config/postgres_hibernate.cfg.xml /usr/local/transitclock/config/hibernate.cfg.xml
COPY ${TRANSITCLOCK_PROPERTIES} /usr/local/transitclock/config/transitclock.properties

CMD ["/start_transitclock.sh"]