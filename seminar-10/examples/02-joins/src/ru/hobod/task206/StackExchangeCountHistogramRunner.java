package ru.hobod.task206;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by akhtyamov on 26.02.17.
 */
public class StackExchangeCountHistogramRunner extends Configured implements Tool {
    private static final int REDUCERS_COUNT = 2;


    public static void main(String[] args) throws Exception {
        ToolRunner.run(new StackExchangeCountHistogramRunner(), args);
    }

    @Override
    public int run(String[] strings) throws Exception {
        Path usersPath = new Path(strings[0]);
        Path postsPath = new Path(strings[1]);
        Path outputPath = new Path(strings[2]);
        Path midFilePath = new Path(strings[2] + "_mid");
        Path partitionPath = new Path(strings[2] + "_partition");

        Configuration conf = this.getConf();
        FileSystem fs = FileSystem.get(conf);
        if (fs.exists(outputPath)) {
            fs.delete(outputPath, true);
        }
        if (fs.exists(midFilePath)) {
            fs.delete(midFilePath, true);
        }
        if (fs.exists(partitionPath)) {
            fs.delete(partitionPath, true);
        }

        Job counter = Job.getInstance(conf, "task_206_counter");

        counter.setJarByClass(StackExchangeCountHistogramRunner.class);
        counter.setMapperClass(InputDataMapper.class);
//        counter.setCombinerClass(SimpleCombiner.class);
        counter.setReducerClass(SumReducer.class);

        counter.setNumReduceTasks(REDUCERS_COUNT);

        counter.setMapOutputKeyClass(Text.class);
        counter.setMapOutputValueClass(StackExchangeEntry.class);
        counter.setOutputKeyClass(IntWritable.class);
        counter.setOutputValueClass(PairWritable.class);

        counter.setInputFormatClass(TextInputFormat.class);
        counter.setOutputFormatClass(SequenceFileOutputFormat.class);

        FileInputFormat.addInputPath(counter, usersPath);
        FileInputFormat.addInputPath(counter, postsPath);
        SequenceFileOutputFormat.setOutputPath(counter, midFilePath);
//
        if (!counter.waitForCompletion(true)) {
            deleteFolder(fs, midFilePath);
            return -1;
        }

        Job sorter = Job.getInstance(conf, "task_206_sorter");
        sorter.setJarByClass(StackExchangeCountHistogramRunner.class);
        sorter.setMapperClass(Mapper.class);
        sorter.setReducerClass(SortReducer.class);

        sorter.setMapOutputKeyClass(IntWritable.class);
        sorter.setMapOutputValueClass(PairWritable.class);
        sorter.setInputFormatClass(SequenceFileInputFormat.class);
        sorter.setOutputFormatClass(TextOutputFormat.class);

        SequenceFileInputFormat.setInputPaths(sorter, midFilePath);
        FileOutputFormat.setOutputPath(sorter, outputPath);

        sorter.setNumReduceTasks(REDUCERS_COUNT);

        InputSampler.Sampler<LongWritable, Text> sampler = new InputSampler.RandomSampler<>(0.8, 1000, 10);
        TotalOrderPartitioner.setPartitionFile(sorter.getConfiguration(), partitionPath);
        InputSampler.writePartitionFile(sorter, sampler);
        sorter.setPartitionerClass(TotalOrderPartitioner.class);

        int result = 1;
        if (sorter.waitForCompletion(true)) {
            printTop(fs, outputPath, 10);
            result = 0;
            deleteFolder(fs, midFilePath);
            deleteFolder(fs, partitionPath);
        }
        return result;
    }

    private static void deleteFolder(FileSystem fs, Path folder) throws IOException {
        fs.deleteOnExit(folder);
    }

    public static class Sorter implements Comparable<Sorter> {
        
        public String bigram;
        public Integer value;

        public Sorter(String bigram, Integer value) {
            this.bigram = bigram;
            this.value = value;    
        }

        @Override
        public int compareTo(Sorter other) {
            int firstResult = -value.compareTo(other.value);
            if (firstResult != 0) {
                return firstResult;
            }
            return bigram.compareTo(other.bigram);
        }

        @Override
        public String toString() {
            return bigram + "\t" + value.toString();    
        }
    }


    private void printTop(FileSystem fs, Path outputPath, int count) throws IOException {
        ArrayList<Sorter> values = new ArrayList<>();
        for (FileStatus status : fs.listStatus(outputPath)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(status.getPath())));
            String line = null;
            while (true) {
                line = reader.readLine();

                if (line == null || line.length() < 2) {
                    break;
                }
                System.out.println(line);
            }


        }

    }
}
