package com.hobod;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * Created by velkerr on 26.03.17.
 */
@Description(
        name = "identity",
        value = "Returns input string without any changes",
        extended = "Example:\n" +
                "SELECT identity(field) from a;"
)
public class IdentityUDF extends UDF {

    public String evaluate(String str) {
        return str;
    }
}

