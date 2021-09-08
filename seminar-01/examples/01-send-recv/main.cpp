#include <mpi.h>
#include <iostream>

/**
    ------------------------------------------
    | C type            | MPI types          |
    | -------------------------------------- |
    | char	            | MPI_CHAR           |
    | unsigned char	    | MPI_UNSIGNED_CHAR  |
    | char	            | MPI_SIGNED_CHAR    |
    | short	            | MPI_SHORT          |
    | unsigned short    | MPI_UNSIGNED_SHORT |
    | int	              | MPI_INT            |
    | unsigned int	    | MPI_UNSIGNED       |
    | long int	        | MPI_LONG           |
    | unsigned long int	| MPI_UNSIGNED_LONG  |
    | long long int	    | MPI_LONG_LONG_INT  |
    | float	            | MPI_FLOAT          |
    | double	          | MPI_DOUBLE         |
    | long double	      | MPI_LONG_DOUBLE    |
    | unsigned char	    | MPI_BYTE           |
    ------------------------------------------

    MPI_Status - структура, содержащая атрибуты сообщений, содержит три обязательных поля:
    { ...
      MPI_Source; // номер процесса отправителя
      MPI_Tag   ; // идентификатор сообщения
      MPI_Error ; // код ошибки
    }
    MPI_STATUS_IGNORE - преодпределенная константа для игнорирования параметра status
 */

int main(int argc, char** argv) {
    MPI_Init(&argc, &argv);

    int world_size;
    MPI_Comm_size(MPI_COMM_WORLD, &world_size);

    int world_rank;
    MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);

    int array[10];
    MPI_Status status;

    if (world_rank == 0) {
        for (int i = 0; i < 10; ++i) {
            array[i] = i;
        }

        MPI_Send(
            /* const void *buf       (aka pointer to start) = */ &array[5],
            /* int count             (aka number of words)  = */ 5,
            /* MPI_Datatype datatype (aka word type)        = */ MPI_INT,
            /* int dest              (aka rank of receiver) = */ 1,
            /* int tag               (aka tag)              = */ 0,
            /* MPI_Comm comm         (aka communicator)     = */ MPI_COMM_WORLD
        );
    } else if (world_rank == 1) {
        MPI_Recv(
            /* void *buf             (aka pointer to start) = */ &array[5], // <--- объявлен после MPI_Init
            /* int count             (aka number of words)  = */ 5,
            /* MPI_Datatype datatype (aka word type)        = */ MPI_INT,
            /* int source            (aka rank of sender)   = */ 0,
            /* int tag               (aka tag)              = */ 0,
            /* MPI_Comm comm         (aka communicator)     = */ MPI_COMM_WORLD,
            /* MPI_Status *status    (aka status of recv)   = */ &status
        );

        std::cout << "Process 1 received 5 elements from process 0. They are: ";
        for (int i = 5; i < 10; ++i) {
            std::cout << array[i] << " ";
        }
        std::cout << ".";
    }
    
    MPI_Finalize();
    return 0;
}
