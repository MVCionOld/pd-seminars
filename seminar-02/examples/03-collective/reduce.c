#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>

//MPI_MAX - Returns the maximum element.
//MPI_MIN - Returns the minimum element.
//MPI_SUM - Sums the elements.
//MPI_PROD - Multiplies all elements.
//MPI_LAND - Performs a logical and across the elements.
//MPI_LOR - Performs a logical or across the elements.
//MPI_BAND - Performs a bitwise and across the bits of the elements.
//MPI_BOR - Performs a bitwise or across the bits of the elements.
//MPI_MAXLOC - Returns the maximum value and the rank of the process that owns it.
//MPI_MINLOC - Returns the minimum value and the rank of the process that owns it.

enum {
  ELEMENTS_PER_PROCESS = 100
};

float compute_sum(float* floats, int floats_size) {
    float res = 0;
    for (int i = 0; i < floats_size; ++i) {
        res += floats[i];
    }
    return res;
}

int main(int argc, char *argv[]) {
    MPI_Init(&argc, &argv);

    int world_size = 0;
    MPI_Comm_size(MPI_COMM_WORLD, &world_size);

    int world_rank = 0;
    MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);

    float* rand_nums = (float*)malloc(ELEMENTS_PER_PROCESS * world_size * sizeof(float));
    if (world_rank == 0) {
        for (int i = 0; i < ELEMENTS_PER_PROCESS * world_size; ++i) {
            rand_nums[i] = i;
        }
    }

    float *sub_rand_nums = malloc(sizeof(float) * ELEMENTS_PER_PROCESS);

//    MPI_Scatter(
//        void* send_data,
//        int send_count,
//        MPI_Datatype send_datatype,
//        void* recv_data,
//        int recv_count,
//        MPI_Datatype recv_datatype,
//        int root,
//        MPI_Comm communicator)
    MPI_Scatter(rand_nums, ELEMENTS_PER_PROCESS, MPI_FLOAT,
                sub_rand_nums, ELEMENTS_PER_PROCESS, MPI_FLOAT,
                0, MPI_COMM_WORLD);

    float local_sum = compute_sum(sub_rand_nums, ELEMENTS_PER_PROCESS);
    printf("Local sum for process %d: %.6f\n", world_rank, local_sum);

    float global_sum;
//    MPI_Reduce(
//        void* send_data,
//        void* recv_data,
//        int count,
//        MPI_Datatype datatype,
//        MPI_Op op,
//        int root,
//        MPI_Comm communicator)
    MPI_Reduce(&local_sum, &global_sum, 1, MPI_FLOAT, MPI_SUM, 0, MPI_COMM_WORLD);

    if (world_rank == 0) {
        printf("Total sum = %.6f\n", global_sum);
    }

    MPI_Finalize();
    return 0;
}