package edu.utah.blulab.db.models;

import javax.persistence.*;

@Entity
@Table(name = "NLP_RESULT_FEATURES")
public class NlpResultFeaturesEntity {
    private Integer resultFeatureId;
    private Integer snippetId;
    private Integer resultDocId;
    private String featureName;
    private String featureValue;
    private String featureValueNumeric;

    @Id
    @Column(name = "RESULT_FEATURE_ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer getResultFeatureId() {
        return resultFeatureId;
    }

    public void setResultFeatureId(Integer resultFeatureId) {
        this.resultFeatureId = resultFeatureId;
    }

    @Basic
    @Column(name = "SNIPPET_ID")
    public Integer getSnippetId() {
        return snippetId;
    }

    public void setSnippetId(Integer snippetId) {
        this.snippetId = snippetId;
    }

    @Basic
    @Column(name = "RESULT_DOC_ID")
    public Integer getResultDocId() {
        return resultDocId;
    }

    public void setResultDocId(Integer resultDocId) {
        this.resultDocId = resultDocId;
    }

    @Basic
    @Column(name = "FEATURE_NAME")
    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    @Basic
    @Column(name = "FEATURE_VALUE")
    public String getFeatureValue() {
        return featureValue;
    }

    public void setFeatureValue(String featureValue) {
        this.featureValue = featureValue;
    }

    @Basic
    @Column(name = "FEATURE_VALUE_NUMERIC")
    public String getFeatureValueNumeric() {
        return featureValueNumeric;
    }

    public void setFeatureValueNumeric(String featureValueNumeric) {
        this.featureValueNumeric = featureValueNumeric;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NlpResultFeaturesEntity that = (NlpResultFeaturesEntity) o;

        if (resultFeatureId != null ? !resultFeatureId.equals(that.resultFeatureId) : that.resultFeatureId != null)
            return false;
        if (snippetId != null ? !snippetId.equals(that.snippetId) : that.snippetId != null) return false;
        if (resultDocId != null ? !resultDocId.equals(that.resultDocId) : that.resultDocId != null) return false;
        if (featureName != null ? !featureName.equals(that.featureName) : that.featureName != null) return false;
        if (featureValue != null ? !featureValue.equals(that.featureValue) : that.featureValue != null) return false;
        if (featureValueNumeric != null ? !featureValueNumeric.equals(that.featureValueNumeric) : that.featureValueNumeric != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = resultFeatureId != null ? resultFeatureId.hashCode() : 0;
        result = 31 * result + (snippetId != null ? snippetId.hashCode() : 0);
        result = 31 * result + (resultDocId != null ? resultDocId.hashCode() : 0);
        result = 31 * result + (featureName != null ? featureName.hashCode() : 0);
        result = 31 * result + (featureValue != null ? featureValue.hashCode() : 0);
        result = 31 * result + (featureValueNumeric != null ? featureValueNumeric.hashCode() : 0);
        return result;
    }
}
