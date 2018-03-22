package edu.utah.blulab.dao;

import java.util.List;

import edu.utah.blulab.model.OntologyModel;

public interface OntologyDao
{
	public List<OntologyModel> getAllOntologies();
}