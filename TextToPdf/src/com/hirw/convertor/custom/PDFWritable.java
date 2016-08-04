package com.hirw.convertor.custom;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

// remember that the main goal of a
// Writable object is to serialize
// an object in an efficient and simple manner
// in order to transmit trough the nodes
public class PDFWritable implements Writable {

	private static final Log log = LogFactory.getLog(PDFWritable.class);
	
	private byte[] bytes;
	public PdfReader reader = null;


	public PDFWritable(){

	}

	public PDFWritable(byte[] bytes){
		this.bytes = bytes;
	}


	// this method read what the "write" method
	// write to the stream
	
	//Read PDFWritable from stream
	@Override
	public void readFields(DataInput in) throws IOException {
		
		// we read the bytes of the "in" input
		int newlength = WritableUtils.readVInt(in);
		bytes = new byte[newlength];
		in.readFully(bytes, 0, newlength);
		
		// then we use them to initialize
		// a pdf reader
		try{
			reader = new PdfReader(bytes);
		}
		catch(Exception e){
			log.error("PDFWritable readFields - " +getStackTrace(e));
		}
	}

	//Read PDFWritable to stream
	@Override
	public void write(DataOutput out) throws IOException {

	Document document = new Document(PageSize.LETTER, 40, 40, 40, 40);

	try{
		// we are simply creating (by using an external library)
		// a pdf document, filling the content with the input
		// from the stream
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PdfWriter.getInstance(document, output);
		document.open();

		Paragraph p = new Paragraph(new String(bytes));
		document.add(p);
		
        document.close();
		WritableUtils.writeVInt(out, output.size());
		
		// with this command we are writing the document
		// to the stream
		out.write(output.toByteArray(), 0, output.size());
		
	}
	catch(Exception e){
		log.error("PDFWritable Write - "+ getStackTrace(e));

	}
	}
	
	private String getStackTrace(final Throwable throwable) {
	     final StringWriter sw = new StringWriter();
	     final PrintWriter pw = new PrintWriter(sw, true);
	     throwable.printStackTrace(pw);
	     return sw.getBuffer().toString();
	}
}