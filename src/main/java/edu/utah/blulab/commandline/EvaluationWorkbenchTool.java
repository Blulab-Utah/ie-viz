package edu.utah.blulab.commandline;

import edu.utah.blulab.evaluationworkbenchmanager.EvaluationWorkbenchManager;
import workbench.api.gui.WBGUI;

public class EvaluationWorkbenchTool {
	private WBGUI workbench = null;
	
	public EvaluationWorkbenchTool() {
		startupWorkbench();
	}
	
	public boolean startupWorkbench() {
		try {
			EvaluationWorkbenchManager.doTest();
			this.workbench = WBGUI.WorkbenchGUI;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
}
