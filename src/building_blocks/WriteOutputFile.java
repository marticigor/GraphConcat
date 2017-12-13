package building_blocks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import entity.NodeEntity;

public class WriteOutputFile {

	private final static String EXTENSION = ".graph";
	private String path,name;
	private List<NodeEntity> dataSet;
	private Graph creator;//ask him questions about sizes already computed;
	
	public WriteOutputFile(String thePath, String theName, List <NodeEntity> theDataSet, Graph theCreator){
		this.path = thePath;
		this.name = theName;
		this.dataSet = theDataSet;
		this.creator = theCreator;
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
