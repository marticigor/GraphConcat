package building_blocks;

import java.io.File;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.util.*;

public class OutputXml {

	private Document outputDocument;
	private String filenameOutput = "not_defined.gpx";
	private String trackName = "not_defined";
	private static final String URI = "http://www.topografix.com/GPX/1/1";

	private List<Trackpoint> track;

	private Element mainRootElement;
	private Element nameElement;
	private Element trackElement;
	private Element trackSeqElement;
	private Element trackpointElement;

	public OutputXml(List<Trackpoint> track, String trackName, String filenameOutput) {
		this.track = track;
		this.trackName = trackName;
		this.filenameOutput = filenameOutput;
	}
	
	/**
	 *
	 */
	public void composeOutputDoc() throws ParserConfigurationException {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		dbf.setNamespaceAware(true);
		dbf.setValidating(true);

		DocumentBuilder db = dbf.newDocumentBuilder();

		outputDocument = db.newDocument();

		mainRootElement = outputDocument.createElementNS(URI, "gpx");

		mainRootElement.setAttribute("xmlns", "http://www.topografix.com/GPX/1/1");
		mainRootElement.setAttribute("creator", "experimental.program");
		mainRootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		mainRootElement.setAttribute("xsi:schemaLocation",
				"http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensions/v3/GpxExtensionsv3.xsd http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd");

		trackElement = outputDocument.createElementNS(URI, "trk");

		nameElement = outputDocument.createElementNS(URI, "name");
		nameElement.setTextContent(trackName);

		trackSeqElement = outputDocument.createElementNS(URI, "trkseg");

		outputDocument.appendChild(mainRootElement);
		mainRootElement.appendChild(trackElement);
		trackElement.appendChild(nameElement);
		trackElement.appendChild(trackSeqElement);

		for (Trackpoint trackpoint : track) {

			trackpointElement = outputDocument.createElementNS(URI, "trkpt");

			trackpointElement.setAttribute("lat", trackpoint.getLat());
			trackpointElement.setAttribute("lon", trackpoint.getLon());

			trackSeqElement.appendChild(trackpointElement);

		}
	}

	/**
	*
	*/
	public void writeOutputFile() throws TransformerException {
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(outputDocument);

		StreamResult result = new StreamResult(new File(filenameOutput));

		transformer.transform(source, result);

	}
}
