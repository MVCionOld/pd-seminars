#include <assert.h>
#include <iostream>
#include <math.h>
#include <mpi.h>
#include <random>
#include <unistd.h>


enum {
  NUM_TASKS = 100
};


int solve_quadratic_equation(int* params, double* solution) {
    double a = (double) params[0];
    double b = (double) params[1];
    double c = (double) params[2];

    if (a == 0) {
        if (b == 0) {
            if (c == 0) {
                return -1;
            } else {
                return 0;
            }
        } else {
            solution[0] = -c / b;
            return 1;
        }
    }

    double discriminant = b*b - 4*a*c;

    if (discriminant > 0) {
        double x1 = (-b + sqrt(discriminant)) / (2*a);
        double x2 = (-b - sqrt(discriminant)) / (2*a);
        solution[0] = x1;
        solution[1] = x2;
        return 2;
    } else if (fabs(discriminant) < 1e-6) {
        double x0 = -b / (2*a);
        solution[0] = x0;
        return 1;
    } else {
        return 0;
    }

    return 0;
}

int main(int argc, char** argv) {
	MPI_Init(&argc, &argv);

	int rank;
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	int world_size;
	MPI_Comm_size(MPI_COMM_WORLD, &world_size);

    assert(NUM_TASKS % (world_size - 1) == 0);  // только для простоты демонстрации

	if (rank == 0) {
        MPI_Request send_requests[NUM_TASKS];
        MPI_Status send_statuses[NUM_TASKS];

        int equations[NUM_TASKS][3];

		for (int local_rank = 1; local_rank < world_size; ++local_rank) {
            for (int node_task = 0; node_task < NUM_TASKS / (world_size - 1); ++node_task) {
                int task_id = node_task + (local_rank - 1) * (NUM_TASKS / (world_size - 1));

                equations[task_id][0] = rand() % 16;
                equations[task_id][1] = rand() % 16;
                equations[task_id][2] = rand() % 16;

                MPI_Isend(equations[task_id], 3, MPI_INT, local_rank, 0, MPI_COMM_WORLD, &send_requests[task_id]);
            }
		}

        MPI_Waitall(NUM_TASKS, send_requests, send_statuses);

        int roots_num[NUM_TASKS];
        double roots[NUM_TASKS][2];

        for (int local_rank = 1; local_rank < world_size; ++local_rank) {
            for (int node_task = 0; node_task < NUM_TASKS / (world_size - 1); ++node_task) {
                int task_id = node_task + (local_rank - 1) * (NUM_TASKS / (world_size - 1));

                MPI_Recv(&roots_num[task_id], 1, MPI_INT, local_rank, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

                if (roots_num[task_id] > 0) {
                    MPI_Recv(
                        roots[task_id],
                        roots_num[task_id],
                        MPI_DOUBLE,
                        local_rank,
                        0,
                        MPI_COMM_WORLD,
                        MPI_STATUS_IGNORE
                    );
                }
            }
        }

        for (int local_rank = 1; local_rank < world_size; ++local_rank) {
            for (int node_task = 0; node_task < NUM_TASKS / (world_size - 1); ++node_task) {
                int task_id = node_task + (local_rank - 1) * (NUM_TASKS / (world_size - 1));

                std::cout << "[process: " << local_rank << "; task: " << task_id << "] "
                          << "(" << equations[task_id][0] << ")x^2 + "
                          << "(" << equations[task_id][1] << ")x + "
                          << "(" << equations[task_id][1] << ") = 0"
                          << " >>> roots: ";

                if (roots_num[task_id] == -1) {
                    std::cout << "INFINITE NUM OF ROOTS\n";
                } else if (roots_num[task_id] == 0) {
                    std::cout << "NO ROOTS\n";
                } else if (roots_num[task_id] == 1) {
                    std::cout << "[x0=" << roots[task_id][0] << "]\n";
                } else if (roots_num[task_id] == 2) {
                    std::cout << "[x1=" << roots[task_id][0] << ",x2=" << roots[task_id][1] << "]\n";
                } else {
                    std::cout << "ERROR\n";
                }
            }
        }

	} else {
        for (int i = 0; i < NUM_TASKS / (world_size - 1); ++i) {
            int params[3];
            MPI_Recv(params, 3, MPI_INT, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

            double solution[2];
            int roots_num = solve_quadratic_equation(params, solution);

            MPI_Send(&roots_num, 1, MPI_INT, 0, 0, MPI_COMM_WORLD);

            if (roots_num > 0) {
                MPI_Send(solution, roots_num, MPI_DOUBLE, 0, 0, MPI_COMM_WORLD);
            }
        }
	}
	
	MPI_Finalize();
} 
