package edu.utah.blulab.controller;

import edu.utah.blulab.constants.ServiceConstants;
import edu.utah.blulab.db.models.AnnotationResultsDao;
import edu.utah.blulab.db.models.DocumentIdentifierDao;
import edu.utah.blulab.db.query.QueryUtility;
import edu.utah.blulab.services.INoblementionsConnector;
import edu.utah.blulab.utilities.Converters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


@Controller
public class NoblementionsConnectionController {

    private static final Logger LOGGER = Logger.getLogger(NoblementionsConnectionController.class);

    @Autowired
    private View jsonView;

    @RequestMapping(value = "/uploadMultiplePath", method = RequestMethod.POST)
    public ModelAndView getFeatures(@RequestParam("OntologyPath") String ontPath,
                                    @RequestParam("Input") String input,
                                    @RequestParam("Output") String output) throws Exception {



        final URL url = new URL("http://localhost:8080/Ieviz-NoblementionsWS/getAnnotations");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json");

        connection.setRequestProperty("Input", input);
        connection.addRequestProperty("Output", output);
        connection.addRequestProperty("OntologyPath", ontPath);

        connection.setDoOutput(true);

        OutputStream os = connection.getOutputStream();
        os.write(input.getBytes());
        os.flush();


        if (connection.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + connection.getResponseCode());
        }

        String contents = FileUtils.readFileToString(new File(output + "\\RESULTS.tsv"));
        String contentsToCsv = Converters.tsvToCsv(contents);

        Scanner scanner = new Scanner(contentsToCsv);
        List<String> linesList = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            linesList.add(line);

        }
        scanner.close();

        linesList.remove(0);
        DocumentIdentifierDao doc = new DocumentIdentifierDao();
        List<String> documentList = new ArrayList<>();


        for (String element : linesList) {
            List<String> objectList = Arrays.asList(element.split(",", -1));
            LOGGER.debug("\nObject Size: " + objectList.size());

            String documentName = objectList.get(0);
            if (!documentList.contains(documentName)) {
                documentList.add(documentName);
                doc.setDocName(documentName);
                QueryUtility.insertDocumentId(doc);
            }

            int id = QueryUtility.getID(documentName);
            if(objectList.size() == 8)
            {
                AnnotationResultsDao entity = new AnnotationResultsDao();
                entity.setDocumentId(id);
                entity.setDocumentType(objectList.get(1));
                entity.setId(objectList.get(2));
                entity.setAnnotationVariable(objectList.get(3));
                entity.setProperty(objectList.get(4));
                entity.setDocumentValue(objectList.get(5));
                entity.setValueProperties(objectList.get(6));
                entity.setAnnotations(objectList.get(7));

                QueryUtility.insertAnnotataions(entity);
            }
        }


        String contentToJson = Converters.tsvToJson(contents);

        return new ModelAndView(jsonView, ServiceConstants.STATUS_FIELD, contentToJson);
    }
}
