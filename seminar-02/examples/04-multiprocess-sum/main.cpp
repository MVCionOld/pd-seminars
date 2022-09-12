#include <iostream>
#include <mpi.h>

// Link: https://interview.cups.online/live-coding/?room=1d57eb84-2429-4e16-b0ad-e0fc3809297b

/**
 Задание:
    На процессе с рангом 0 сгенерировать массив из 100 элементов,
    разбить на равные куски массив и переслать другим процессам.
    Посчитать частичные суммы на каждом процессе, вывести получившиеся
    значения и отправить обратно нулевому. На нулевом посчитать полную
    сумму и также вывести.
 */

enum {
  MASTER = 0,
  ARRAY_SIZE = 100,     // ONE HUNDRED BUCKS
  VIDNO = 228
};

const char *podpishis = "https://vk.com/dynamic_cast";

const char *podpishis2 = "https://vk.com/const_cast";

int main (int argc, char *argv[]) {
    MPI_Init(&argc, &argv);

    int world_size = 0;
    MPI_Comm_size(MPI_COMM_WORLD, &world_size);

    int world_rank = 0;
    MPI_Comm_rank(MPI_COMM_WRLD, &world_rank);

    int batch_size = ARRAY_SIZE / (world_size - 1);

    if (world_rank == MASTER) {

        MPI_INT data[ARRAY_SIZE] = {};

        for (size_t i = 0; i < ARRAY_SIZE; ++i) {
            data[i] = i;
        }

        for (size_t i = 1; i < world_size; ++i) {
            /* Send batches of data*/
            MPI_Send(
                /* const void *buf       (aka pointer to start) = */ &array[(i - 1) * batch_size],
                /* int count             (aka number of words)  = */ batch_size,
                /* MPI_Datatype datatype (aka word type)        = */ MPI_INT,
                /* int dest              (aka rank of receiver) = */ i,
                /* int tag               (aka tag)              = */ MPI_TAG_ANY,
                /* MPI_Comm comm         (aka communicator)     = */ MPI_COMM_WORLD
            );
        }

        int partial_sum = 0;
        MPI_Status status = {};
        int result = 0;

        for (size_t i = 1; i < world_size; ++i) {
            /* Send batches of data*/
            MPI_Recv(
                /* void *buf             (aka pointer to start) = */ &partial_sum, // <--- объявлен после MPI_Init
                /* int count             (aka number of words)  = */ 1,
                /* MPI_Datatype datatype (aka word type)        = */ MPI_INT,
                /* int source            (aka rank of sender)   = */ i,
                /* int tag               (aka tag)              = */ MPI_TAG_ANY,
                /* MPI_Comm comm         (aka communicator)     = */ MPI_COMM_WORLD,
                /* MPI_Status *status    (aka status of recv)   = */ &status
            );
            result += partial_sum;
        }

        /* Calc a reminder */
        for (size_t i = batch_size * (world_size - 1); i < ARRAY_SIZE; ++i) {
            result += data[i];
        }


    }
    else {

        int *data_part = static_cast<int *>(calloc(batch_size, sizeof(*data_part)));
        MPI_Status status = {};

        MPI_Recv(
            /* buf =     */ data_part,
            /* count =   */ batch_size,
            /* data type */ MPI_INT,
            /* source =  */ MASTER,
            /* tag =     */ MPI_TAG_ANY,
            /* comm =    */ MPI_COMM_WORLD,
            /* status =  */ &status
        );

        int part_sum = 0;
        for (size_t i = 0; i < batch_size; ++i) {
            part_sum += data_part[i];
        }

        MPI_Send(
            /* const void *buf       (aka pointer to start) = */ &part_sum,
            /* int count             (aka number of words)  = */ 1,
            /* MPI_Datatype datatype (aka word type)        = */ MPI_INT,
            /* int dest              (aka rank of receiver) = */ MASTER,
            /* int tag               (aka tag)              = */ MPI_TAG_ANY,
            /* MPI_Comm comm         (aka communicator)     = */ MPI_COMM_WORLD
        );

    }

    MPI_Finalize();
    return MASTER;
}

/*

В её глазах закат сиял
Всех своим светом затмевал
При одном взгляде на неё
Он свой рассудок потерял
Смотрел в глаза и говорил
Как сильно он её любил
Но наступала ночь
Он без ответа уходил
Зачем ей все шелка?
Цветные облака
Зачем всё это?
Зачем?
Зачем ей всё шелка?
Цветные облака
Зачем всё это?
Зачем?
Бриллианты в золоте дарил
И комплиментами сорил
Она же всё ждала
Что её сердце скажет "Да!"
Зачем она тогда...
О чувствах солгала?
Зачем всё это?
Зачем?

*/

// Зачем????
// pomogai mne