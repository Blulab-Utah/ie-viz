package edu.utah.blulab.service;

import java.util.List;

import edu.utah.blulab.model.OntologyModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.utah.blulab.dao.OntologyDao;

@Service
public class OntologyManagerImpl implements OntologyManager {

	@Autowired
	OntologyDao dao;
	
	public List<OntologyModel> getAllOntologies()
	{
		return dao.getAllOntologies();
	}
}
