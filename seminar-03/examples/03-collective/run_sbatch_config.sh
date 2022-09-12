#!/bin/bash
#
#SBATCH --ntasks=6
#SBATCH --cpus-per-task=1
#SBATCH --partition=RT
#SBATCH --job-name=collectiveexample
#SBATCH --comment="Run mpi from config (collective)"
#SBATCH --output=output.txt
#SBATCH --error=error.txt
mpiexec ./a.out
