#!/bin/bash
sbatch -n 10 --comment="Collective on MPI" \
  --output=output.txt \
  --error=error.out \
  run.sh
