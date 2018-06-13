#!/bin/bash
# build a canned Postgres image
docker build -t "postgres:wave-challenge" -f Dockerfile.postgres .

# assemble the application as a Docker image (will be wave-challenge:<sbt version>)
sbt docker:publishLocal
