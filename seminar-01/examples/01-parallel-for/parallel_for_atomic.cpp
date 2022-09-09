#include <iostream>
#include <omp.h>

int main (int argc, char **argv) {
    int all_sum = 0;

#pragma omp parallel
    {
        int cur_sum = 0;
#pragma omp for
        for (int i = 0; i < 10000; i++) {
            cur_sum += 1;
        }
#pragma omp atomic
        all_sum += cur_sum;
    }

    std::cout << "Result sum: " << all_sum << std::endl;
}
