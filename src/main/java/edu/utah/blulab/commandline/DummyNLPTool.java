package edu.utah.blulab.commandline;

import java.io.File;

public class DummyNLPTool {
    
	
	public DummyNLPTool() {
		
	}
	
	public String getResults() {
		String results = null;
		File file = Utilities.getResourceFile(IevizCmd.class, "FakeReport1.txt.knowtator.xml");
		if (file != null && file.exists()) {
			results = Utilities.readFile(file);
		}
		return results;
	}

}
