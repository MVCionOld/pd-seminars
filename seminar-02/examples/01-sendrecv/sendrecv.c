#include <iostream>
#include <mpi.h>

int main (int argc, char *argv[]) {
    MPI_Init(&argc, &argv);

    int world_rank;
    MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);

    int buff[10];

    if (world_rank == 0) {
        for (int i = 0; i < 5; ++i) {
            buff[i] = i;
        }
    } else {
        for (int i = 5; i < 10; ++i) {
            buff[i] = i * i;
        }
    }

    if (world_rank == 0) {
        MPI_Sendrecv(buff, 5, MPI_INT, 1, MPI_ANY_TAG,
                     &buff[5], 5, MPI_INT, 1, MPI_ANY_TAG,
                     MPI_COMM_WORLD, MPI_IGNORE_STATUS);
    } else {
        MPI_Sendrecv(&buff[5], 5, MPI_INT, 0, MPI_ANY_TAG,
                     buff, 5, MPI_INT, 0, MPI_ANY_TAG,
                     MPI_COMM_WORLD, MPI_IGNORE_STATUS);
    }

    MPI_Finalize();

    printf("0th process's array: ");
    for (int i = 0; i < 10; i++) {
        printf("%d ", buff[i]);
    }

    return 0;
}
