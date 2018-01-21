package building_blocks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import core.App;
import entity.NodeEntity;
import utils.FormatDouble;
import utils.Haversine;

// graph format
// https://www.dropbox.com/s/cpaidvxzisyic4d/2017-12-30%2021.54.47.jpg?dl=0

public class WriteOutputFile {

	public final static String EXTENSION = ".graph";
	public final static String SEP = "|";
	private String path, name;
	private List<NodeEntity> dataSet; // sorted by id
	private Graph graph; // ask him about sizes already computed;
	private App app; // ask him about metadata already computed;

	public WriteOutputFile(String thePath, String theName, List<NodeEntity> theDataSet, Graph theGraph, App theApp) {
		this.path = thePath;
		this.name = theName;
		this.dataSet = theDataSet;
		this.graph = theGraph;
		this.app = theApp;
	}

	public void write() throws IOException {

		File output = new File(path + File.separator + name + EXTENSION);

		FileOutputStream fos = new FileOutputStream(output);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

		// write metadata
		String metadata = app.maxElev + SEP + app.minElev + SEP + app.elevAvg + SEP
				+ FormatDouble.formatDouble10(app.minLat) + SEP + FormatDouble.formatDouble10(app.maxLat) + SEP
				+ FormatDouble.formatDouble10(app.minLon) + SEP + FormatDouble.formatDouble10(app.maxLon);
		bw.write(metadata);
		bw.newLine();
		// write graphStats
		String stats = graph.getDatasetSize() + SEP + graph.getEdgeSizeAfterPrune();
		bw.write(stats);
		bw.newLine();

		int size = graph.getDatasetSize();

		assert (size == dataSet.size());

		NodeEntity current = null;
		// write node data
		long currentId = 0;
		for (int id = 1; id <= size; id++) {
			current = dataSet.get(id - 1);
			currentId = current.getId();

			assert ((long) id == currentId);

			// in the final app using Byte will save memory
			assert ((int) current.getWeight() <= (int) Byte.MAX_VALUE);

			bw.write(FormatDouble.formatDouble10(current.getLat()) + SEP + FormatDouble.formatDouble10(current.getLon())
					+ SEP + current.getElev() + SEP + current.getWeight());
			bw.newLine();
		}
		double distance = 0.0;
		String line = "";
		int assertionCounter = 0;
		// write edges data
		for (NodeEntity currentNode : dataSet) {
			for (NodeEntity adj : currentNode.getAdjacents()) {
				distance = Haversine.haversineInM(currentNode.getLat(), currentNode.getLon(), adj.getLat(),
						adj.getLon());
				line = currentNode.getId() + SEP + adj.getId() + SEP + FormatDouble.formatDouble2(distance);
				bw.write(line);
				bw.newLine();
				assertionCounter++;
			}
		}
		assert (graph.getEdgeSizeAfterPrune() == assertionCounter);
		bw.close();
		fos.close();
	}
}
