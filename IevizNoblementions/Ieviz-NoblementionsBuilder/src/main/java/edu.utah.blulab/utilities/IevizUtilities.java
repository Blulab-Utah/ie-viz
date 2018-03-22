package edu.utah.blulab.utilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class IevizUtilities {

    public static boolean isNullOrEmpty(String feature) {
        return (null == feature) || (feature.isEmpty());
    }

    public static boolean isNullOrEmpty(Collection<?> value) {
        return value == null || value.isEmpty();
    }



}
