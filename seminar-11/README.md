### SQL over MapReduce. Hive

##### 1. Запуск оболочки Hive
Существует 3 основных вида запуска задач в Hive. Проверим их работу на команде `SHOW DATABASES` (показывает список существующих баз данных Hive).
* запуск с помощью интерактивной оболочки:

    ``` bash
    $ hive
    hive> SHOW DATABASES;
    ```
*  запуск внешней команды:
    ``` bash
    $ hive -e 'SHOW DATABASES'
    ```
* запуск внешнего файла:
    ``` bash
    $ echo 'SHOW DATABASES' > sh_db.sql # запись в файл
    hive -f sh_db.sql
    ```
Hive shell также позволяет запускать shell-команды внутри оболочки Hive.
*После '__!__' не должно быть пробелов, после команды должна ставиться '__;__'*.
Попробуем получить кол-во виртуальных ядер на клиенте:
```bash
hive> !nproc;
```
Составные команды вроде `cat file | grep 'key' | tee new_file` hive shell не поддерживает.
Также можно из Hive shell работать с hdfs.
```bash
hive> dfs -ls;
```

##### 1.1. Оболочка HUE

* Проброс порта: `ssh <USERNAME>@mipt-client.atp-fivt.org -L 8888:mipt-node03.atp-fivt.org:8888`
* Логин и пароль см. в чате.

##### 2. Создание базы данных
* Создадим тестовую БД.
    ``` bash
    hive> create database <YOUR_USER>_test location '/user/<YOUR_USER>/test_metastore';
    ```
  `Hive metastore` - это реляционная БД, которая находится в HDFS. Она хранит метаинформацию о таблицах. При создании базы нужно указать **полный путь** к metastore.
* Если вы указали неверное название базы или LOCATION, базу можно удалить:
    ``` bash
    hive> drop database if exists <YOUR_USER>_test cascade;
    ```
  Слово `CASCADE` отвечает за удаление базы вместе с её содержимым.
* Вывод информации о БД
    ``` bash
    hive> DESCRIBE DATABASE <YOUR_USER>_test
    ```
Выходим из Hive shell.

##### 3. Создание таблиц
Создадим таблицу в тестовой базе. Для исходных данных используем датасет "Подсети" (`/data/subnets/variant1`):
* IP-адрес,
* маска подсети, в которой он находится.

``` sql
ADD JAR /opt/cloudera/parcels/CDH/lib/hive/lib/hive-contrib.jar;
USE <YOUR_USER>_test;
DROP TABLE IF EXISTS Subnets;

CREATE EXTERNAL TABLE Subnets (
    ip STRING,
    mask STRING
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY  '\t'
STORED AS TEXTFILE
LOCATION '/data/subnets/variant1';
```
Пояснения
1. `ADD JAR` - подключение Jar'ника с классами Hive. Для запросов, которые запускают MapReduce-задачи.
2. `USE ...` - подключение к базе данных. Без этой строки таблицы будут создаваться в базе "default". Также можно вместо `USE` использовать аргумент `--database` при запуске запроса.
3. `EXTERNAL` - существует 2 типа таблиц: managed и external. External-таблицы работают с внешними данными не изменяя их, а managed позволяют их изменять.
4. `STORED AS` здесь выбирается формат хранения таблицы. Для External-таблиц формат должен совпадать с форматом хранения данных. Для managed рекомендуется использовать сжатые форматы хранения (RCFile, AVRO и т.д.).

