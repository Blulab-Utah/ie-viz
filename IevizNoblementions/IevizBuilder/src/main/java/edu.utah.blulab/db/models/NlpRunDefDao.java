package edu.utah.blulab.db.models;

import javax.persistence.*;

@Entity
@Table(name = "NLP_RUN_DEF")
public class NlpRunDefDao {
    private Integer runId;
    private String runName;
    private String runDescription;

    @Id
    @Column(name = "RUN_ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer getRunId() {
        return runId;
    }

    public void setRunId(Integer runId) {
        this.runId = runId;
    }

    @Basic
    @Column(name = "RUN_NAME")
    public String getRunName() {
        return runName;
    }

    public void setRunName(String runName) {
        this.runName = runName;
    }

    @Basic
    @Column(name = "RUN_DESCRIPTION")
    public String getRunDescription() {
        return runDescription;
    }

    public void setRunDescription(String runDescription) {
        this.runDescription = runDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NlpRunDefDao that = (NlpRunDefDao) o;

        if (runId != null ? !runId.equals(that.runId) : that.runId != null) return false;
        if (runName != null ? !runName.equals(that.runName) : that.runName != null) return false;
        if (runDescription != null ? !runDescription.equals(that.runDescription) : that.runDescription != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = runId != null ? runId.hashCode() : 0;
        result = 31 * result + (runName != null ? runName.hashCode() : 0);
        result = 31 * result + (runDescription != null ? runDescription.hashCode() : 0);
        return result;
    }
}
