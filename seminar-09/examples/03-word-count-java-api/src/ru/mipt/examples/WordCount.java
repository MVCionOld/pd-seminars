package ru.mipt.examples;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by VeLKerr on 06.11.16.
 */
public class WordCount extends Configured implements Tool{
    /**
     * Базовый класс Configured отвечает за возможность получить конфигурацию HDFS (достаточно вызвать getConf() внутри run()).
     * Это полезно в тех случаях, когда нужно работать с HDFS из программы (например, удалять промежуточные результаты).
     *
     * Tool - интерфейс, содержащий единственный метод run(), который
     *      (а) парсит аргументы командной строки,
     *      (б) производит настройку Job'ы. Выполняется run() на клиенте.
     */

    /**
     * Маппер. На вход получаем сплит (фрагмент файла размером с HDFS-блок).
     * На выходе - множество пар (слово, 1).
     */
    public static class WordMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
        //переменная static final т.к. будет использоваться во всех мапперах без изменения
        private static final IntWritable one = new IntWritable(1);
        //здесь static не пишем т.к. значение переменной будет менятся в кажом маппере, а мапперы работаю параллельно
        private Text word = new Text();

        /**
         * Мап-функция. На вход подаётся строка данных, на выходе - множество пар (слово, 1).
         * (Чтобы разбивка шла не по строкам, нужно изменть разделитель в конфигурации textinputformat.record.delimiter)
         * @param offset номер строки, начиная от начала входного сплита (не будет использован ни в этом примере, ни в ДЗ).
         * @param line строка текста.
         * @param context объект, отвечающий за сохранение результата.
         */
        public void map(LongWritable offset, Text line, Context context) throws IOException, InterruptedException {
            for(String element: line.toString().split("\\s+")){
                word.set(element);
                context.write(word, one);
            }
        }
    }

    /**
     * Редьюсер. Суммирует пары (слово, 1) по ключу (слово).
     * На выходе получаем пары (уникальн_слово, кол-во).
     * В поставке Hadoop уже есть простейшие predefined reducers. Функционал данного редьюсера реализован в IntSumReducer.
     */
    public static class CountReducer extends Reducer<Text, IntWritable, Text, IntWritable>{
        //Пустой IntWritable-объект для экономии памяти, чтоб не создавать его при каждом выполнении reduce-функции
        private IntWritable count = new IntWritable();

        public void reduce(Text word, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            Iterator<IntWritable> it = values.iterator();
            int sum = 0;
            while (it.hasNext()){
                sum += it.next().get();
            }
            count.set(sum);
            context.write(word, count);
        }
    }

    @Override
    public int run(String[] strings) throws Exception {
        Path outputPath = new Path(strings[1]);

        // настройка Job'ы
        Job job1 = Job.getInstance();
        job1.setJarByClass(WordCount.class);

        job1.setMapperClass(WordMapper.class);
        job1.setCombinerClass(CountReducer.class);
        job1.setReducerClass(CountReducer.class);

        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(IntWritable.class);

        job1.setInputFormatClass(TextInputFormat.class);
        job1.setOutputFormatClass(TextOutputFormat.class);

        job1.setMapOutputKeyClass(Text.class);
        job1.setMapOutputValueClass(IntWritable.class);

        job1.setNumReduceTasks(8); // по умолчанию задаётся 1 reducer

        TextInputFormat.addInputPath(job1, new Path(strings[0]));
        TextOutputFormat.setOutputPath(job1, outputPath);

        return job1.waitForCompletion(true)? 0: 1; //ждём пока закончится Job и возвращаем результат
    }

    public static void main(String[] args) throws Exception {
        new WordCount().run(args);
    }
}
