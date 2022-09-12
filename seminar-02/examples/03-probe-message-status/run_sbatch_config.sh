#!/bin/bash
#
#SBATCH --ntasks=2
#SBATCH --cpus-per-task=1
#SBATCH --partition=RT
#SBATCH --job-name=probeexample
#SBATCH --comment="Run mpi from config (probeexample)"
#SBATCH --output=output.txt
#SBATCH --error=error.txt
mpiexec ./a.out
