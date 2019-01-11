package edu.utah.blulab.services;

import edu.utah.blulab.containers.AnnotationContainer;
import edu.utah.blulab.containers.DocumentContainer;
import edu.utah.blulab.db.models.FileContentsDao;
import edu.utah.blulab.db.query.QueryUtility;
import edu.utah.blulab.utilities.Converters;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class NlpOperationsWebServices implements INlpOperationsWebServices {

    private static final Logger logger = Logger.getLogger(NlpOperationsWebServices.class);

    @Override
    public String getAnnotationsFromNoblementions(List<FileContentsDao> fileContentsDaoList,
                                                  List<FileContentsDao> ontologyFilesList) {
        MultiPart multiPart;
        String contents = null;

        try {
            Client client = ClientBuilder.newBuilder().
                    register(MultiPartFeature.class).build();
            WebTarget server = client.target("http://blutc-dev.chpc.utah.edu/NoblementionsWS/getAnnotations");
            multiPart = new MultiPart();
            List<FileDataBodyPart> bodyParts = new ArrayList<>();

//            for(File file:ontologyFiles)
//            {
//                bodyParts.add(new FileDataBodyPart
//                        ("ontFile", file, MediaType.APPLICATION_XML_TYPE));
//            }

            for (FileContentsDao fileContentsDao : ontologyFilesList) {
                File ont = new File(fileContentsDao.getFileName());
                FileUtils.writeStringToFile(ont,
                        fileContentsDao.getInputContent(), "UTF-8");

                bodyParts.add(new FileDataBodyPart
                        ("ontFile", ont, MediaType.TEXT_PLAIN_TYPE));

            }

            for(FileContentsDao fileContentsDao: fileContentsDaoList)
            {
                File inputFile = new File(fileContentsDao.getFileName());
                FileUtils.writeStringToFile(inputFile,
                        fileContentsDao.getInputContent(),"UTF-8");

                bodyParts.add(new FileDataBodyPart
                        ("ip", inputFile, MediaType.TEXT_PLAIN_TYPE));


            }
            for (FileDataBodyPart bp : bodyParts) {
                multiPart.bodyPart(bp);
            }
            Response response = server.request(MediaType.MULTIPART_FORM_DATA_TYPE)
                    .post(Entity.entity(multiPart, "multipart/form-data"));
            if (response.getStatus() == 200) {
                contents = response.readEntity(String.class);
                logger.info(contents);
            } else {
                logger.error("Response is not ok");
            }
        } catch (Exception e) {
            logger.error("Exception has occured " + e.getMessage());
        }

        logger.info(contents);
        String tsvContents = Converters.csvToTsv(contents);

        List<DocumentContainer> docList = QueryUtility.processAnnotatedOutput(tsvContents);

        int runID = QueryUtility.persistRun("RunXXX");
        try {
            for (DocumentContainer doc : docList) {
                QueryUtility.persistAnnotation(doc, runID);
                for (AnnotationContainer annotation : doc.getAnnotations()) {
                    logger.debug(annotation.toString());

                }
            }
        }
        catch (Exception e) {
            e.getMessage();
        }

        return contents;
    }


}
