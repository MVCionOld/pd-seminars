package ru.hobod.task206;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by akhtyamov on 26.02.17.
 */
public class SortReducer extends Reducer<IntWritable, PairWritable, Text, Text> {

    @Override
    public void reduce(IntWritable count, Iterable<PairWritable> counts, Context context)
            throws IOException, InterruptedException {

        int questionsCount = 0;
        int answersCount = 0;
        for (PairWritable pair : counts) {
            questionsCount += pair.questionsCount.get();
            answersCount += pair.answersCount.get();
        }
        Text answer = new Text();
        String answerString = "";
        answerString += Integer.valueOf(questionsCount).toString();
        answerString += " ";
        answerString += Integer.valueOf(answersCount).toString();
        answer.set(answerString);
        context.write(new Text(Integer.valueOf(count.get()).toString()), answer);
    }
}
