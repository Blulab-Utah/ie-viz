package edu.utah.blulab.controller;

import edu.utah.blulab.db.models.FileContentsDao;
import edu.utah.blulab.db.mongo.MongoOperations;
import edu.utah.blulab.db.query.QueryUtility;
import edu.utah.blulab.services.INlpOperationsWebServices;
import edu.utah.blulab.utilities.IevizUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.util.List;

import static edu.utah.blulab.utilities.ControllerUtilities.getPrincipal;

@Controller
public class NlpOperationsController {

    @Autowired
    private INlpOperationsWebServices nlpOperationsWebServices;


    @RequestMapping(value = "/getOperations", method = RequestMethod.GET)
    private ModelAndView getOperations(ModelMap model) {
        model.addAttribute("user", getPrincipal());
        List<String> toolsList = QueryUtility.getNlpApplicationsList();
        List<String> corpusList = QueryUtility.getCorpus();
        model.addAttribute("nlpAppList",toolsList);
        model.addAttribute("corpusList", corpusList);
        ModelAndView modelAndView = new ModelAndView("operations");
        modelAndView.addObject(model);
        return modelAndView;
    }

    @RequestMapping(value = "/processAnnotations", method = RequestMethod.POST)
    public @ResponseBody
    ModelAndView processAnnotations(ModelMap model, @RequestParam(value = "appName") String toolName,
                                 @RequestParam (value = "corpusName") List<String> corpusNameList,
                                 @RequestParam(value = "ontologies") MultipartFile[] rawOntologyFiles) throws Exception {

        String annotationContent = null;
        if("Noblementions".equals(toolName))
        {
            List<File> ontologyFiles = IevizUtilities.getOntologyFileList(rawOntologyFiles);
            List<FileContentsDao> fileContentsDaoList = MongoOperations.queryCollectionByFilter(corpusNameList);
            annotationContent = nlpOperationsWebServices.getAnnotationsFromNoblementions(fileContentsDaoList, ontologyFiles);
        }
        //        ModelAndView modelAndView = new ModelAndView("status");

//        List<File> rawFileList = IevizUtilities.getRawFileList(files);
//        model.addAttribute("user", getPrincipal());
//
//        String status = corpusManagementWebService.createNewCorpus(corpusName, rawFileList);
//        model.addAttribute("status", status);
//        modelAndView.addObject(model);
        ModelAndView modelAndView = new ModelAndView("viewannotations");
        modelAndView.addObject("content",annotationContent);
        model.addAttribute("user", getPrincipal());
        modelAndView.addObject(model);
        return modelAndView;
    }


}
