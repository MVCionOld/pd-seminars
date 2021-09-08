#include <iostream>
#include <mpi.h>

int main(int argc, char *argv[]) {
    MPI_Init(&argc, &argv);
    
    int procid, num_procs;
    MPI_Comm_rank(MPI_COMM_WORLD, &procid);
    MPI_Comm_size(MPI_COMM_WORLD, &num_procs);
    
    char processor_name[MPI_MAX_PROCESSOR_NAME];
    int name_length;
    MPI_Get_processor_name(processor_name, &name_length);
    
    double t1 = MPI_Wtime();
    
    std::cout << "Hello, World! My id is " << procid << " and my processor name is " << processor_name << std::endl;
    
    if (procid == 0) {
        std::cout << "All processes count: " << num_procs << std::endl;
    }
    
    double t2 = MPI_Wtime();
    
    if (procid == 0) {
        std::cout << "Time in seconds: " << t2 - t1 << std::endl;
    }
    
    MPI_Finalize();
    return 0;
}
