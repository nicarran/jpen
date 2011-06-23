/* [{
Copyright 2010 Nicolas Carranza <nicarran at gmail.com>

This file is part of jpen.

jpen is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License,
or (at your option) any later version.

jpen is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with jpen.  If not, see <http://www.gnu.org/licenses/>.
}] */
package jpen.internal.filter;

import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

final class LogToFileRule
	implements RelativeLocationFilter.Rule{
	private static final Logger L=Logger.getLogger(LogToFileRule.class.getName());
	//static { L.setLevel(Level.ALL); }

	private int logFileStamp=0;
	private List<Record> records;

	static class Record{
		final RelativeLocationFilter.SamplePoint samplePoint;
		final Point2D.Float reference;
		final Point2D.Float deviation;
		Record(RelativeLocationFilter filter){
			this.samplePoint=filter.samplePoint.clone();
			this.reference=new Point2D.Float();
			this.reference.setLocation(filter.reference);
			this.deviation=new Point2D.Float();
			this.deviation.setLocation(filter.deviation);
		}
	}

	//@Override
	public void reset(){
		if(records==null){
			logFileStamp++;
			records=new ArrayList<Record>();
		}else
			records.clear();
	}

	//@Override
	public RelativeLocationFilter.State evalFilterNextState(RelativeLocationFilter filter){
		if(records==null)
			return null;
		if(records.size()>=100){
			writeRecords();
			records=null;
			return null;
		}
		records.add(new Record(filter));
		return RelativeLocationFilter.State.UNDEFINED;
	}

	private void writeRecords(){
		try{
			StringBuilder filePath=new StringBuilder();
			filePath.append("RelativeLocationFilter-");
			filePath.append(logFileStamp);
			filePath.append(".txt");
			Writer writer=new BufferedWriter(new FileWriter(new File(filePath.toString())));
			for(int i=0; i<records.size(); i++){
				Record record=records.get(i);
				writer.write(Integer.valueOf(i+1).toString());
				writer.write(", reference=");
				writePoint(writer, record.samplePoint, record.reference.x, record.reference.y);
				writer.write(", sample=");
				writePoint(writer, record.samplePoint,
									 record.samplePoint.levelX==null? 0: record.samplePoint.levelX.value,
									 record.samplePoint.levelY==null? 0: record.samplePoint.levelY.value);
				writer.write(", deviation=");
				writePoint(writer, record.samplePoint, record.deviation.x, record.deviation.y);
				writer.write("\n");
			}
			writer.close();
			L.info("written: "+filePath);
		}catch(IOException ex){
			throw new AssertionError(ex);
		}
	}

	private void writePoint(Writer writer, RelativeLocationFilter.SamplePoint samplePoint, float x, float y)
	throws IOException{
		writer.write("(");
		if(samplePoint.levelX!=null)
			writeValue(writer, x);
		else
			writer.write("??");
		writer.write(", ");
		if(samplePoint.levelY!=null)
			writeValue(writer, y);
		else
			writer.write("??");
		writer.write(")");
	}

	private static final DecimalFormat decimalFormat=new DecimalFormat("###0.0");

	private void writeValue(Writer writer, float value) throws IOException{
		writer.write(decimalFormat.format(value));
	}
}