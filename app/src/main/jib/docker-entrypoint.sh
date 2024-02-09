#!/usr/bin/env bash
# waiting for db to be up
./waitforit -h $POSTGRES_PORT_5432_TCP_ADDR -p $POSTGRES_PORT_5432_TCP_PORT -t 20

echo 'THETRANSITCLOCK DOCKER: Start TheTransitClock.'
# This is to substitute into config file the env values
find /app/config/ -type f -exec sed -i s#"POSTGRES_PORT_5432_TCP_ADDR"#"$POSTGRES_PORT_5432_TCP_ADDR"#g {} \;
find /app/config/ -type f -exec sed -i s#"POSTGRES_PORT_5432_TCP_PORT"#"$POSTGRES_PORT_5432_TCP_PORT"#g {} \;
find /app/config/ -type f -exec sed -i s#"PGPASSWORD"#"$PGPASSWORD"#g {} \;
find /app/config/ -type f -exec sed -i s#"AGENCYNAME"#"$AGENCYNAME"#g {} \;
find /app/config/ -type f -exec sed -i s#"GTFSRTVEHICLEPOSITIONS"#"$GTFSRTVEHICLEPOSITIONS"#g {} \;

JAVA_OPTS="$JAVA_OPTS -Dtransitclock.apikey=f78a2e9a -Dtransitclock.configFiles=/app/config/transitclock.properties -Dtransitclock.core.agencyId=$AGENCYID"

java "$JAVA_OPTS" -cp @/app/jib-classpath-file @/app/jib-main-class-file /var/transitclock/ "$@"