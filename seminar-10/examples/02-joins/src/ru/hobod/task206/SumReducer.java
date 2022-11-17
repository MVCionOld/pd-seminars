package ru.hobod.task206;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Created by akhtyamov on 26.02.17.
 */
public class SumReducer extends Reducer<Text, StackExchangeEntry, IntWritable, PairWritable> {
    private LongWritable count = new LongWritable();
    private Boolean isWrittenEmptyData = false;

    private void writeEmptyData(Context context) throws IOException, InterruptedException {
        if (!isWrittenEmptyData) {
	    for (int age = 0; age <= 96; ++age) {
	        context.write(new IntWritable(age), new PairWritable(0, 0));
	    }
            isWrittenEmptyData = true;
       	} 
    }

    @Override
    public void reduce(Text key, Iterable<StackExchangeEntry> values, Context context)
            throws IOException, InterruptedException {

        Integer userAge = null;
        int questionsCount = 0;
        int answersCount = 0;
        for (StackExchangeEntry entry : values) {
//            System.err.println(entry.type + " " + entry.age + " " + entry.questionCount + " " + entry.answerCount);
            if (entry.type.toString().equals("post")) {
                questionsCount += entry.questionCount.get();
                answersCount += entry.answerCount.get();
            } else {
                if (userAge != null) {
                    System.err.println(key + " ALERT " + entry.age.get());
                }
                userAge = entry.age.get();
            }
        }

        if (userAge != null && (questionsCount > 0 || answersCount > 0) && (int)userAge > 0) {
            context.write(new IntWritable(userAge), new PairWritable(questionsCount, answersCount));
        }
        writeEmptyData(context);
        
    }
}
