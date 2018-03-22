package edu.utah.blulab.model;

import java.io.Serializable;

public class Ontology implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer id;
	private String name;

	public Ontology(Integer id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Ontology [id=" + id + ", name=" + name + "]";
	}

}
