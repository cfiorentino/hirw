package com.hirw.maxcloseprice;

/**
 * MaxClosePriceMapper.java
 * www.hadoopinrealworld.com
 * This is a Mapper program to calculate Max Close Price from stock dataset using MapReduce
 */

import java.io.IOException;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

// in oorder to have a mapper, it has to extends the "Mapper" class
// defined in the "org.apache.hadoop.mapreduce.Mapper" jar

// the mapper is call for each input splits
// at least one or more mapper will be called

												//input key   input value  output key   output value
public class MaxClosePriceMapper extends Mapper<LongWritable, Text,        Text,        FloatWritable> {

	// this method is called for each row of the input splits
	
	@Override		// offset from beginning of file    row of file   Hadoop context
	public void map(LongWritable key, 					Text value,   Context context)
			throws IOException, InterruptedException {

		String line = value.toString();
		// the row is a coma separeted value
		String[] items = line.split(",");
		
		// this is the symbol
		String stock = items[1];
		// this is the close price
		Float closePrice = Float.parseFloat(items[6]);
		
		// from now Hadoop takes care of the output
		// in the shuffle phase the results will be grouped by key
		context.write(new Text(stock), new FloatWritable(closePrice));
		
	}
}
