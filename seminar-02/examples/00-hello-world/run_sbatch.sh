#!/bin/bash

for i in {1..10}; do
  sbatch -n $i --comment="Hello world on MPI" \
  --output="output${i}.txt" \
  --error="error${i}.out" \
  run.sh
done
