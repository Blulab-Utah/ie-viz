package edu.utah.blulab.commandline;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;

public class Utilities {

    public static File getResourceFile(Class c, String fname) {
        File file = null;
        try {
			URL url = c.getClassLoader().getResource(fname);
			if (url != null) {
			    file = new File(url.getFile());
			} 
			if (file == null || !file.exists()) {
				String current = new java.io.File(".").getCanonicalPath();
				String fpath = current + File.separatorChar + fname;
			    file = new File(fpath);
			}
		} catch (IOException e) {
		}
        return file;
    }

    public static String convertStreamToString(final InputStream in) throws IOException {
        InputStreamReader read = new InputStreamReader(in);
        // String line;
        // int let = 0;
        StringWriter swr = new StringWriter();
        char[] byt = new char[1024];
        int len = read.read(byt);
        while (len > 0) {
            swr.write(byt, 0, len);
            len = read.read(byt);
        }
        StringBuffer sb = swr.getBuffer();
        return sb.toString();
    }
}
