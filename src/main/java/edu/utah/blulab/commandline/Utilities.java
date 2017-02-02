package edu.utah.blulab.commandline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Vector;

import tsl.utilities.VUtils;

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

    public static String readFile(File file) {
        StringBuffer sb = new StringBuffer();
        try {
            if (file != null && file.exists()) {
                BufferedReader read = new BufferedReader(new FileReader(file));
                StringWriter swr = new StringWriter();
                char[] byt = new char[1024];
                int len = read.read(byt);
                while (len > 0) {
                    swr.write(byt, 0, len);
                    len = read.read(byt);
                }
                sb = swr.getBuffer();
                read.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    
    public static Vector<File> readFilesFromDirectory(String dname) {
		Vector<File> v = null;
		if (dname != null) {
			File sourcedir = new File(dname);
			if (sourcedir != null && sourcedir.exists() && sourcedir.isDirectory()) {
				v = readFilesFromDirectory(sourcedir);
			}
		}
		return v;
	}
    
    public static Vector<File> readFilesFromDirectory(File sourcedir) {
		Vector<File> v = null;
		if (sourcedir != null && sourcedir.exists()) {
			File[] files = sourcedir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isFile() && file.getName().charAt(0) != '.') {
					v = VUtils.add(v, file);
				} else if (file.isDirectory()) {
					v = VUtils.append(v, readFilesFromDirectory(file));
				}
			}
		}
		return v;
	}

}
