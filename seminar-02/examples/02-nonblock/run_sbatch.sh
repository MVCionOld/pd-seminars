#!/bin/bash
sbatch -n 6 --comment="IsendIrecv on MPI" \
  --output=output.txt \
  --error=error.out \
  run.sh
