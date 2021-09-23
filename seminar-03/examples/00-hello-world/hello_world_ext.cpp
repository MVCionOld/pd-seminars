#include <iostream>
#include <omp.h>

int main (int argc, char **argv) {
#ifdef _OPENMP
    std::cout << "OpenMP is supported" << std::endl;
#endif

    std::cout << "--------------------------------------------------------------------" << std::endl;

    int num_procs = omp_get_num_procs();
    std::cout << "Number of available processes: " << num_procs << std::endl;

    omp_set_num_threads(num_procs);
    int num_of_threads = omp_get_num_threads();
    std::cout << "Number of working threads (main part): " << num_of_threads << std::endl;

    int thread_id = omp_get_thread_num();
    std::cout << "Thread id (main part): " << thread_id << std::endl;

    std::cout << "--------------------------------------------------------------------" << std::endl;

#pragma omp parallel private(thread_id)
    {
        thread_id = omp_get_thread_num();

#pragma omp critical
        {
            std::cout << "Hello, World from id: " << thread_id << "!" << std::endl;
        }

        if (thread_id == 0) {
            num_of_threads = omp_get_num_threads();
            std::cout << "Num of working threads (parallel part): " << num_of_threads << std::endl;
        }
    }

    return 0;
}
