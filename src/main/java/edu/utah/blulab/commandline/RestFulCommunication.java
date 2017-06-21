package edu.utah.blulab.commandline;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import tsl.utilities.StrUtils;

public class RestFulCommunication {

	private final String USER_AGENT = "Mozilla/5.0";

	public static void main(String[] args) throws Exception {
		// String url = args[0];

		// TEST
		String url = "http://0.0.0.0:32812/MoonstoneServlet2/MoonstoneServlet2";
		String text = "daughter helps the patient daily with chores";

		RestFulCommunication http = new RestFulCommunication();
		http.sendPost(url, text, null);
	}

	// HTTP POST request
	public String sendPost(String url, String text, String ontologyPath) throws Exception {
		String urlParameters = "";
		URL obj = new URL(url);
		java.net.HttpURLConnection con = (java.net.HttpURLConnection) obj.openConnection();
		// add request header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		if (text != null) {
			String htext = StrUtils.textToHtml(text);
			urlParameters += "text=\"" + htext + "\"";
		}
		if (ontologyPath != null) {
			if (urlParameters.length() > 1) {
				urlParameters += "&";
			}
			urlParameters += "ontology=\"" + ontologyPath + "\"";
		}
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());

		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
		int responseCode = con.getResponseCode();

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}

}
