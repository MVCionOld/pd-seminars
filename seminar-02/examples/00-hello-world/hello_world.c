#include <stdio.h>
#include <mpi.h>

int main (int argc, char *argv[]) {
    MPI_Init(&argc, &argv);

    int procid, num_procs;
    MPI_Comm_rank(MPI_COMM_WORLD, &procid);
    MPI_Comm_size(MPI_COMM_WORLD, &num_procs);

    char processor_name[MPI_MAX_PROCESSOR_NAME];
    int name_length;
    MPI_Get_processor_name(processor_name, &name_length);

    printf("Hello, World! My id is %d and my processor name is %s\n", procid, processor_name);

    if (procid == 0) {
        printf("All processes count: %d\n", num_procs);
    }

    MPI_Finalize();
    return 0;
}
