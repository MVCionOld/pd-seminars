package ru.hobod.task206;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by akhtyamov on 20.03.17.
 */
public class StackExchangeEntry implements Writable {

    public IntWritable age;
    public IntWritable questionCount;
    public IntWritable answerCount;
    public Text type;

    public StackExchangeEntry() {
        age = new IntWritable(0);
        questionCount = new IntWritable(0);
        answerCount = new IntWritable(0);
        type = new Text();
    }

    public StackExchangeEntry(Boolean isQuestion) {
        type = new Text("post");
        age = new IntWritable(0);
        if (isQuestion) {
            questionCount = new IntWritable(1);
            answerCount = new IntWritable(0);
        } else {
            questionCount = new IntWritable(0);
            answerCount = new IntWritable(1);
        }
    }

    public StackExchangeEntry(Integer age) {
        type = new Text("user");
        this.age = new IntWritable(age);
        questionCount = new IntWritable(0);
        answerCount = new IntWritable(0);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        type.write(dataOutput);
        age.write(dataOutput);
        questionCount.write(dataOutput);
        answerCount.write(dataOutput);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        type.readFields(dataInput);
        age.readFields(dataInput);
        questionCount.readFields(dataInput);
        answerCount.readFields(dataInput);
    }
}
