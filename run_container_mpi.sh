#!/bin/zsh

CONTAINER_NAME=pd-mpi

docker run -d --volume=`pwd`:/home --name="${CONTAINER_NAME}" ubuntu:18.04 tail -f /dev/null
docker exec -t "${CONTAINER_NAME}" apt update
docker exec -t "${CONTAINER_NAME}" apt upgrade -y
docker exec -t "${CONTAINER_NAME}" apt-get install -y build-essential make libomp-dev cmake openmpi-bin libopenmpi-dev