package edu.utah.blulab.utilities;

import java.util.ArrayList;
import java.util.List;

public class ArrayOfJson {

	private List<JsonObject> objects = new ArrayList<JsonObject>();

	public List<JsonObject> getObjects() {
		return objects;
	}

	public void setObjects(List<JsonObject> objects) {
		this.objects = objects;
	}

	public void addObject(JsonObject object) {
		this.objects.add(object);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("[");
		for (JsonObject object : objects) {
			str.append(object.toString()).append(",");
		}
		str.setLength(str.length() - 1);
		str.append("]");
		return str.toString();
	}

}
