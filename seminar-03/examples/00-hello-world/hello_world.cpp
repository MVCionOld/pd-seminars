#include <iostream>
#include <omp.h>

int main (int argc, char **argv) {
#ifdef _OPENMP
    std::cout << "OpenMP is supported" << std::endl;
#endif

    int thread_id;
    int num_of_threads;

#pragma omp parallel private(thread_id)
    {
        thread_id = omp_get_thread_num();

#pragma omp critical
        {
            std::cout << "Hello, World from id: " << thread_id << "!" << std::endl;
        }

        if (thread_id == 0) {
            num_of_threads = omp_get_num_threads();
            std::cout << "Num of threads: " << num_of_threads << std::endl;
        }
    }

    return 0;
}
