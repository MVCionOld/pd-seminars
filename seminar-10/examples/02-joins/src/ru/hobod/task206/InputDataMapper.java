package ru.hobod.task206;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by akhtyamov on 26.02.17.
 */
public class InputDataMapper extends Mapper<LongWritable, Text, Text, StackExchangeEntry> {
    private final static LongWritable one = new LongWritable(1);
    private final static LongWritable zero = new LongWritable(0);
    private static Pattern pattern = Pattern.compile("[A-Z][A-Za-z]*=\"[^\"]+\"", Pattern.UNICODE_CASE);
    private static Pattern valuePattern = Pattern.compile("\"[^\"]+\"", Pattern.UNICODE_CASE);
    private final static String POSTS_NAME = "Posts.xml";
    private final static String USERS_NAME = "Users.xml";


    public Map<String, String> fillValues(List<String> rawTokensList) {
        Map<String, String>  tokensMap = new HashMap<>();
        for (String token : rawTokensList) {
            String[] splitted_tokens = token.split("=");
            String key = splitted_tokens[0];
            Matcher matcher = valuePattern.matcher(token);
            String value = null;
            if (matcher.find()) {
                value = matcher.group(0);
                if (value.length() <= 2) {
                    continue;
                }
                value = value.substring(1, value.length() - 1);

            }
            tokensMap.put(key, value);
        }
        return tokensMap;
    }

    @Override
    public void map(LongWritable offset, Text line, Context context) throws IOException, InterruptedException {
        String currentString = line.toString().trim();

        if (!currentString.startsWith("<row")) {
            return;
        }
        FileSplit split = (FileSplit)context.getInputSplit();
        Path filePath = split.getPath();
        String fileName = filePath.getName();


        Matcher matcher = pattern.matcher(currentString);
        ArrayList<String> keyValueTokens = new ArrayList<>();
        while (matcher.find()) {
            keyValueTokens.add(matcher.group(0));
        }

        Map<String, String> keyValueMap = fillValues(keyValueTokens);

        if (POSTS_NAME.equals(fileName)) {
            Boolean isQuestion = false;
            String userId = null;
            for (Map.Entry entry : keyValueMap.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (key.equals("PostTypeId")) {
                    if (value.equals("1")) {
                        isQuestion = true;
                    }
                }
                if (key.equals("OwnerUserId")) {
                    userId = value;
                }
            }
            if (userId == null) {
                return;
            }

            context.write(new Text(userId), new StackExchangeEntry(isQuestion));
        } else {
            Integer age = null;
            String userId = null;
            for (Map.Entry entry : keyValueMap.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (key.equals("Id")) {
                    userId = value;
                }
                if (key.equals("Age")) {
                    age = Integer.parseInt(value);
                }
            }
            if (age == null || userId == null) {
                return;
            }

            context.write(new Text(userId), new StackExchangeEntry(age));

        }

    }
}
