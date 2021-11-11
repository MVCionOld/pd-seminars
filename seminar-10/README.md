# Семинар №10

---

## Hadoop MapReduce Advanced

---

### Глобальная сортировка на Hadoop Streaming

Рассмотрим задачу:  

Данные представляют собой статистику по активности пользователей сервера Minecraft.
Отсортировать по среднему кол-ву команд. При равном среднем количестве команд, отсортировать лексикографически по никам пользователей.
```
Login    ср. кол-во команд на сессию     общее кол-во сессий
```
Путь к данным: `/data/minectaft_user_activity`

#### Пример вывода:
```
lucky20    25.0    10
avivzusim   5.0    15
```

#### Решение
1. Указываем то, что у нас 3 поля
2. Используем `KeyFieldBasedComparator`
3. В опциях настраиваем сортировку по 2-му полю в обратном (`r`) порядке и числовую (`n`) сортировку.
4. При прочих равных сортируем по 1м полю. Подробнее см. `man sort`, так как именно опции этой команды унаследованы для `mapreduce.partition.keycomparator.options`.

```bash
#!/usr/bin/env bash

IN_DIR="/data/minectaft_user_activity"
OUT_DIR="minecraft_result"

# Look at the input data
hdfs dfs -cat ${IN_DIR}/part-00000 | head -n 10

# Remove previous results
hdfs dfs -rm -r -skipTrash ${OUT_DIR}* > /dev/null

yarn jar /opt/cloudera/parcels/CDH/lib/hadoop-mapreduce/hadoop-streaming.jar \
    -D stream.num.map.output.key.fields=3 \
    -D mapreduce.job.reduces=1 \
    -D mapreduce.job.output.key.comparator.class=org.apache.hadoop.mapreduce.lib.partition.KeyFieldBasedComparator \
    -D mapreduce.partition.keycomparator.options='-k2,2nr -k1' \
    -mapper cat \
    -reducer cat \
    -input ${IN_DIR} \
    -output ${OUT_DIR}

# Checking result
hdfs dfs -cat ${OUT_DIR}/part-00000 | head -n 10
```

C использованием Hadoop Streaming допускается использовать один редьюсер для глобальной сортировки.
Если стоит задача "посчитайте топ-N записей", где `N` -- достаточно малое число, то можно использовать `R > 1` редьюсера.
Вывод каждого редьюсера можно также отсортировать как в примере выше. А после выполнить R-путевое слияние и вывести первые `N` строчек.

### Глобальная сортировка на Hadoop Java API

Используем `R` редьюсеров, если в случае Streaming порядок будет соблюдаться только внутри отдельно взятого редьюсера, то в случае Java API 
мы может добиться того, что порядок будет соблюдаться и между выводами редьюсеров. Для этого надо добавить отдельную джобу, в которой указать
Partitioner'y как надо распределять ключи по редьюсерам. Лобовое решение будет работать очень долго (пройтись по всем записям всех ключей),
поэтому можно воспользоваться семплированием. Проходим не все ключи, а выбираем с некоторой вероятностью, затем аппроксиимруем на весь датасет.

Пример кода: `InputSampler.Sampler<LongWritable, Text> sampler = new InputSampler.RandomSampler<>(0.5, 10000, 10);`.
* 0,5 - вероятность выбора записи:
* максимальное кол-во "выборов"
* максимальное кол-во сплитов.

Как только дошли до границы хотя бы по одному аргументу, семплирование прекращаем.

Такой Sampler подаётся в TotalOrderPartitioner. Подробнее см. "Hadoop. The definitive guide, 4 изд. стр. 287".
Пример WordCount с глобальной сортировкой: `examples/00-word-count-globalsort`.


