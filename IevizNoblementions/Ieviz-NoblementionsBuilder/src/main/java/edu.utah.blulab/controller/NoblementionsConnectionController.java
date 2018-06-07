package edu.utah.blulab.controller;

import edu.utah.blulab.constants.ServiceConstants;
import edu.utah.blulab.db.models.AnnotationResultsDao;
import edu.utah.blulab.db.models.DocumentIdentifierDao;
import edu.utah.blulab.db.query.QueryUtility;
import edu.utah.blulab.services.INoblementionsConnector;
import edu.utah.blulab.utilities.Converters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


@Controller
public class NoblementionsConnectionController {

    private static final Logger LOGGER = Logger.getLogger(NoblementionsConnectionController.class);

    @Autowired
    private View jsonView;

    @RequestMapping(value = "/uploadMultiplePath", method = RequestMethod.POST)
    public ModelAndView getFeatures(@RequestParam("input") String input,
                                    @RequestParam("output") String output,
                                    @RequestParam("file") MultipartFile[] files) throws Exception {

        File ontologyFile = null;
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                if (file.getOriginalFilename().split("\\.")[1].equals("owl")) {
                    ontologyFile = new File(file.getOriginalFilename());
                    try {
                        file.transferTo(ontologyFile);
                    } catch (IOException e) {
                        return createErrorResponse(e.getMessage());
                    }
                }
            }
        }

//        CloseableHttpClient client = HttpClients.createDefault();
//        HttpPost httpPost = new HttpPost("http://localhost:8080/NoblementionsWS/getAnnotations");
//        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//        builder.addTextBody("input", input);
//        builder.addTextBody("output", output);
        assert ontologyFile != null;
        String x = ontologyFile.getAbsolutePath();
        String y = ontologyFile.getPath();
        String z = ontologyFile.getCanonicalPath();
//        builder.addTextBody("ont",ontologyFile.getAbsolutePath());
//        assert ontologyFile != null;
//        builder.addBinaryBody("file", ontologyFile,
//                ContentType.APPLICATION_OCTET_STREAM, "file.ext");

//        HttpEntity multipart = builder.build();
//        httpPost.setEntity(multipart);

//        CloseableHttpResponse response = client.execute(httpPost);
//        String responseContent =EntityUtils.toString(response.getEntity());
//        int statusCode = response.getStatusLine().getStatusCode();
//        LOGGER.debug("\n"+responseContent);
////        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
//        client.close();


        final URL url = new URL("http://localhost:8080/NoblementionsWS/getAnnotations");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json");

        connection.setRequestProperty("input", input);
        connection.addRequestProperty("output", output);
        connection.addRequestProperty("ont", x);

        connection.setDoOutput(true);

        OutputStream os = connection.getOutputStream();
        os.write(input.getBytes());
        os.flush();


        if (connection.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + connection.getResponseCode());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder contentsToCsv = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            contentsToCsv.append(inputLine);
        }
        in.close();


//        String contents = FileUtils.readFileToString(new File(output + "\\RESULTS.tsv"));

        Scanner scanner = new Scanner(contentsToCsv.toString());
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
            if (objectList.size() == 8) {
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


        String contentToJson = Converters.csvToJson(contentsToCsv.toString());

        return new ModelAndView(jsonView, ServiceConstants.STATUS_FIELD, contentToJson);
    }

    private ModelAndView createErrorResponse(String sMessage) {
        return new ModelAndView(jsonView, ServiceConstants.ERROR_FIELD, sMessage);
    }
}
