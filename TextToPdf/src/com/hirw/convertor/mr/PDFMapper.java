package com.hirw.convertor.mr;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.hirw.convertor.custom.PDFWritable;


public class PDFMapper extends Mapper<Text, PDFWritable, Text, PDFWritable>{

	private static final Log log = LogFactory.getLog(PDFMapper.class);
	String dirName = null;
	String fileName = null;

	// the mapper in this case is really simple:
	// we have already (in PDFWritable class) read the
	// stream and get the pdf file
	
	@Override
	public void map(Text key, PDFWritable value, Context context) 
			throws IOException, InterruptedException {
		try{
			// so we only need to emit the <key,value>
			context.write(key, value);
		}
		catch(Exception e){
			log.info(e);
		}
	}
}