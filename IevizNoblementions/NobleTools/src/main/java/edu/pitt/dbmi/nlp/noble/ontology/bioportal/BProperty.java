package edu.pitt.dbmi.nlp.noble.ontology.bioportal;

import edu.pitt.dbmi.nlp.noble.ontology.IClass;
import edu.pitt.dbmi.nlp.noble.ontology.IProperty;
import edu.pitt.dbmi.nlp.noble.ontology.IResource;

/**
 * The Class BProperty.
 */
public class BProperty extends BResource implements IProperty {
	private String orignalName;
	
	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#addSubProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void addSubProperty(IProperty p) {
		// TODO Auto-generated method stub

	}
	
	
	/**
	 * Gets the orignal name.
	 *
	 * @return the orignal name
	 */
	public String getOrignalName() {
		return orignalName;
	}


	/**
	 * Sets the orignal name.
	 *
	 * @param orignalName the new orignal name
	 */
	public void setOrignalName(String orignalName) {
		this.orignalName = orignalName;
	}


	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#addSuperProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void addSuperProperty(IProperty p) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#createSubProperty(java.lang.String)
	 */
	public IProperty createSubProperty(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getDirectSubProperties()
	 */
	public IProperty[] getDirectSubProperties() {
		return new IProperty [0];
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getDirectSuperProperties()
	 */
	public IProperty[] getDirectSuperProperties() {
		return new IProperty [0];
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getDomain()
	 */
	public IClass[] getDomain() {
		return new IClass [0];
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getInverseProperty()
	 */
	public IProperty getInverseProperty() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getPropertyType()
	 */
	public int getPropertyType() {
		return IProperty.ANNOTATION_DATATYPE;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getRange()
	 */
	public Object[] getRange() {
		return new String [0];
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getSubProperties()
	 */
	public IProperty[] getSubProperties() {
		return new IProperty [0];
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#getSuperProperties()
	 */
	public IProperty[] getSuperProperties() {
		return new IProperty [0];
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isAnnotationProperty()
	 */
	public boolean isAnnotationProperty() {
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isDatatypeProperty()
	 */
	public boolean isDatatypeProperty() {
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isFunctional()
	 */
	public boolean isFunctional() {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isInverseOf(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public boolean isInverseOf(IProperty p) {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isObjectProperty()
	 */
	public boolean isObjectProperty() {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isSymmetric()
	 */
	public boolean isSymmetric() {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#isTransitive()
	 */
	public boolean isTransitive() {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#removeSubProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void removeSubProperty(IProperty p) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#removeSuperProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void removeSuperProperty(IProperty p) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setDomain(edu.pitt.dbmi.nlp.noble.ontology.IResource[])
	 */
	public void setDomain(IResource[] domain) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setFunctional(boolean)
	 */
	public void setFunctional(boolean b) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setInverseProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public void setInverseProperty(IProperty p) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setRange(java.lang.Object[])
	 */
	public void setRange(Object[] range) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setSymmetric(boolean)
	 */
	public void setSymmetric(boolean b) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#setTransitive(boolean)
	 */
	public void setTransitive(boolean b) {
		// TODO Auto-generated method stub

	}


	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#hasSuperProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public boolean hasSuperProperty(IProperty o) {
		// TODO Auto-generated method stub
		return false;
	}


	/* (non-Javadoc)
	 * @see edu.pitt.dbmi.nlp.noble.ontology.IProperty#hasSubProperty(edu.pitt.dbmi.nlp.noble.ontology.IProperty)
	 */
	public boolean hasSubProperty(IProperty o) {
		// TODO Auto-generated method stub
		return false;
	}

}
