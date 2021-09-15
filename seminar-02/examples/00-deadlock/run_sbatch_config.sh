#!/bin/bash
#
#SBATCH --ntasks=2
#SBATCH --cpus-per-task=1
#SBATCH --partition=RT
#SBATCH --job-name=deadlockexample
#SBATCH --comment="Run mpi from config (deadlock)"
#SBATCH --output=output.txt
#SBATCH --error=error.txt
mpiexec ./a.out
