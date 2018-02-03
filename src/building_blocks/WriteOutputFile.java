package building_blocks;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import core.App;
import entity.NodeEntity;
import utils.FormatDouble;
import utils.Haversine;

//metadata file format:
//528|174|305|49.8890337397|50.1461788264|14.2016829361|14.7505701889
//222203|716776

public class WriteOutputFile {

	public final static String EXTENSION_META = ".txt";
	public final static String EXTENSION_DATA = ".DAT";
	public final static String SEP = "|";
	private String path, project;
	private List<NodeEntity> dataSet; // sorted by id
	private Graph graph; // ask him about sizes already computed;
	private App app; // ask him about metadata already computed;

	public WriteOutputFile(String thePath, String theName, List<NodeEntity> theDataSet, Graph theGraph, App theApp) {
		this.path = thePath;
		this.project = theName;
		this.dataSet = theDataSet;
		this.graph = theGraph;
		this.app = theApp;
	}

	public void write() throws IOException {

		String directoryName = path.concat(File.separator).concat(project);
		File directory = new File(directoryName);
		if (!directory.exists()) {
			directory.mkdir();
			// If you require it to make the entire directory path including
			// parents,
			// use directory.mkdirs(); here instead.
		}

		String[] dataFiles = new String[] { "_nodes_latD", "_nodes_lonD", "_nodes_elevS", "_nodes_weightB",
				"_nodes_clusterI", "_edges_fromI", "_edges_toI", "_edges_lenghtD", "_metadata" };

		File outputMetadata = new File(directoryName + File.separator + project + dataFiles[8] + EXTENSION_META);

		FileOutputStream fos = new FileOutputStream(outputMetadata);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		System.out.println("\n\nWriting into directory: " + directoryName);
		System.out.println("Writing metadata: " + outputMetadata.toString());
		// write metadata
		String metadata = app.maxElev + SEP + app.minElev + SEP + app.elevAvg + SEP
				+ FormatDouble.formatDouble10(app.minLat) + SEP + FormatDouble.formatDouble10(app.maxLat) + SEP
				+ FormatDouble.formatDouble10(app.minLon) + SEP + FormatDouble.formatDouble10(app.maxLon);
		bw.write(metadata);
		bw.newLine();
		// write graphStats
		String stats = graph.getDatasetSize() + SEP + graph.getEdgeSizeAfterPrune();
		bw.write(stats);

		bw.close();
		fos.close();

		// build nodes data

		List<Integer> clusterIds = new LinkedList<Integer>();
		List<Short> elevs = new LinkedList<Short>();
		List<Byte> weights = new LinkedList<Byte>();

		List<Double> lats = new LinkedList<Double>();
		List<Double> lons = new LinkedList<Double>();

		int c = 0;
		for (NodeEntity ne : dataSet) {
			if (c < 20)
				System.out.println("\n");
			clusterIds.add((int) ne.getIdCLuster());
			if (c < 20)
				System.out.print("clusterId: " + ne.getIdCLuster());
			elevs.add(ne.getElev());
			if (c < 20)
				System.out.print(" elev: " + ne.getElev());
			weights.add((byte) ne.getWeight());
			if (c < 20)
				System.out.print(" weight:" + ne.getWeight());
			lats.add(ne.getLat());
			if (c < 20)
				System.out.print(" lat:" + ne.getLat());
			lons.add(ne.getLon());
			if (c < 20)
				System.out.print(" lon:" + ne.getLon());
			c++;
		}
		
		System.out.println("\n");

		// build edges data

		List<Integer> froms = new LinkedList<Integer>();
		List<Integer> tos = new LinkedList<Integer>();
		List<Double> dists = new LinkedList<Double>();

		int assertionCounter = 0;
		double distance = 0.0;
		for (NodeEntity currentNode : dataSet) {
			for (NodeEntity adj : currentNode.getAdjacents()) {
				distance = Haversine.haversineInM(currentNode.getLat(), currentNode.getLon(), adj.getLat(),
						adj.getLon());
				dists.add(distance);
				froms.add((int) currentNode.getId());
				tos.add((int) adj.getId());
				assertionCounter++;
			}
		}
		assert (graph.getEdgeSizeAfterPrune() == assertionCounter);

		String dirSepProj = directoryName + File.separator + project;

		try {

			writeData(new File(dirSepProj + dataFiles[4] + EXTENSION_DATA), clusterIds);
			writeData(new File(dirSepProj + dataFiles[2] + EXTENSION_DATA), elevs);
			writeData(new File(dirSepProj + dataFiles[3] + EXTENSION_DATA), weights);
			writeData(new File(dirSepProj + dataFiles[0] + EXTENSION_DATA), lats);
			writeData(new File(dirSepProj + dataFiles[1] + EXTENSION_DATA), lons);

			writeData(new File(dirSepProj + dataFiles[5] + EXTENSION_DATA), froms);
			writeData(new File(dirSepProj + dataFiles[6] + EXTENSION_DATA), tos);
			writeData(new File(dirSepProj + dataFiles[7] + EXTENSION_DATA), dists);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// write

	private <T extends Number> void writeData(File file, List<T> data) throws IOException {
		System.out.println("WriteData, writing " + file.toString());

		if (data.size() == 0)
			throw new RuntimeException("ZERO");

		int bufferSize = 8 * 1024;

		OutputStream output = new BufferedOutputStream(new FileOutputStream(file), bufferSize);

		byte[] bytes;

		if (data.get(0).getClass() == Integer.class) {
			for (T t : data) {
				bytes = ByteBuffer.allocate(4).putInt((int) t).array();
				output.write(bytes);
			}
		} else if (data.get(0).getClass() == Short.class) {
			for (T t : data) {
				bytes = ByteBuffer.allocate(2).putShort((short) t).array();
				output.write(bytes);
			}
		} else if (data.get(0).getClass() == Byte.class) {
			for (T t : data) {
				output.write(new Byte((byte) t).intValue());
			}
		} else if (data.get(0).getClass() == Double.class) {
			for (T t : data) {
				bytes = ByteBuffer.allocate(8).putDouble((double) t).array();
				output.write(bytes);
			}
		}
		output.close();
	}
}
