package edu.utah.blulab.controller;

import edu.utah.blulab.constants.ServiceConstants;
import edu.utah.blulab.services.INoblementionsConnector;
import edu.utah.blulab.utilities.Converters;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


@Controller
public class NoblementionsConnectionController {

    private static final Logger LOGGER = Logger.getLogger(NoblementionsConnectionController.class);

    @Autowired
    private INoblementionsConnector noblementionsConnector;
    @Autowired
    private View jsonView;

    @RequestMapping(value = "/getAnnotations", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ModelAndView getFeatures(@RequestHeader(value = "Input") String input,
                              @RequestHeader(value = "Output") String output,
                              @RequestHeader(value = "OntologyPath") String ontPath) throws Exception {


        Map<String, String> pathMap = new HashMap<>();
        pathMap.put("ont", ontPath);
        pathMap.put("input", input);
        pathMap.put("output", output);

        LOGGER.debug("\nSending request to Noblementions\n");

        noblementionsConnector.processNobleMentions(pathMap);

        LOGGER.debug("\nReading contents from Noblementions\n");
        String contents = FileUtils.readFileToString(new File(output + "\\RESULTS.tsv"));
//        return Converters.tsvToCsv(contents);

//        Scanner scanner = new Scanner(contentsToCsv);
//        List<String> linesList = new ArrayList<>();
//        while (scanner.hasNextLine()) {
//            String line = scanner.nextLine();
//            linesList.add(line);
//
//        }
//
//        scanner.close();
//
//        linesList.remove(0);
//        DocumentIdentifierDao doc = new DocumentIdentifierDao();
//        List<String> documentList = new ArrayList<>();
//
//
//        for (String element : linesList) {
//            List<String> objectList = Arrays.asList(element.split(",", -1));
//            LOGGER.debug("\nObject Size: " + objectList.size());
//
//            String documentName = objectList.get(0);
//            if (!documentList.contains(documentName)) {
//                documentList.add(documentName);
//                doc.setDocName(documentName);
//                QueryUtility.insertDocumentId(doc);
//            }
//
//            int id = QueryUtility.getID(documentName);
//            if(objectList.size() == 8)
//            {
//                AnnotationResultsDao entity = new AnnotationResultsDao();
//                entity.setDocumentId(id);
//                entity.setDocumentType(objectList.get(1));
//                entity.setId(objectList.get(2));
//                entity.setAnnotationVariable(objectList.get(3));
//                entity.setProperty(objectList.get(4));
//                entity.setDocumentValue(objectList.get(5));
//                entity.setValueProperties(objectList.get(6));
//                entity.setAnnotations(objectList.get(7));
//
//                QueryUtility.insertAnnotataions(entity);
//            }
//        }
//
//
        String contentToJson = Converters.tsvToJson(contents);

        return new ModelAndView(jsonView, ServiceConstants.STATUS_FIELD, contentToJson);
    }
}
