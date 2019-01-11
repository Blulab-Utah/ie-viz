package edu.utah.blulab.services;

import edu.utah.blulab.db.models.FileContentsDao;

import java.io.File;
import java.util.List;

public interface INlpOperationsWebServices {

    String getAnnotationsFromNoblementions(List<FileContentsDao> fileContentsDaoList, List<FileContentsDao> ontologyFilesList);
}
