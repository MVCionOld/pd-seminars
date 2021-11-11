# Семинар №10

---

## Hadoop MapReduce Advanced

[Общий ридинг](https://gitlab.com/fpmi-atp/pd2021-supplementary/global/-/blob/master/materials/09-mapreduce_part2.md)

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

---

### Joins

Задача:  

По данным stackoverflow посчитайте гистограмму количества вопросов и ответов в зависимости по возрастам пользователей. В случае отсутствия или невалидного возраста пусть будет 0. Выведите ее на печать, сортировка по возрасту (числовая, по возрастанию).
На печать: весь результат, сортировка по возрасту.

* *Входные данные:* посты stackoverflow.com
* *Формат ответа:* age <tab> num_questions <tab> num_answers

#### Описание входных данных.

`/data/stackexchange/posts` - сами посты, записаны в строках, начинающихся с ‘<row’ (можно разбирать строки вручную, без специальных xml-парсеров). Значение полей:

* PostTypeId - ‘1’, если это вопрос; ‘2’, если ответ на вопрос
* Id - идентификатор; если это вопрос, то он откроется тут: http://stackoverflow.com/questions/<Id>
* Score - показатель полезности этого вопроса или ответа
* FavoriteCount - добавления вопроса в избранные
* ParentId - для ответа - идентификатор поста-вопроса
* **OwnerUserId - идентификатор пользователя - автора поста**

`/data/stackexchange/users` - пользователи

* **Id - идентификатор пользователя** (в posts это OwnerUserId)
* Age - возраст (может отсутствовать)
* Reputation - уровень репутации пользователя

  
Как бы Вы решали подобную задачу, используя реляционную БД и привычный Вам диалект SQL (например, Postgres)?

```sql
    SELECT
        coalesce(u.Age :: integer, 0) as age
         
        , sum(
            case p.PostTypeId
                when '1' then 1
                else 0
            end
        ) as num_questions
         
        , sum(
            case p.PostTypeId
                when '2' then 1
                else 0
            end
        ) as num_answers
    
    FROM
        Users u
    JOIN
        Posts p
        On u.Id = p.OwnerUserId
    GROUP BY
        coalesce(u.Age :: integer, 0)
    ORDER BY
        age ASC
```

Как реализовать join на MapReduce -- сложно, но можно (смотрите лекцию).

**Не надо печалиться, что все такое сложное, нам давно упростили жизнь, сделав Hive!**

