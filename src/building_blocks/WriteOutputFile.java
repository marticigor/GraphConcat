package building_blocks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;

import entity.NodeEntity;

public class WriteOutputFile {

	private final static String EXTENSION = ".graph";
	private String path,name;
	private Set<NodeEntity> dataSet;
	
	public WriteOutputFile(String thePath, String theName, Set<NodeEntity> theDataSet){
		this.path = thePath;
		this.name = theName;
		this.dataSet = theDataSet;
	}
	
	public void write() throws IOException{
		
		File output = new File(path + File.separator + name + EXTENSION);

    	FileOutputStream fos = new FileOutputStream(output); 
    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
     
    		for(NodeEntity ne : dataSet){
					bw.write(ne.toString());
					bw.newLine();
    		}
    	
    	bw.close();
    	fos.close();
	}
	
}
