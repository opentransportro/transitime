#!/bin/bash
export PGPASSWORD=transitclock

echo "Stopping old containers"
docker stop tc tc-db > /dev/null

echo "Removing old containers"
docker rm tc-db tc > /dev/null

docker rmi transitclock-server
docker build -t transitclock-server:latest .
mkdir "logs"

docker run --name tc-db -p 5432:5432 -e POSTGRES_PASSWORD=$PGPASSWORD -d postgres:15.5
echo "-- wait for db"
docker run --name tc --rm --link tc-db:postgres -e PGPASSWORD=$PGPASSWORD transitclock-server check_db_up.sh
echo "-- creating tables"
docker run --name tc --rm --link tc-db:postgres -e PGPASSWORD=$PGPASSWORD transitclock-server create_tables.sh > ./logs/table-creation.log 2>&1
echo "-- starting transit clock"
docker run --name tc --rm --link tc-db:postgres -e PGPASSWORD=$PGPASSWORD -p 8080:8080 transitclock-server  start.sh > ./logs/transit-clock.log 2>&1
