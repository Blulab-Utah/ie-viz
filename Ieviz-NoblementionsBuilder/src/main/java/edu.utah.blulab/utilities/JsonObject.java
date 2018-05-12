package edu.utah.blulab.utilities;

import java.util.ArrayList;
import java.util.List;

public class JsonObject {

	private final List<JsonPair> pairs = new ArrayList<JsonPair>();

	public void addPair(JsonPair pair) {
		this.pairs.add(pair);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{");
		for (JsonPair pair : pairs) {
			str.append(pair.toString()).append(",");
		}
		str.setLength(str.length() - 1);
		str.append("}");
		return str.toString();
	}
}
