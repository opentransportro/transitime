FROM maven:3-eclipse-temurin-17

RUN apt-get update && apt-get install -y postgresql-client git-core vim jq

ENV AGENCYID ro.stpt
ENV AGENCYNAME STPT
ENV GTFS_URL https://data.opentransport.ro/routing/gtfs/gtfs-stpt.zip
ENV GTFSRTVEHICLEPOSITIONS https://api.opentransport.ro/exporter/v1/realtime/stpt/vehicle-positions
ENV TRANSITCLOCK_PROPERTIES docker/config/transitclock.properties
ENV PATH /usr/local/tomcat/bin:$PATH

ENV TOMCAT_TGZ_URL https://dlcdn.apache.org/tomcat/tomcat-10/v10.1.18/bin/apache-tomcat-10.1.18.tar.gz

EXPOSE 8080
RUN mkdir -p "/usr/local/tomcat"
WORKDIR /usr/local/tomcat

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
    mkdir /usr/local/transitclock/data

WORKDIR /usr/local/transitclock

COPY api/target/api.war /usr/local/tomcat/webapps/ROOT.war
COPY core/target/core.jar /usr/local/transitclock/core.jar

# Scripts required to start transiTime.
COPY docker/bin/waitforit /usr/local/transitclock/bin/waitforit
COPY docker/bin/check_db_up.sh /usr/local/transitclock/bin/check_db_up.sh
COPY docker/bin/create_tables.sh /usr/local/transitclock/bin/create_tables.sh
COPY docker/bin/start.sh /usr/local/transitclock/bin/start.sh
COPY docker/bin/get_api_key.sh /usr/local/transitclock/bin/get_api_key.sh
COPY docker/bin/update_traveltimes.sh /usr/local/transitclock/bin/update_traveltimes.sh

ENV PATH="/usr/local/transitclock/bin:${PATH}"

RUN \
	sed -i 's/\r//' /usr/local/transitclock/bin/*.sh &&\
 	chmod 777 /usr/local/transitclock/bin/*.sh

COPY docker/config/postgres_hibernate.cfg.xml /usr/local/transitclock/config/hibernate.cfg.xml
COPY docker/config/tomcat_postgres_hibernate.cfg.xml /usr/local/transitclock/config/tomcat_hibernate.cfg.xml

COPY docker/config/transitclock.properties /usr/local/transitclock/config/transitclock.properties
COPY docker/config/tomcat-transitclock.properties /usr/local/transitclock/config/tomcat-transitclock.properties

CMD ["start.sh"]