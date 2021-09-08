#!/bin/bash
sbatch -n 8 --comment="Hello world on MPI" \
  --output=output.txt \
  --error=error.out \
  run.sh
