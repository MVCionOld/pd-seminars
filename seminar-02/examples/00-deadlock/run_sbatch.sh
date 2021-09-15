#!/bin/bash
sbatch -n 2 --comment="Deadlock on MPI" \
  --output=output.txt \
  --error=error.out \
  run.sh
