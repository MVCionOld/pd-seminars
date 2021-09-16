#!/bin/bash
#
#SBATCH --ntasks=6
#SBATCH --cpus-per-task=1
#SBATCH --partition=RT
#SBATCH --job-name=isendirecvexample
#SBATCH --comment="Run mpi from config (IsendIrecv)"
#SBATCH --output=output.txt
#SBATCH --error=error.txt
mpiexec ./a.out
