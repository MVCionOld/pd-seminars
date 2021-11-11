package ru.mipt;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.InputSampler;
import org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.StringTokenizer;

public class GlobalSorter extends Configured implements Tool {

    public static class DeletePunctAndCountMapper extends Mapper<LongWritable, Text, Text, LongWritable> {
        private final static LongWritable one = new LongWritable(1);
        private Text word = new Text();

        @Override
        public void map(LongWritable offset, Text line, Context context) throws IOException, InterruptedException {
            String str = line.toString().replaceAll("\\p{Punct}|\\d", " ");
            StringTokenizer tokenizer = new StringTokenizer(str);
            while (tokenizer.hasMoreTokens()) {
                word.set(tokenizer.nextToken());
                context.write(word, one);
            }
        }
    }

    public static class SumAndReverseReducer extends Reducer<Text, LongWritable, LongWritable, Text> {
        private LongWritable count = new LongWritable();

        @Override
        public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (LongWritable num : values) {
                sum += num.get();
            }
            count.set(sum);
            context.write(count, key);
        }
    }

    public static class StubMapper extends Mapper<LongWritable, Text, LongWritable, Text> {
        @Override
        public void map(LongWritable num, Text word, Context context) throws IOException, InterruptedException {
            context.write(num, word);
        }
    }

    public static class InverseReducer extends Reducer<LongWritable, Text, Text, LongWritable> {
        @Override
        public void reduce(LongWritable num, Iterable<Text> words, Context context) throws IOException, InterruptedException {
            for (Text word : words) {
                context.write(word, num);
            }
        }
    }

    private static void deleteFolder(FileSystem fs, Path... paths) throws IOException {
        for (Path path: paths) {
            if (fs.exists(path)) {
                fs.deleteOnExit(path);
            }
        }
    }

    @Override
    public int run(String[] strings) throws Exception {
        Path inputPath = new Path(strings[0]);
        Path outputPath = new Path(strings[1]);
        Path midPath = new Path(strings[1] + "_tmp");
        Integer reducersNum = Integer.parseInt(strings[3]);
        Configuration conf = this.getConf();
        FileSystem fs = FileSystem.get(conf);

        Job counter = Job.getInstance(conf);
        counter.setJobName("wordcounter");
        counter.setJarByClass(GlobalSorter.class);

        counter.setInputFormatClass(TextInputFormat.class);
        counter.setOutputFormatClass(SequenceFileOutputFormat.class);

        counter.setMapperClass(DeletePunctAndCountMapper.class);
        counter.setReducerClass(SumAndReverseReducer.class);
        counter.setNumReduceTasks(reducersNum);

        counter.setOutputKeyClass(LongWritable.class);
        counter.setOutputValueClass(Text.class);
        counter.setMapOutputKeyClass(Text.class);
        counter.setMapOutputValueClass(LongWritable.class);

        FileInputFormat.addInputPath(counter, inputPath);
        SequenceFileOutputFormat.setOutputPath(counter, midPath);

        if (!counter.waitForCompletion(true)) {
            deleteFolder(fs, midPath);
            return -1;
        }

        Path partPath = new Path(strings[1] + "_part");

        Job sorter = Job.getInstance(conf);
        sorter.setJobName("sorter");
        sorter.setJarByClass(GlobalSorter.class);

        sorter.setMapperClass(StubMapper.class);
        sorter.setReducerClass(InverseReducer.class);

        sorter.setInputFormatClass(SequenceFileInputFormat.class);
        sorter.setOutputFormatClass(TextOutputFormat.class);

        sorter.setOutputKeyClass(Text.class);
        sorter.setOutputValueClass(Text.class);
        sorter.setMapOutputKeyClass(LongWritable.class);
        sorter.setMapOutputValueClass(Text.class);
        sorter.setSortComparatorClass(LongWritable.DecreasingComparator.class);

        SequenceFileInputFormat.setInputPaths(sorter, midPath);
        FileOutputFormat.setOutputPath(sorter, outputPath);

        if (reducersNum > 1) {
            InputSampler.Sampler<LongWritable, Text> sampler = new InputSampler.RandomSampler<>(0.2, 10000, 10);
            sorter.setNumReduceTasks(reducersNum);
            TotalOrderPartitioner.setPartitionFile(sorter.getConfiguration(), partPath);
            InputSampler.writePartitionFile(sorter, sampler);
            sorter.setPartitionerClass(TotalOrderPartitioner.class);
            FileOutputFormat.setOutputPath(sorter, outputPath);
        }

        int resultCode = 0;
        if (!sorter.waitForCompletion(true)) {
            resultCode = -2;
        }
        deleteFolder(fs, midPath, partPath);
        return resultCode;
    }

    public static void main(String[] args) throws Exception {
        ToolRunner.run(new GlobalSorter(), args);
    }
}
