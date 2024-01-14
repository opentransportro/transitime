#!/usr/bin/env bash
echo 'THETRANSITCLOCK DOCKER: Start TheTransitClock.'
# This is to substitute into config file the env values
find /usr/local/transitclock/config/ -type f -exec sed -i s#"POSTGRES_PORT_5432_TCP_ADDR"#"$POSTGRES_PORT_5432_TCP_ADDR"#g {} \;
find /usr/local/transitclock/config/ -type f -exec sed -i s#"POSTGRES_PORT_5432_TCP_PORT"#"$POSTGRES_PORT_5432_TCP_PORT"#g {} \;
find /usr/local/transitclock/config/ -type f -exec sed -i s#"PGPASSWORD"#"$PGPASSWORD"#g {} \;
find /usr/local/transitclock/config/ -type f -exec sed -i s#"AGENCYNAME"#"$AGENCYNAME"#g {} \;
find /usr/local/transitclock/config/ -type f -exec sed -i s#"GTFSRTVEHICLEPOSITIONS"#"$GTFSRTVEHICLEPOSITIONS"#g {} \;

rmiregistry &

java -Xss12m -Xms16g -Xmx32g -Duser.timezone=Europe/Bucharest -Dtransitclock.configFiles=/usr/local/transitclock/config/transitclock.properties -Dtransitclock.core.agencyId=$AGENCYID -jar /usr/local/transitclock/core.jar --gtfs-url https://data.opentransport.ro/routing/gtfs/gtfs-stpt.zip &

sleep 15

#set the API as an environment variable so we can set in JSP of template/includes.jsp in the transitime webapp
export APIKEY="$(get_api_key.sh)"
echo "APIKEY SET AS $APIKEY"

# make it so we can also access as a system property in the JSP
export JAVA_OPTS="$JAVA_OPTS -Dtransitclock.apikey=$APIKEY -Dtransitclock.configFiles=/usr/local/transitclock/config/transitclock.properties"

/usr/local/tomcat/bin/startup.sh
tail -f /dev/null
