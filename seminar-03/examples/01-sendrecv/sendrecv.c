#include <mpi.h>
#include <stdio.h>

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
        MPI_Sendrecv(buff, 5, MPI_INT, 1, 0,
                     &buff[5], 5, MPI_INT, 1, 0,
                     MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    } else {
        MPI_Sendrecv(&buff[5], 5, MPI_INT, 0, 0,
                     buff, 5, MPI_INT, 0, 0,
                     MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    }

    if (world_rank == 0) {
        printf("0th process's array: ");
        for (int i = 0; i < 10; i++) {
            printf("%d ", buff[i]);
        }
    }

    MPI_Finalize();

    return 0;
}
