package edu.utah.blulab.domainontology;

import java.util.ArrayList;
import java.util.HashMap;

public class Modifier {
	private String modName, uri, prefCUI;
	private ArrayList<LexicalItem> items;
	private HashMap<String, ArrayList<LexicalItem>> closures;
	public String getModName() {
		return modName;
	}
	public void setModName(String modName) {
		this.modName = modName;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getPrefCUI() {
		return prefCUI;
	}
	public void setPrefCUI(String prefCUI) {
		this.prefCUI = prefCUI;
	}
	public ArrayList<LexicalItem> getItems() {
		return items;
	}
	public void setItems(ArrayList<LexicalItem> items) {
		this.items = items;
	}
	public HashMap<String, ArrayList<LexicalItem>> getClosures() {
		return closures;
	}
	public void setClosures(HashMap<String, ArrayList<LexicalItem>> closures) {
		this.closures = closures;
	}
	@Override
	public String toString() {
		return "Modifier [modName=" + modName + ", uri=" + uri + ", prefCUI="
				+ prefCUI + ", items=" + items + ", closures=" + closures + "]";
	}
	
	
	
}
