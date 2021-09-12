#!/bin/bash
sbatch -n 8 --comment="Sendrecv on MPI" \
  --output=output.txt \
  --error=error.out \
  run.sh
