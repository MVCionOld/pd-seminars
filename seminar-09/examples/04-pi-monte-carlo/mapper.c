#include <stdio.h>
#include <stdlib.h>
#include <time.h>

enum {
    RANDOM_POINTS_NUM = 10000
};

int is_inside_unit_circle(float x, float y) {
    return x*x + y*y <= 1.0f;
}

int main() {
    srand(time(NULL));

    char buffer[1024];

    while (1) {
        if (0 > scanf("%s", buffer)) {
            break;
        }
    }

    for (int point = 0; point < RANDOM_POINTS_NUM; ++point) {
        float x = rand() * 1.0f / RAND_MAX;
        float y = rand() * 1.0f / RAND_MAX;

        int inside = is_inside_unit_circle(x, y);

        fprintf(stderr, "reporter:counter:Monte Carlo,Inside points,%d\n", inside);
        fprintf(stderr, "reporter:counter:Monte Carlo,Total points,%d\n", 1);
    }

    return 0;
}
