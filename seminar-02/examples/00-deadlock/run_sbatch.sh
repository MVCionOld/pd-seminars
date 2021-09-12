#!/bin/bash
sbatch -n 8 --comment="Deadlock on MPI" \
  --output=output.txt \
  --error=error.out \
  run.sh
