#include <stdio.h>
#include <mpi.h>

int main(int argc, char *argv[]) {
    MPI_Init(&argc, &argv);

    int buff[10];
    
    MPI_Send(buff, 10, MPI_INT, 0, MPI_ANY_TAG, MPI_COMM_WORLD);
    // MPI_Send(buff, 10, MPI_INT, 0, MPI_ANY_TAG, MPI_COMM_WORLD, MPI_IGNORE_STATUS);
    
    MPI_Finalize();
    return 0;
}