#!/usr/bin/env bash
echo 'THETRANSITCLOCK DOCKER: Create database'

createdb -h "$POSTGRES_PORT_5432_TCP_ADDR" -p "$POSTGRES_PORT_5432_TCP_PORT" -U postgres $AGENCYNAME
