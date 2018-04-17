package edu.utah.blulab.db.models;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Organizations")//, schema = "dbo", catalog = "NLP_DATASTORE")
public class OrganizationsDao {
    private int id;
    private String organizationName;

    @Id
    @Column(name = "ID", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "organization_name", nullable = false, length = 255)
    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationsDao that = (OrganizationsDao) o;
        return id == that.id &&
                Objects.equals(organizationName, that.organizationName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, organizationName);
    }
}
