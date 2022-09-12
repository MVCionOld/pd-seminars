#!/bin/bash
sbatch -n 2 --comment="Sendrecv on MPI" \
  --output=output.txt \
  --error=error.out \
  run.sh
