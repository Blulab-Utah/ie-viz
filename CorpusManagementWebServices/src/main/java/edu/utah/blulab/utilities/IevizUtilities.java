package edu.utah.blulab.utilities;


import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Deep on 3/15/2016.
 */
public class IevizUtilities {

    private static Logger logger = Logger.getLogger(IevizUtilities.class);

    /**
     * @param feature
     * @return
     */
    public static boolean isNullOrEmpty(String feature) {
        return (null == feature) || (feature.isEmpty());
    }

    public static List<File> getRawFileList(MultipartFile[] files) {
        List<File> rawFileList = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                if (file.getOriginalFilename().split("\\.")[1].equals("txt")) {
                    File rawFile = new File(file.getOriginalFilename());
                    try {
                        file.transferTo(rawFile);
                        rawFileList.add(rawFile);

                    } catch (IOException e) {
                        return null;
                    }
                }
            }
        }
        return rawFileList;
    }

    public static List<File> getRawOntList(MultipartFile[] files) {
        List<File> rawFileList = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                if (file.getOriginalFilename().split("\\.")[1].equals("owl")) {
                    File rawFile = new File(file.getOriginalFilename());
                    try {
                        file.transferTo(rawFile);
                        rawFileList.add(rawFile);

                    } catch (IOException e) {
                        return null;
                    }
                }
            }
        }
        return rawFileList;
    }

    public static List<File> getOntologyFileList(MultipartFile[] files) {
        List<File> rawFileList = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                if (file.getOriginalFilename().split("\\.")[1].equals("owl")) {
                    File rawFile = new File(file.getOriginalFilename());
                    try {
                        file.transferTo(rawFile);
                        rawFileList.add(rawFile);

                    } catch (IOException e) {
                        return null;
                    }
                }
            }
        }
        return rawFileList;
    }
}
