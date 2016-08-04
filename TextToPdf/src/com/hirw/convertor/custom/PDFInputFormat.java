package com.hirw.convertor.custom;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

// the file splits for this class has to be the whole file
// (it cannot be divided); the output will be <file_name, PDFWritable>
// Note -> PDFWritable is a customer writable, not an internal
// Hadoop object
public class PDFInputFormat extends FileInputFormat<Text, PDFWritable> {

	private static final Log log = LogFactory.getLog(PDFInputFormat.class);

	
	// this is the very first thing to do:
	// the file splits size is, for example, 128 MB
	// if we have a file with a size of 170 MB
	// then it will be processed by two different
	// mappers, and this will come up with two different 
	// files, and this is not acceptable
	// this method return false everytime, so the
	// file will not be split
	@Override
	protected boolean isSplitable(JobContext context, Path filename) {
		return false;
	}

	// every mapper implement a RecordReader method, which
	// is responsible for creating the <key,value> output;
	// in our case the output will be
	// <file_name, PDFWritable>
	@Override
	public RecordReader<Text, PDFWritable> createRecordReader(
			InputSplit inputSplit, TaskAttemptContext context) throws IOException,
			InterruptedException {
		PDFRecordReader reader = new PDFRecordReader();
		reader.initialize(inputSplit, context);
		return reader;
	}

	public class PDFRecordReader extends
	RecordReader<Text, PDFWritable> {

		private FileSplit split;
		private Configuration conf;

		private Text currKey = null;
		private PDFWritable currValue = null;
		private boolean fileProcessed = false;

		// remember that we are not splitting the file
		// so here the split is the whole file
		@Override
		public void initialize(InputSplit split, TaskAttemptContext context)
				throws IOException, InterruptedException {
			this.split = (FileSplit)split;
			this.conf = context.getConfiguration();
		}

		// in this method we are assigning the <key,value> values
		// 
		@Override
		public boolean nextKeyValue() throws IOException, InterruptedException {     
			
			if ( fileProcessed ){
				return false;
			}

			int fileLength = (int)split.getLength();
			byte [] result = new byte[fileLength];

			FileSystem  fs = FileSystem.get(conf);
			FSDataInputStream in = null; 
			try {
				// open the file
				in = fs.open( split.getPath());
				
				// read the whole content
				IOUtils.readFully(in, result, 0, fileLength);
				   
			} finally {
				IOUtils.closeStream(in);
			}
			this.fileProcessed = true;

			Path file = split.getPath();
			// assign the key
			this.currKey = new Text(file.getName());
			// assign the value
			this.currValue = new PDFWritable(result);
			
			return true;
		}

		@Override
		public Text getCurrentKey() throws IOException,
		InterruptedException {
			return currKey;
		}

		@Override
		public PDFWritable getCurrentValue() throws IOException,
		InterruptedException {
			return currValue;
		}

		@Override
		public float getProgress() throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void close() throws IOException {
			// nothing to close
		}

	}        

}