#!/bin/bash
export PGPASSWORD=transitclock

echo "Stopping old containers"
docker stop tc tc-db > /dev/null

echo "Removing old containers"
docker rm tc-db tc > /dev/null

docker rmi transitclock-server
docker build -t transitclock-server:latest \
  --build-arg TRANSITCLOCK_PROPERTIES="docker/config/transitclock.properties" \
  --build-arg AGENCYID="stpt" \
  --build-arg AGENCYNAME="STPT" \
  --build-arg GTFS_URL="https://data.opentransport.ro/routing/gtfs/gtfs-stpt.zip" \
  --build-arg GTFSRTVEHICLEPOSITIONS="https://api.opentransport.ro/exporter/v1/realtime/stpt/vehicle-positions" .

mkdir "logs"

docker run --name tc-db -p 5432:5432 -e POSTGRES_PASSWORD=$PGPASSWORD -d postgres:15.5
echo "-- wait for db"
docker run --name tc --rm --link tc-db:postgres -e PGPASSWORD=$PGPASSWORD transitclock-server check_db_up.sh
echo "-- creating tables"
docker run --name tc --rm --link tc-db:postgres -e PGPASSWORD=$PGPASSWORD transitclock-server create_tables.sh > ./logs/table-creation.log 2>&1
echo "-- import gtfs data"
docker run --name tc --rm --link tc-db:postgres -e PGPASSWORD=$PGPASSWORD transitclock-server import_gtfs.sh > ./logs/gtfs-import.log 2>&1
echo "-- creating api & webagency"
docker run --name tc --rm --link tc-db:postgres -e PGPASSWORD=$PGPASSWORD transitclock-server create_api_key.sh > ./logs/create-api-key.log 2>&1
docker run --name tc --rm --link tc-db:postgres -e PGPASSWORD=$PGPASSWORD transitclock-server create_webagency.sh > ./logs/create-web-agency.log 2>&1
#docker run --name tc --rm --link tc-db:postgres -e PGPASSWORD=$PGPASSWORD transitclock-server ./import_avl.sh
#docker run --name tc --rm --link tc-db:postgres -e PGPASSWORD=$PGPASSWORD transitclock-server ./process_avl.sh
echo "-- starting transit clock"
docker run --name tc --rm --link tc-db:postgres -e PGPASSWORD=$PGPASSWORD -p 8080:8080 transitclock-server  start_transitclock.sh > ./logs/transit-clock.log 2>&1
