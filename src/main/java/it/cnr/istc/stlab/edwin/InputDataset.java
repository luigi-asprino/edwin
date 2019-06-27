package it.cnr.istc.stlab.edwin;

import java.io.IOException;

import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputDataset {

	private static Logger logger = LoggerFactory.getLogger(InputDataset.class);

	private static InputDataset instance;
	private static HDT hdt;
	private String hdtFilePath;

	private InputDataset(String hdtFilePath) throws IOException {
		this.hdtFilePath = hdtFilePath;

		logger.info("Mapping HDT {}", hdtFilePath);
		hdt = HDTManager.mapIndexedHDT(this.hdtFilePath, null);
		logger.info("HDT mapped");

	}

	public static InputDataset getInstance(String hdtPath) throws IOException {
		if (instance == null) {
			instance = new InputDataset(hdtPath);
		}
		return instance;
	}

	public HDT getHDT() {
		return hdt;
	}

}
