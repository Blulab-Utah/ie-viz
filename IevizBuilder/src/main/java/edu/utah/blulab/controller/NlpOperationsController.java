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
    private ModelAndView getOperations(ModelMap model) throws Exception {
        model.addAttribute("user", getPrincipal());
        List<String> toolsList = QueryUtility.getNlpApplicationsList();
        List<String> corpusList = QueryUtility.getCorpus();
        List<String> ontList = MongoOperations.getOntologies();
        model.addAttribute("nlpAppList",toolsList);
        model.addAttribute("corpusList", corpusList);
        model.addAttribute("ontList", ontList);
        ModelAndView modelAndView = new ModelAndView("operations");
        modelAndView.addObject(model);
        return modelAndView;
    }

    @RequestMapping(value = "/processAnnotations", method = RequestMethod.POST)
    public @ResponseBody
    ModelAndView processAnnotations(ModelMap model, @RequestParam(value = "appName") String toolName,
                                 @RequestParam (value = "ontName") List<String> ontList,
                                 @RequestParam (value = "corpusName") List<String> corpusNameList) throws Exception {

        String annotationContent = null;
        if("Noblementions".equals(toolName))
        {

            List<FileContentsDao> ontologyFiles = MongoOperations.queryCollectionByOntologies(ontList);
            List<FileContentsDao> fileContentsDaoList = MongoOperations.queryCollectionByFilter(corpusNameList);
            annotationContent = nlpOperationsWebServices.getAnnotationsFromNoblementions(fileContentsDaoList, ontologyFiles);
        }

        ModelAndView modelAndView = new ModelAndView("viewannotations");
        modelAndView.addObject("content",annotationContent);
        model.addAttribute("user", getPrincipal());
        modelAndView.addObject(model);
        return modelAndView;
    }


}
