#include <iostream>
#include <cstdlib>
#include <omp.h>

double f(double x) {
    return 4 / (1 + x*x);
}

int main (int argc, char *argv[]) {
    int num_threads = atoi(argv[1]);
    long N = atol(argv[2]);

    omp_set_num_threads(num_threads);

    double integral = 0;
    double dx = 1 / (double) N;

    double start = omp_get_wtime();

#pragma omp parallel for reduction(+:integral)
    for (int i = 1; i <= N; i++) {
        double part = (f(dx * i) + f(dx * (i - 1))) * dx / 2;  // площадь трапеции
        integral = integral + part;
    }

    double end = omp_get_wtime();

    std::cout << "Processes " << num_threads
                << " took time " << end - start
                << " and got integral " << integral
                << std::endl;
}
