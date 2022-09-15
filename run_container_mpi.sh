#!/bin/zsh

CONTAINER_NAME=pd-mpi

docker run -d -e TZ=Europe/Moscow -e OMPI_ALLOW_RUN_AS_ROOT=1 -e OMPI_ALLOW_RUN_AS_ROOT_CONFIRM=1 \
  --volume=`pwd`:/home --name="${CONTAINER_NAME}" ubuntu:18.04 tail -f /dev/null
docker exec -t "${CONTAINER_NAME}" apt update
docker exec -t "${CONTAINER_NAME}" apt upgrade -y
docker exec -t "${CONTAINER_NAME}" apt-get install -y \
  build-essential make vim g++ sudo libomp-dev cmake libopenmpi-dev \
  openmpi-common openmpi-bin libopenmpi-dev openssh-client openssh-server net-tools netcat iptables