package edu.utah.blulab.controller;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.utah.blulab.containers.AnnotationContainer;
import edu.utah.blulab.containers.DocumentContainer;
import edu.utah.blulab.db.query.QueryUtility;
import edu.utah.blulab.utilities.Converters;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import edu.utah.blulab.constants.ServiceConstants;
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
import java.util.*;


@Controller
public class NoblementionsConnectionController {

    private static final Logger logger = Logger.getLogger(NoblementionsConnectionController.class);

    @Autowired
    private View jsonView;

    @RequestMapping(value = "/processAnnotations", method = RequestMethod.POST)
    public ModelAndView getFeatures(@RequestParam(value = "inputFile") MultipartFile[] inputFiles,
                                    @RequestParam(value = "ontFile") MultipartFile[] ontologyFiles) throws Exception {
        File inputFile = null;
        for (MultipartFile file : inputFiles) {
            if (!file.isEmpty()) {
                if (file.getOriginalFilename().split("\\.")[1].equals("txt")) {
                    inputFile = new File(file.getOriginalFilename());
                    try {
                        file.transferTo(inputFile);
                    } catch (IOException e) {
                        return createErrorResponse(e.getMessage());
                    }
                }
            }
        }

        File ontologyFile = null;
        for (MultipartFile file : ontologyFiles) {
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


        MultiPart multiPart;
        String contents = null;

        try {
            Client client = ClientBuilder.newBuilder().
                    register(MultiPartFeature.class).build();
            WebTarget server = client.target("http://blutc-dev.chpc.utah.edu/NoblementionsWS/getAnnotations");
            multiPart = new MultiPart();

            FileDataBodyPart inputBodyPart
                    = new FileDataBodyPart("inputFile", inputFile,MediaType.TEXT_PLAIN_TYPE);
            FileDataBodyPart ontBodyPart = new FileDataBodyPart("ontFile", ontologyFile,
                    MediaType.APPLICATION_XML_TYPE);

            // Add body part
            multiPart.bodyPart(inputBodyPart);
            multiPart.bodyPart(ontBodyPart);

            Response response = server.request(MediaType.MULTIPART_FORM_DATA_TYPE)
                    .post(Entity.entity(multiPart, "multipart/form-data"));
            if (response.getStatus() == 200) {
                contents = response.readEntity(String.class);
                logger.info(contents);
            } else {
                logger.error("Response is not ok");
            }
        } catch (Exception e) {
            logger.error("Exception has occured "+ e.getMessage());
        }

        String tsvContents = Converters.csvToTsv(contents);
        QueryUtility processor = new QueryUtility();
        List<DocumentContainer> docList = processor.processAnnotatedOutput(tsvContents);

        int runID = processor.persistRun("RunXXX");
        try
        {
            for (DocumentContainer doc : docList) {
                processor.persistAnnotation(doc, runID);
                for (AnnotationContainer annotation : doc.getAnnotations()) {
                    logger.debug(annotation.toString());

                }
            }
        }
        catch (Exception e) {
            return new ModelAndView(jsonView, ServiceConstants.STATUS_FIELD, e.getMessage());
        }

        String jsonContent = Converters.csvToJson(contents);

        return new ModelAndView(jsonView, ServiceConstants.STATUS_FIELD, jsonContent);

    }


    private ModelAndView createErrorResponse(String sMessage) {
        return new ModelAndView(jsonView, ServiceConstants.ERROR_FIELD, sMessage);
    }
}
