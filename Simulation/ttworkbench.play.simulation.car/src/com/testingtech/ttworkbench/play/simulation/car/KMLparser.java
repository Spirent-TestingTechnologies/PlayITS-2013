package com.testingtech.ttworkbench.play.simulation.car;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class KMLparser {

	private static ArrayList<GPSposition> parseTxt(File file)
			throws NumberFormatException, IOException {
		ArrayList<GPSposition> positions = new ArrayList<GPSposition>();
		StringBuilder contents = new StringBuilder();
		BufferedReader reader = null;

		reader = new BufferedReader(new FileReader(file));
		String text = null;

		// repeat until all lines are read
		while ((text = reader.readLine()) != null) {
			contents.append(text).append(System.getProperty("line.separator"));

			String[] splitted = text.replace("\t ", "").replace(" ", "")
					.split(",");
			GPSposition gps = new GPSposition(Double.parseDouble(splitted[0]),
					Double.parseDouble(splitted[1]));
			positions.add(gps);
		}
		return positions;
	}

	private static String getExtension(String fileName) {
		String extension = "";
		/*
		 * FIXME We are only assuming basic Windows file and folder structure
		 * with nothing like "foo.tar.gz" or "/home/.foo/bar"
		 */
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			extension = fileName.substring(i + 1);
		}
		return extension;
	}

	public static ArrayList<GPSposition> parseFile(File file)
			throws NumberFormatException, IOException, CannotParseFileException {
		ArrayList<GPSposition> positions = new ArrayList<GPSposition>();
		// get file extension
		String fileExtension = getExtension(file.getName());

		if (fileExtension.equals("txt")) {
			positions = parseTxt(file);
		} else if (fileExtension.equals("KML")) {
			positions = parseKML(file);
		} else {
			throw new CannotParseFileException(file);
		}
		return positions;
	}

	// Works basically just like the txt parser, except it checks for
	// "coordinates" and
	// adds them only, if the KML file might be not in correct format this will
	// not work
	private static ArrayList<GPSposition> parseKML(File file) throws NumberFormatException, IOException {
		ArrayList<GPSposition> positions = new ArrayList<GPSposition>();
		StringBuilder contents = new StringBuilder();
		BufferedReader reader = null;
		boolean open = false;

		reader = new BufferedReader(new FileReader(file));
		String text = null;

		// repeat until all lines are read
		while ((text = reader.readLine()) != null) {

			contents.append(text).append(System.getProperty("line.separator"));
			// check where coordinates begin and end
			if (text.contains("<coordinates>")) {
				open = true;
			} else if (text.contains("</coordinates>")) {
				open = false;
			}
			// replace the not needed parts of the coordinates
			String[] splitted = text.replace("<coordinates>", "")
					.replace("</coordinates>", "").replace("\t ", "")
					.replace(" ", "").split(",");
			// if coordinates begin add the gps positions to the positions array
			if (open) {
				GPSposition gps = new GPSposition(
						Double.parseDouble(splitted[0]),
						Double.parseDouble(splitted[1]));

				positions.add(gps);

			}
		}
		return positions;
	}
}
