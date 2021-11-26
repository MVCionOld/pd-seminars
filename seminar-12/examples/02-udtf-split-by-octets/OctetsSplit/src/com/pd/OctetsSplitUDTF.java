package com.pd;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.IntObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;

import java.util.ArrayList;
import java.util.List;

@Description(name = "OctetsSplit")
public class OctetsSplitUDTF extends GenericUDTF {

    private StringObjectInspector address;
    /**
     * Since the processing of 1 record may give us a full table, Hive uses an array for its storage.
     * However, in this example we have a single field in each record.
     * Therefore our array will always have 1 element.
     */
    private Object[] forwardObjArray = new Object[3];

    @Override
    public StructObjectInspector initialize(ObjectInspector[] args) throws UDFArgumentException {
        // Parsing an input data
        if(args.length != 1){  //Checking the quantity of arguments
            throw new UDFArgumentException(getClass().getSimpleName() + " takes only 1 argument!");
        }
        address = (StringObjectInspector) args[0]; // IP is a String, so use StringObjectInspector

        // Describing the structure for output
        // Setting collumns name. Output table will have 3 collumns
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("ip");
        fieldNames.add("octet_num");
        fieldNames.add("octet_value");

        List<ObjectInspector> fieldInspectors = new ArrayList<ObjectInspector>();
        fieldInspectors.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
        fieldInspectors.add(PrimitiveObjectInspectorFactory.javaIntObjectInspector);
        fieldInspectors.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);

        return ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, fieldInspectors);
    }

    @Override
    public void process(Object[] objects) throws HiveException {
        // UDTF has 1 argument, hence `objects` has a single element too
        String ip = address.getPrimitiveJavaObject(objects[0]);
        forwardObjArray[0] = ip;

        if (ip == null) {
            forwardObjArray[1] = -1;
            forwardObjArray[2] = null;
            forward(forwardObjArray);
        } else {
            int i = 0;
            for (String octet: ip.split("\\.")) {
                forwardObjArray[1] = i++;
                forwardObjArray[2] = octet;
                forward(forwardObjArray);
            }
        }
    }

    @Override
    public void close() throws HiveException {
    }
}
