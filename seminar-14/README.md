## Spark. RDD API

1. Скопируйте в свой home на кластере:
- ноутбук из `/home/velkerr/seminars/pd2020/14-15-spark/05-spark-base_nb.ipynb`
- директорию images из `/home/velkerr/seminars/pd2020/14-15-spark/images`

2. Запустите ноутбук следующей командой из директории, в которую вы его скопировали:
```bash
PYSPARK_DRIVER_PYTHON=jupyter PYSPARK_PYTHON=/usr/bin/python3 PYSPARK_DRIVER_PYTHON_OPTS='notebook --ip="*" --port=<PORT> --no-browser' pyspark2 --master=yarn --num-executors=<N>
```
* `PORT` - порт, на котором откроется ноутбук. Порты 30000 - 30100 открыты наружу, их пробрасывать не нужно.
* `N` - кол-во executors (YARN containers), выделенных на приложение.

Подробное описание команды и материалы семинара в ноутбуке.
  

**Обязательно** в конце работы почистите за собой запущенные Spark-приложения, например, это можно сделать с помощью связки двух
команд YARN'a:
```shell
# узнать список запущенных Spark-приложений
# (также их можно найти в Spark UI в разделе Incompleted)
yarn application -list -appTypes SPARK
```
И по полученным `Application-Id` вида `application_XXXXXXXXXXXXX_YYYY`:
```shell
yarn application -kill application_XXXXXXXXXXXXX_YYYY
```