#include <iostream>
#include <omp.h>

int main (int argc, char **argv) {
    int sum = 0;

    omp_set_num_threads(4);

#pragma omp parallel
    {
        int thread_num = omp_get_thread_num();
        int cur_sum = 0;

#pragma omp for collapse(2) // schedule(dynamic, 1000)
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 1000; j++) {
                cur_sum += 1;
            }
        }
#pragma omp critical
        {
            std::cout << "Thread " << thread_num << " got sum: " << cur_sum << std::endl;
            sum += cur_sum;
        }
    }

    std::cout << "Result sum: " << sum << std::endl;
}
