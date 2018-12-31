package edu.utah.blulab.db.models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "CORPUS_METADATA")
public class CorpusMetadataDao {
    private Integer metadataId;
    private String fileObjId;
    private String corpusName;
    private Timestamp timestamp;
    private String fileName;

    @Id
    @Column(name = "METADATA_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getMetadataId() {
        return metadataId;
    }

    public void setMetadataId(Integer metadataId) {
        this.metadataId = metadataId;
    }

    @Column(name = "FILE_OBJ_ID")
    public String getFileObjId() {
        return fileObjId;
    }

    public void setFileObjId(String fileObjId) {
        this.fileObjId = fileObjId;
    }


    @Basic
    @Column(name = "FILE_NAME")
    public String getFileName(){ return fileName;}

    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    @Basic
    @Column(name = "CORPUS_NAME")
    public String getCorpusName() {
        return corpusName;
    }

    public void setCorpusName(String corpusName) {
        this.corpusName = corpusName;
    }

    @Basic
    @Column(name = "TIMESTAMP")
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CorpusMetadataDao that = (CorpusMetadataDao) o;
        return Objects.equals(fileObjId, that.fileObjId) &&
                Objects.equals(corpusName, that.corpusName) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileObjId, corpusName, timestamp);
    }
}
