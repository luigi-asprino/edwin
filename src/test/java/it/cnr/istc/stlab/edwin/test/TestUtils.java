package it.cnr.istc.stlab.edwin.test;

import java.io.File;
import java.io.IOException;

public class TestUtils {

	static void clean() {
		try {
			System.out.println("\n\n\n\nCLEAN Test Resource Folder\n\n\n\n");
			org.apache.commons.io.FileUtils.deleteDirectory(new File("src/main/resources/testResources/ESGs"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
