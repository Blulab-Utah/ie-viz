package edu.utah.blulab.controller;

import edu.utah.blulab.constants.ServiceConstants;
import edu.utah.blulab.services.INoblementionsConnector;
import edu.utah.blulab.utilities.Converters;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import java.io.File;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


@Controller
public class ConnectionController {

    @Autowired
    private INoblementionsConnector noblementionsConnector;
    @Autowired
    private View jsonView;

    @RequestMapping(value = "/uploadMultiplePath", method = RequestMethod.POST)
    public ModelAndView getFeatures(@RequestParam("OntologyPath") String ontPath,
                                    @RequestParam("Input") String input,
                                    @RequestParam("Output") String output) throws Exception {


        Map<String, String> pathMap = new HashMap<String, String>();
        pathMap.put("ont", ontPath);
        pathMap.put("input", input);
        pathMap.put("output", output);

        noblementionsConnector.processNobleMentions(pathMap);


        String contents = FileUtils.readFileToString(new File(output+"\\RESULTS.tsv"));
        String contentToJson = Converters.tsvToJson(contents);

        return new ModelAndView(jsonView, ServiceConstants.STATUS_FIELD, contentToJson);
    }
}
