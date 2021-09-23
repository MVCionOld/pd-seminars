#include <iostream>
#include <omp.h>

int main (int argc, char **argv) {
    int sum = 0;

#pragma omp parallel for schedule(dynamic, 1000) reduction(+:sum)
    for (int i = 0; i < 10000; i++) {
        sum += 1;
    }

    std::cout << "Result sum: " << sum << std::endl;
}
