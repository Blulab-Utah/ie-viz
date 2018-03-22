package edu.utah.blulab.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import edu.utah.blulab.utilities.IevizUtilities;
import org.apache.log4j.Logger;

import edu.utah.blulab.model.OntologyModel;
import edu.utah.blulab.model.Ontology;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import edu.utah.blulab.editors.OntologyRetriever;
import edu.utah.blulab.service.OntologyManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

@RestController
@RequestMapping("/ontology-module/addNew")
@SessionAttributes("ontology")
public class OntologyController {

    private static final Logger LOGGER = Logger.getLogger(OntologyController.class);
    @Autowired
    OntologyManager manager;

    @Autowired
    private View jsonView;

    private Validator validator;

    public OntologyController() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Ontology.class, new OntologyRetriever());
    }

    @ModelAttribute("allOntologies")
    public List<Ontology> populateOntologies() throws FileNotFoundException {
        ArrayList<Ontology> ontologyList = new ArrayList<Ontology>();
        ontologyList.add(new Ontology(-1, "Select Ontology"));

        String file = "C:\\Users\\Deep\\Documents\\noble\\test\\ontologyFileList.txt";

        List<String> ontologyNames = IevizUtilities.getOntologyNames(file);

        int count = 1;
        for (String ontology : ontologyNames) {
            ontologyList.add(new Ontology(count++, ontology));
        }

        return ontologyList;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String setupForm(Model model) {
        OntologyModel ontologyModel = new OntologyModel();
        model.addAttribute("ontology", ontologyModel);
        return "addOntology";
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView submitForm(@ModelAttribute("ontology") OntologyModel ontologyModel,
                                   BindingResult result, SessionStatus status) throws IOException {

        String ontologyName = ontologyModel.getOntology().getName();

        String ontologyContent = IevizUtilities.getOntologyContent(ontologyName);

        //TO DO:

        JSONObject obj = XML.toJSONObject(ontologyContent);

        // Mark Session Complete
        status.setComplete();
        return new ModelAndView(jsonView,"ontology",obj.toString());
    }

    @RequestMapping(value = "/success", method = RequestMethod.GET)
    public String success(Model model) {
        return "addSuccess";
    }
}