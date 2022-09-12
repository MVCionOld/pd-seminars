#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>

enum {
  ELEMENTS_PER_PROCESS = 100
};

float compute_avg(float* floats, int floats_size) {
    float res = 0;
    for (int i = 0; i < floats_size; ++i) {
        res += floats[i];
    }
    res /= floats_size;
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
            rand_nums[i] = rand();
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

    float sub_avg = compute_avg(sub_rand_nums, ELEMENTS_PER_PROCESS);
    printf("%d:%.6f\n", world_rank, sub_avg);

    float *sub_avgs = NULL;

    if (world_rank == 0) {
        sub_avgs = malloc(sizeof(float) * world_size);
    }

//    MPI_Gather(
//        void* send_data,
//        int send_count,
//        MPI_Datatype send_datatype,
//        void* recv_data,
//        int recv_count,
//        MPI_Datatype recv_datatype,
//        int root,
//        MPI_Comm communicator)
    MPI_Gather(&sub_avg, 1, MPI_FLOAT,
               sub_avgs, 1, MPI_FLOAT,
               0, MPI_COMM_WORLD);

    if (world_rank == 0) {
        float avg = compute_avg(sub_avgs, world_size);
        printf("Total avg: %.6f", avg);
    }

    MPI_Finalize();
    return 0;
}