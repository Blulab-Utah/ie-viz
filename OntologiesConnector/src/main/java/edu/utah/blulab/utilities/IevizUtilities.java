package edu.utah.blulab.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class IevizUtilities {


    public static List<String> getOntologyNames(String directory) throws FileNotFoundException {
        List<String> results = new ArrayList<String>();

        Scanner sc = new Scanner(new File(directory));
        while(sc.hasNextLine()){
            String fileNameWithExtension = sc.nextLine();
            results.add(FilenameUtils.removeExtension(fileNameWithExtension));
        }
        return results;
    }

    public static String getOntologyContent(String ontologyName) throws IOException {

        String filename = "C:\\Users\\Deep\\Documents\\noble\\test\\ont\\"+ontologyName+".owl";
        return FileUtils.readFileToString(new File(filename),"UTF-8");

    }
}
