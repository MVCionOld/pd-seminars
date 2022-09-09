#include <iostream>
#include <omp.h>

int main (int argc, char **argv) {
    int all_sum = 0;

# pragma omp parallel
    {
# pragma omp for
        for (int i = 0; i < 10000; i++) {
            all_sum += 1;
        }
    }

    std::cout << "Result sum: " << all_sum << std::endl;
}
