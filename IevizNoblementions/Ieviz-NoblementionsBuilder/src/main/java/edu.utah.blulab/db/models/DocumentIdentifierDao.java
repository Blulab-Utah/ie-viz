package edu.utah.blulab.db.models;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "DocumentIdentifier") //, schema = "dbo", catalog = "NLP_DATASTORE")
public class DocumentIdentifierDao {
    private int id;
    private String docName;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "Doc_Name", nullable = false, length = 255)
    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentIdentifierDao that = (DocumentIdentifierDao) o;
        return id == that.id &&
                Objects.equals(docName, that.docName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, docName);
    }
}
