package edu.utah.blulab.controller;

import edu.utah.blulab.services.ICorpusManagementWebService;
import edu.utah.blulab.utilities.IevizUtilities;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CorpusManagementController {

    @Autowired
    private ICorpusManagementWebService corpusManagementWebService;

    @RequestMapping(value = "/createCorpus", method = RequestMethod.POST)
    public @ResponseBody
    ModelAndView createNewCorpus(ModelMap model, @RequestParam(value = "corpusName") String corpusName,
                                 @RequestParam(value = "corpusFiles") MultipartFile[] files) {
        ModelAndView modelAndView = new ModelAndView("status");
        List<File> rawFileList = IevizUtilities.getRawFileList(files);
        model.addAttribute("user", getPrincipal());

        String status = corpusManagementWebService.createNewCorpus(corpusName, rawFileList);
        model.addAttribute("status", status);
        modelAndView.addObject(model);
        return modelAndView;
    }

    @RequestMapping(value = "/manageCorpora", method = RequestMethod.GET)
    private ModelAndView manageCorpus(ModelMap model) {
        ModelAndView modelAndView = new ModelAndView("corpusManagement");
        List<String> corpusList = corpusManagementWebService.populateCorpusNames();
        model.addAttribute("user", getPrincipal());
        model.addAttribute("corpusList", corpusList);
        modelAndView.addObject(model);
        return modelAndView;
    }


//    @RequestMapping(value = "/updateCorpus", method = RequestMethod.POST)
//    private ModelAndView updateCorpus(ModelMap model, @RequestParam("corpusItem") String corpusName,
//                                      @RequestParam(value = "corpusFiles") MultipartFile[] files) {
//        ModelAndView modelAndView = new ModelAndView("status");
//        List<File> rawFileList = IevizUtilities.getRawFileList(files);
//        model.addAttribute("user", getPrincipal());
//        String status = corpusManagementWebService.createNewCorpus(corpusName, rawFileList);
//        model.addAttribute("status", status);
//        modelAndView.addObject(model);
//        return null;
//    }

}
