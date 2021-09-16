#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>

int main (int argc, char *argv[]) {
    MPI_Init(&argc, &argv);

    int world_rank;
    MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);

    int request_code = rand();
    printf("process %d has %d\n", world_rank, request_code);

    if (world_rank == 0) {
        request_code = 42;
    }

    MPI_Bcast(&request_code, 1, MPI_INT, 0, MPI_COMM_WORLD);

    if (world_rank != 0) {
        printf("process %d received %d\n", world_rank, request_code);
    }

    MPI_Finalize();
    return 0;
}