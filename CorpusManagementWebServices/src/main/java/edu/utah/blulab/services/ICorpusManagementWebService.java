package edu.utah.blulab.services;

import java.io.File;
import java.util.List;

/**
 * Created by Deep on 1/20/2016.
 */
public interface ICorpusManagementWebService {

    String createNewCorpus(String corpusName, List<File> oorpusList);

    List<String> populateCorpusNames();
}