Про типы в DDL HQL можно почитать на официальной страничке в [cwiki](https://cwiki.apache.org/confluence/display/hive/languagemanual+types#LanguageManualTypes-HiveDataTypes). Рекомендуется подходить с умом при определении типа
поля в DDL.

Записываем код в файл, сохраняемся и запускаем:
``` bash
hive -f my_query.hql
```
Проверим, как создалась таблица (выведем первые 10 строк):
```bash
hive --database <YOUR_USER>_test -e 'SELECT * FROM Subnets LIMIT 10'
```

Проверить список таблиц в базе:

```bash
hive --database <YOUR_USER>_test -e 'SHOW TABLES'
```

##### 4. Партиционирование
Создадим партиционированную таблицу из таблицы Subnets. Информация о каждой партиции хранится в отдельной HDFS-директории внутри metastore.
``` sql
ADD JAR /opt/cloudera/parcels/CDH/lib/hive/lib/hive-contrib.jar;

SET hive.exec.dynamic.partition.mode=nonstrict;

USE <YOUR_USER>_test;
DROP TABLE IF EXISTS SubnetsPart;

CREATE EXTERNAL TABLE SubnetsPart (
    ip STRING
)
PARTITIONED BY (mask STRING)
STORED AS TEXTFILE;

INSERT OVERWRITE TABLE SubnetsPart PARTITION (mask)
SELECT * FROM Subnets;
```

Уже здесь вы можете увидеть, что запрос транслируется в MapReduce-задачу. Чтоб убедиться в этом, можно зайти на ApplicationMaster UI: http://mipt-master.atp-fivt.org:8088
Видим 1 MapReduce Job, в которой имеется только Map-стадия.

`SET hive.exec.dynamic.partition.mode=nonstrict;` - динамическое создание партиций. По умолчанию Hive партиционирует статически, т.е. создаётся фиксированное число партиций. Но в данном случае мы не знаем, сколько у нас уникальных значений маски, а значит не знаем сколько потребуется партиций.

Проверить получившиеся партиции:
```bash
hive --database <YOUR_USER>_test -e 'SHOW PARTITIONS SubnetsPart'
```

С помощью `SET` можно устанавливать и другие конфиги для job'ы. Например, так можно задать название всех job, которые сгенерируются в запросе: `SET mapred.job.name=my_query;`. Другой способ - с помощью аргумента `--hiveconf hive.session.id=my_query`.

Коды программ: `/home/velkerr/seminars/mcs17_hive1`.

##### 5. Парсинг входных данных с помощью регулярных выражений
1. Создаём новую таблицу.

    ``` sql
    add jar /opt/cloudera/parcels/CDH/lib/hive/lib/hive-contrib.jar;
    add jar /opt/cloudera/parcels/CDH/lib/hive/lib/hive-serde.jar;
    USE <YOUR_USER>_test;
    DROP TABLE IF EXISTS SerDeExample;
    
    CREATE EXTERNAL TABLE SerDeExample (
        ip STRING,
        date STRING,
        request STRING,
        responseCode STRING
    )
    ROW FORMAT SERDE 'org.apache.hadoop.hive.contrib.serde2.RegexSerDe'
    WITH SERDEPROPERTIES (
        "input.regex" = '^(\\S*)\\t.*$'
    )
    STORED AS TEXTFILE
    LOCATION '/data/user_logs/user_logs_S';
    
    select * from SerDeExample limit 10;
    ```
   Получаем на выходе:
    ```
    135.124.143.193	NULL	NULL	NULL
    247.182.249.253	NULL	NULL	NULL
    135.124.143.193	NULL	NULL	NULL
    222.131.187.37	NULL	NULL	NULL
     ...
    ```

   Группы в регулярке матчатся в поля. Матчинг по умолчанию происходит в том же порядке, в каком группы расположены в регулярке. Для изменения порядка матчинга можно задать `"output.format.string"`. Подробнее см. [здесь](https://cwiki.apache.org/confluence/display/Hive/UserGuide).

   Видим, что получилось «откусить» регуляркой первое поле. Остальные пока NULL'ы.
2. Пробуем откусить следующее поле. Меняем регулярное выражение (3 табуляции ставятся только в этом случае, все остальные поля разделены одной табуляцией).

    ```
    "input.regex" = '^(\\S*)\\t\\t\\t(\\S*)\\t.*$'
    ```
   Сохраняемся, запускаем: `$ hive –f myQuery.sql`
   Получаем:

    ```
    135.124.143.193	20150601013300	NULL	NULL
    247.182.249.253	20150601013354	NULL	NULL
    135.124.143.193	20150601013818	NULL	NULL
    222.131.187.37	20150601013957	NULL	NULL
     ...
    ```

Для отладки регулярок полезно пользоваться сервисом https://regex101.com/. При работе с сервисом '//' нужно заменять на '/'.

3. Чтобы данные распарсились правильно, нужно воспользоваться таким regex:
```
"input.regex" = '^(\\S*)\\t{3}(\\d{8})\\S*\\t(\\S*)\\t\\S+\\t(\\S*)\\t.*$'
```
Более сильный вариант (вместо `\S*` либо `\S+` либо `\d+` что позволяет не пропустить некорректные строки).
```
"input.regex" = '^(\\S+)\\t{3}(\\d{8})\\d+\\t(\\S+)\\t\\d+\\t(\\d+)\\t.*$
```
> **Задача.** Добавьте его в запрос и выполните, чтоб убедиться, что данные распарсились правильно.

***Примечание.*** Библиотека SerDe из `hive.contrib` имеет одну особенность. На выход после парсинга она выдаёт только строки тогда как в датасете имеются и числа. В случае, если в таблице есть числовые столбцы рекомендуется использовать `org.apache.hadoop.hive.serde2.RegexSerDe`. Эта реализация SerDe корректно парсит большинство типов данных Hive, но работает только для десериализации (т.е. для чтения данных). Синтаксис заросов при использовании этой версии SerDe почти не отличается. Единственное отличие - в пути к классу, который прописываем в `ROW FORMAT SERDE`.

Вывод информации про таблицу:
``` bash
hive -S --database <YOUR_USER>_test -e 'DESCRIBE SerDeExample'
```
Новый аргумент: `-S` отключает логи, информацию о времени выполнения и т.д. Остаётся только результат запроса.
```
ip                  	string              	from deserializer   
date                	string              	from deserializer   
request             	string              	from deserializer   
responsecode        	string              	from deserializer
```
##### 6. Практика
> **Задача 0.** Посчитать кол-во различных масок подсети.

Решение.
```
ADD JAR /opt/cloudera/parcels/CDH/lib/hive/lib/hive-contrib.jar;
USE <YOUR_USER>_test;

SELECT COUNT(DISTINCT mask)
FROM Subnets;
```
С помощью ключевого слова `EXPLAIN` можно вывести план запроса. Там будет показано в какие MapReduce-Job'ы будет транслироваться запрос.

```
STAGE DEPENDENCIES:
  Stage-1 is a root stage
  Stage-0 depends on stages: Stage-1

STAGE PLANS:
  Stage: Stage-1
    Map Reduce
      Map Operator Tree:
          TableScan
            ...
      Reduce Operator Tree:
        Group By Operator
          ...

  Stage: Stage-0
    Fetch Operator
      ...
```
Если зайти на ApplicationMaster Web UI [http://mipt-master.atp-fivt.org:8088] можно увидеть, что запрос действительно транслируется в MapReduce-задачи.
> **Задача 1.** Посчитать кол-во адресов, имеющих маску 255.255.255.128.

В этой задачи видим фильтрацию, поэтому партиционирование, кот. мы сделали раньше, должно повлиять на скорость работы задачи.
* Выполним запрос на таблице Subnets (без партиций) и SubnetsPart (с партициями). Что видим?
* Проверим размер исходных данных: `hdfs dfs -du -h /data/subnets/variant1`
* Пересоздадим таблицы Subnets и SubnetsPart на датасете `/data/subnets/big` (7 Gb) и повторим эксперимент. Как изменилась разница в быстродействии запросов?

> **Задача 2.** Посчитать среднее кол-во адресов по подсетям.
> По каждой задаче выведите план запроса и посчитайте по нему кол-во MapReduce Job.

##### 7. Hive streaming
Существует 2 основных способа использования внешних скриптов в Hive Streaming.
* использование команды,
* подключение внешних скриптов.

> **Пример.** Вывести 1-й октет IP-адресов из таблицы Subnets.

Решение 2-мя способами: `/home/velkerr/seminars/mcs17_hive1/6-1-streaming_example`

- `TRANSFORM`: выбирает поле, кот. мы будем обрабатывать с помощью streaming.
- `USING`: команда или подключаемый скрипт, кот. обрабатывает поле.
- `AS`: alias'ы для полей, кот. получаются после обработки стримингом. Полей модет быть несколько.

Внешние скрипты (по аналогии с Hadoop Streaming) не забываем добавлять в Distributed Cache с помощью `ADD FILE`.

###### 7.1. Отладка скриптов для Streaming
Внешние скрипты могут быть достаточно сложными, поэтому удобно сначала их отладить.
``` bash
hive --database <YOUR_USER>_test -e 'SELECT * FROM SerDeExample LIMIT 10' | ./<your_script>.sh
```

###### 7.2. Практика
Входные данные - таблица логов (SerDeExample).
> **Задача 4.** Заменить в логах дату 20150601 на сегодняшнее число, используя Streaming.

Входные данные - таблица логов (SerDeExample) или таблица подсетей (Subnets)
> **Задача 5.** Перевести IP-адреса в численное представление. Для быстрого перевода можно воспользоваться таким Python-кодом: `struct.unpack("!I", socket.inet_aton(ip)`

> По каждой задаче выведите план запроса и посчитайте по нему кол-во MapReduce Job.
