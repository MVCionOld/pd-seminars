### Hive. Пользовательские функции

* Regular UDF: обрабатываем вход построчно,
* UDAF: аггрегация, n строк на вход, 1 на выходе,
* UDTF: 1 строка на вход, таблица (несколько строк и полей) на выходе,
* Window functions: "окно" (несколько строк, *m*) на вход, несколько строк(*n*) на выходе (1 строка для каждого окна). Функции аггрегации и UDAF тоже могут быть использованы в качестве оконных.

#### I. (Regular) User-defined functions

1. Для реализации UDF нужно создать Java-класс, являющийся наследником класса org.apache.hadoop.hive.ql.exec.UDF.
2. Реализовать в этом классе один или несколько методов evaluate(), в которых будет записана логика UDF.
3. Для сборки нужно подключить ещё один Jar-файл:
```
/opt/cloudera/parcels/CDH/lib/hive/lib/hive-exec.jar
```
4. Для использования UDF в запросе нужно:

а) добавить собранный Jar-файл в Distributed cache (можно использовать относительный путь):
```
ADD JAR <path_to_jar>
```
При этом никаких дополнительных Jar-файлов в запросе можно не добавлять т.к. Jar с UDF уже содержит все необходимые коды.

б) создать функцию на основе Java-класса UDF:
```
CREATE TEMPORARY FUNCTION <your_udf> AS 'com.your.OwnUDF';
```
> **Пример 10.** Реализовать UDF, которая возвращает тоже, что было подано ей на вход без каких-либо изменений.

На данном примере можно изучить синтаксис UDF и использовать его в дальнейших задачах. Код UDF и запроса с её использованием лежит в:
```
/home/velkerr/seminars/pd2020/13-hive_adv/1-example_udf[example.sql, Identity/]
```

> **Задача 11.** Реализовать UDF, принимающую на вход IP-адрес. На выход UDF выдаёт число - сумму октетов адреса. Можно использовать как таблицу Subnets, так и SerDeExample т.к. IP есть в обеих.

С UDF:
* много кода,
* только **Java** :(

Без UDF:
* Ещё больше кода (правда на SQL). Пример: `/home/velkerr/seminars/pd2020/13-hive_adv/2-sum_udf/query_without_udf.sql`
* Не всегда можно реализовать в 1 запрос => будут подзапросы => будет несколько Job (дольше).

#### II. User-defined table functions (UDTF)

От обычных UDF данный вид функций отличается тем, что на выходе может быть больше одной записи. Причём столбцов также может быть сгенерировано несколько, т.е. по одной записи на входе мы можем получить целую таблицу. Отсюда и название.

1. Для реализации UDTF нужно создать класс-наследника от org.apache.hadoop.hive.ql.udf.generic.GenericUDTF.
2. Логика UDTF пишется в 3 методах:

   а) `initialize()`:
    - разбор входных данных (проверка количества аргументов и их типов), сохранение данных в ObjectInspector'ы
    - создание структуры выходной таблицы (названия и типы полей)

   б) `process()`: реализация механизма получения выходных данных из входных,

   в) `close()`: некий аналог cleanup() в MapReduce. Обрабатывает то, что не было обработано в `process()`.

3. Собираем Jar также, как и в случае с обычными UDF, однако для сборки подключить нужно не 1, а 2 дополнительных Jar:
```
/opt/cloudera/parcels/CDH/lib/hive/lib/hive-exec.jar
/opt/cloudera/parcels/CDH/lib/hive/lib/hive-serde.jar
```

> **Пример 12.** Реализовать UDTF, принимающую на вход IP-адрес. На выход выдаём этот же адрес, повторённый дважды.  Чтоб разграничить выводы для каждого IP, последней строкой в столбце выведите разделитель "-----".

|Вход|Выход|
|:----:|:---:|
|60.143.233.0|60.143.233.0|
||60.143.233.0|
||-----|
|14.226.82.0|14.226.82.0|
||14.226.82.0|
||-----|

Код UDTF и запроса с её использованием лежит в:
```
/home/velkerr/seminars/pd2020/13-hive_adv/3-example_udtf[example.sql, CopyIp/]
```
> **Задача 13.** Реализовать UDTF, принимающую на вход IP-адрес. На выход выдаём список октетов адреса. Чтоб разграничить выводы для каждого IP, последней строкой в столбце выведите числовой разделитель (исп. Integer.MAX_VALUE).

|Вход|Выход|
|:----:|:---:|
|60.143.233.0|60|
||143|
||233|
||0|
||2147483647|
|14.226.82.11|14|
||226|
||82|
||11|
||2147483647|

#### III. User-defined aggregation functions (UDAF)

Позволяют реализовать свои функции наподобие `SUM()`, `COUNT()`, `AVG()`.

**Доп. литература.** [Programming hive](https://www.gocit.vn/files/Oreilly.Programming.Hive-www.gocit.vn.pdf), гл. 13 "Functions" (с. 163).

### Оптимизации в Join'ах

Имеется 2 таблицы в базе `velkerr_test`

**Logs**
* ip (string)
* date (int)
* request (string)
* pagesize (smallint)
* statuscode (smallint)

**IpRegions**
* ip (string)
* region (string)

Сделаем JOIN 2 таблиц с отключенным MapJoin:

```sql
USE velkerr_test;
SET hive.auto.convert.join=false;

SELECT * FROM logs LEFT JOIN ipregions ON logs.ip = ipregions.ip
LIMIT 10;
```
* Видим, что wall time на незагруженном кластере 15+ секунд.
* В Hue Job browser или YARN JobHistory видим, что сгенерировался редьюсер.

Исследуем размер таблицы IpRegions. `SELECT count(1) FROM ipregions;` выдает всего 10 строк, значит она поместится в память. Можем использовать MapJoin (Вспоминаем что такое Map-side join в Hadoop).

Есть 2 способа подсказать Hive что нужно использовать MapJoin.

```sql
USE velkerr_test;

SELECT /*+ MAPJOIN(ipregions) */ * FROM logs LEFT JOIN ipregions ON logs.ip = ipregions.ip
LIMIT 10;
```
*(может не сработать)*

```sql
USE velkerr_test;
SET hive.auto.convert.join=true;

SELECT * FROM logs LEFT JOIN ipregions ON logs.ip = ipregions.ip
LIMIT 10;
```
Видим, что редьюсер пропал и задача стала работать быстрее (~5 с. на незагруженном кластере).

MapJoin нельзя использовать если маленькая таблица стоит слева при `LEFT JOIN` или справа при `RIGHT JOIN`.

[Подробнее про MapJoin в Hive](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+JoinOptimization#LanguageManualJoinOptimization-PriorSupportforMAPJOIN).