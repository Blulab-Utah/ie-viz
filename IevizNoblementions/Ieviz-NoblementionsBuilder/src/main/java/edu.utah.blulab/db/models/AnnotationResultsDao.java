package edu.utah.blulab.db.models;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "AnnotationResults", schema = "dbo", catalog = "NLP_DATASTORE")
public class AnnotationResultsDao {
    private int annotationId;
    private int documentId;
    private String documentType;
    private String id;
    private String annotationVariable;
    private String property;
    private String documentValue;
    private String valueProperties;
    private String annotations;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Annotation_ID", nullable = false)
    public int getAnnotationId() {
        return annotationId;
    }

    public void setAnnotationId(int annotationId) {
        this.annotationId = annotationId;
    }

    @Basic
    @Column(name = "Document_ID", nullable = false)
    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    @Basic
    @Column(name = "Document_Type", nullable = true, length = 255)
    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    @Basic
    @Column(name = "Id", nullable = true, length = 255)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic
    @Column(name = "Annotation_Variable", nullable = true, length = 255)
    public String getAnnotationVariable() {
        return annotationVariable;
    }

    public void setAnnotationVariable(String annotationVariable) {
        this.annotationVariable = annotationVariable;
    }

    @Basic
    @Column(name = "Property", nullable = true, length = 255)
    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    @Basic
    @Column(name = "Document_Value", nullable = true, length = 255)
    public String getDocumentValue() {
        return documentValue;
    }

    public void setDocumentValue(String documentValue) {
        this.documentValue = documentValue;
    }

    @Basic
    @Column(name = "Value_Properties", nullable = true, length = 255)
    public String getValueProperties() {
        return valueProperties;
    }

    public void setValueProperties(String valueProperties) {
        this.valueProperties = valueProperties;
    }

    @Basic
    @Column(name = "Annotations", nullable = true, length = 255)
    public String getAnnotations() {
        return annotations;
    }

    public void setAnnotations(String annotations) {
        this.annotations = annotations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnotationResultsDao that = (AnnotationResultsDao) o;
        return annotationId == that.annotationId &&
                documentId == that.documentId &&
                Objects.equals(documentType, that.documentType) &&
                Objects.equals(id, that.id) &&
                Objects.equals(annotationVariable, that.annotationVariable) &&
                Objects.equals(property, that.property) &&
                Objects.equals(documentValue, that.documentValue) &&
                Objects.equals(valueProperties, that.valueProperties) &&
                Objects.equals(annotations, that.annotations);
    }

    @Override
    public int hashCode() {

        return Objects.hash(annotationId, documentId, documentType, id, annotationVariable, property, documentValue, valueProperties, annotations);
    }
}
