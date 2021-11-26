package com.pd;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;

@Description(
        name = "FirstOctet",
        value = "Returns the first octet of ip address",
        extended = "Example:\n" +
                "SELECT FirstOctet(ip) from Subnets;"
)
public class FirstOctetUDF extends UDF {

    public String evaluate(String ip) {
        if (ip == null) {
            return null;
        } else {
            return ip.substring(0, ip.indexOf("."));
        }
    }
}
