package edu.utah.blulab.dao;

import java.util.ArrayList;
import java.util.List;

import edu.utah.blulab.model.OntologyModel;
import org.springframework.stereotype.Repository;

@Repository
public class OntologyDaoImpl implements OntologyDao {

	public List<OntologyModel> getAllOntologies()
	{
		List<OntologyModel> ontologies = new ArrayList<OntologyModel>();
		
		OntologyModel vo1 = new OntologyModel();
		vo1.setId(1);
		ontologies.add(vo1);
		
		OntologyModel vo2 = new OntologyModel();
		vo2.setId(2);
		ontologies.add(vo2);
		
		return ontologies;
	}
}