package edu.pitt.dbmi.nlp.noble.ontology;

/**
 * This class describes propery construct.
 *
 * @author tseytlin
 */
public interface IProperty extends IResource {
	// property constants
	public static final int OBJECT = 0;
	public static final int DATATYPE = 1;
	public static final int ANNOTATION_OBJECT = 2;
	public static final int ANNOTATION_DATATYPE = 3;
	public static final int ANNOTATION = 4;
	
	// a set of predefined RDFS/OWL properties
	public static final String RDFS_IS_DEFINED_BY = "rdfs:isDefinedBy";
	public static final String RDFS_SEE_ALSO = "rdfs:seeAlso";	
	public static final String RDFS_LABEL = "rdfs:label";
	public static final String RDFS_COMMENT = "rdfs:comment";
	public static final String OWL_VERSION_INFO = "owl:versionInfo";
	public static final String OWL_PRIOR_VERSION = "owl:priorVersion";
	public static final String OWL_BACKWARD_COMPATIBLE_WITH = "owl:backwardCompatibleWith";
	public static final String OWL_INCOMPATIBLE_WITH = "owl:incompatibleWith";
	public static final String OWL_DEPRECATED_CLASS = "owl:DeprecatedClass";
	public static final String OWL_DEPRECATED_PROPERTY = "owl:DeprecatedProperty";
	public static final String OWL_SAME_AS = "owl:sameAs";
	public static final String OWL_DIFFERENT_FROM = "owl:differentFrom";
	public static final String OWL_ALL_DIFFERENT = "owl:AllDifferent";

	// dublin core constants
	public static final String DC_CONTRIBUTOR = "http://purl.org/dc/elements/1.1/contributor";
	public static final String DC_COVERAGE = "http://purl.org/dc/elements/1.1/coverage";
	public static final String DC_CREATOR = "http://purl.org/dc/elements/1.1/creator";
	public static final String DC_DATE = "http://purl.org/dc/elements/1.1/date";
	public static final String DC_DESCRIPTION = "http://purl.org/dc/elements/1.1/description";
	public static final String DC_FORMAT = "http://purl.org/dc/elements/1.1/format";
	public static final String DC_IDENTIFIER = "http://purl.org/dc/elements/1.1/identifier";
	public static final String DC_LANGUAGE = "http://purl.org/dc/elements/1.1/language";
	public static final String DC_PUBLISHER = "http://purl.org/dc/elements/1.1/publisher";
	public static final String DC_RELATION = "http://purl.org/dc/elements/1.1/relation";
	public static final String DC_RIGHTS = "http://purl.org/dc/elements/1.1/rights";
	public static final String DC_SOURCE = "http://purl.org/dc/elements/1.1/source";
	public static final String DC_SUBJECT = "http://purl.org/dc/elements/1.1/subject";
	public static final String DC_TITLE = "http://purl.org/dc/elements/1.1/title";
	public static final String DC_TYPE = "http://purl.org/dc/elements/1.1/type";
	
	
	
	

	/**
	 * create sub property of this property.
	 *
	 * @param name the name
	 * @return the i property
	 */
	public IProperty createSubProperty(String name);
	
	
	/**
	 * get property type
	 * OBJECT, DATATYPE, ANNOTATION_OBJECT, ANNOTATION_DATATYPE.
	 *
	 * @return the property type
	 */
	public int getPropertyType();
	
	
	/**
	 * is property a datatype property.
	 *
	 * @return true, if is datatype property
	 */
	public boolean isDatatypeProperty();
	
	/**
	 * is property an object property.
	 *
	 * @return true, if is object property
	 */
	public boolean isObjectProperty();
	
	/**
	 * is property an annotation property.
	 *
	 * @return true, if is annotation property
	 */
	public boolean isAnnotationProperty();
	
	
	/**
	 * get domain of some property.
	 *
	 * @return the domain
	 */
	public IClass [] getDomain();
	
	
	/**
	 * get range of some property.
	 *
	 * @return the range
	 */
	public Object [] getRange();
	
	
	/**
	 * set domain of some property.
	 *
	 * @param domain the new domain
	 */
	public void setDomain(IResource[] domain);
	
	
	/**
	 * set range of some property.
	 *
	 * @param range the new range
	 */
	public void setRange(Object[] range);
	
	/**
	 * check if given property is an inverse of this.
	 *
	 * @param p the p
	 * @return true, if is inverse of
	 */
	public boolean isInverseOf(IProperty p);
	
	/**
	 * is property transitive.
	 *
	 * @return true, if is transitive
	 */
	public boolean isTransitive();
	
	/**
	 * is property functional.
	 *
	 * @return true, if is functional
	 */
	public boolean isFunctional();
	
	/**
	 * is property symmetrical.
	 *
	 * @return true, if is symmetric
	 */
	public boolean isSymmetric();
	
	/**
	 * get sub properties.
	 *
	 * @return the sub properties
	 */
	public IProperty [] getSubProperties();
	
	/**
	 * get super properties.
	 *
	 * @return the super properties
	 */
	public IProperty [] getSuperProperties();
	
	/**
	 * get sub properties.
	 *
	 * @return the direct sub properties
	 */
	public IProperty [] getDirectSubProperties();
	
	/**
	 * get super properties.
	 *
	 * @return the direct super properties
	 */
	public IProperty [] getDirectSuperProperties();
	
	
	/**
	 * Checks for super property.
	 *
	 * @param o the o
	 * @return true, if successful
	 */
	public boolean hasSuperProperty(IProperty o);
	
	/**
	 * Checks for sub property.
	 *
	 * @param o the o
	 * @return true, if successful
	 */
	public boolean hasSubProperty(IProperty o);
	
	/**
	 * get inverse property.
	 *
	 * @return the inverse property
	 */
	public IProperty getInverseProperty();
	
	/**
	 * set inverse property.
	 *
	 * @param p the new inverse property
	 */
	public void setInverseProperty(IProperty p);
	
	
	/**
	 * add direct super property.
	 *
	 * @param p the p
	 */
	public void addSuperProperty(IProperty p);
	
	/**
	 * add direct super property.
	 *
	 * @param p the p
	 */
	public void addSubProperty(IProperty p);
	
	/**
	 * remove super property.
	 *
	 * @param p the p
	 */
	public void removeSuperProperty(IProperty p);
	
	/**
	 * remove super property.
	 *
	 * @param p the p
	 */
	public void removeSubProperty(IProperty p);
	
	
	/**
	 * set property transitive flag.
	 *
	 * @param b the new transitive
	 */
	public void setTransitive(boolean b);
	
	/**
	 * set property functional flag.
	 *
	 * @param b the new functional
	 */
	public void  setFunctional(boolean b);
	
	/**
	 * set property symmetrical flag.
	 *
	 * @param b the new symmetric
	 */
	public void setSymmetric(boolean b);
	
}
