package edu.utah.blulab.services;
/**
 * Created by Deep on 1/20/2016.
 */


import edu.utah.blulab.db.models.CorpusMetadataDao;
import edu.utah.blulab.db.models.FileContentsDao;
import edu.utah.blulab.db.models.MongoElementsDao;
import edu.utah.blulab.db.mongo.MongoOperations;
import edu.utah.blulab.db.query.QueryUtility;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Deep
 * @version 1.0.0
 */
@Service
public class CorpusManagementWebService implements ICorpusManagementWebService {

    private Logger logger = Logger.getLogger(CorpusManagementWebService.class);


    @Override
    public String createNewCorpus(String corpusName, List<File> corpusList) {

        for (File individualFile : corpusList) {
            FileContentsDao contentsDao = new FileContentsDao();
            try {
                contentsDao.setInputContent(FileUtils.readFileToString(individualFile));
                contentsDao.setCorpusName(corpusName);
                contentsDao.setFileName(individualFile.getName());

            } catch (IOException e) {
                return e.getMessage();
            }
            MongoElementsDao elements = null;
            try {
                elements = MongoOperations.storeInputData(contentsDao);
            } catch (Exception e) {
                return e.getMessage();
            }

            CorpusMetadataDao corpusMetadata = new CorpusMetadataDao();
            corpusMetadata.setCorpusName(corpusName);
            corpusMetadata.setFileObjId(elements.getId());
            corpusMetadata.setTimestamp(elements.getDate());
            corpusMetadata.setFileName(individualFile.getName());


            boolean status;
            try {
                status = QueryUtility.insertCorpusMetadata(corpusMetadata);
            } catch (Exception e) {
                return e.getMessage();
            }
            if (!status) {
                return "Corpus creation/ update failed";
            }
        }


        return "Corpus creation/ update successful";
    }

    @Override
    public List<String> populateCorpusNames() {

        return QueryUtility.getCorpus();
    }


}
