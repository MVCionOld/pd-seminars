package ru.hobod.task206;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by akhtyamov on 20.03.17.
 */
public class PairWritable implements Writable {
    public IntWritable questionsCount;
    public IntWritable answersCount;

    public PairWritable() {
        questionsCount = new IntWritable(0);
        answersCount = new IntWritable(0);
    }

    public PairWritable(Integer questionsCount, Integer answersCount) {
        this.questionsCount = new IntWritable(questionsCount);
        this.answersCount = new IntWritable(answersCount);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        questionsCount.write(dataOutput);
        answersCount.write(dataOutput);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        questionsCount.readFields(dataInput);
        answersCount.readFields(dataInput);
    }
}
