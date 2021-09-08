#!/bin/bash
#
#SBATCH --ntasks=2
#SBATCH --cpus-per-task=1
#SBATCH --partition=RT
#SBATCH --job-name=pingpongexample
#SBATCH --comment="Run mpi from config (ping-pong)"
#SBATCH --output=output.txt
#SBATCH --error=error.txt
mpiexec ./a.out
