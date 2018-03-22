package edu.utah.blulab.model;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

public class OntologyModel implements Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer id;

	@NotNull
	private Ontology ontology;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}


	
	public Ontology getOntology() {
		return ontology;
	}

	public void setOntology(Ontology ontology) {
		this.ontology = ontology;
	}

}