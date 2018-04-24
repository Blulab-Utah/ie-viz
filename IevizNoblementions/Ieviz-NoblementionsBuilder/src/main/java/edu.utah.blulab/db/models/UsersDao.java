package edu.utah.blulab.db.models;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "Users")//, schema = "IE-Viz", catalog = "")
public class UsersDao implements Serializable {
    private int id;
    private String username;
    private int organizationId;
    private Timestamp loginTime;
    private Timestamp logoutTime;

    @Id
    @Column(name = "ID", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "username", nullable = false, length = 255)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Basic
    @Column(name = "organization_id", nullable = false)
    public int getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }

    @Basic
    @Column(name = "login_time", nullable = true)
    public Timestamp getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Timestamp loginTime) {
        this.loginTime = loginTime;
    }

    @Basic
    @Column(name = "logout_time", nullable = true)
    public Timestamp getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(Timestamp logoutTime) {
        this.logoutTime = logoutTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsersDao usersDao = (UsersDao) o;
        return id == usersDao.id &&
                organizationId == usersDao.organizationId &&
                Objects.equals(username, usersDao.username) &&
                Objects.equals(loginTime, usersDao.loginTime) &&
                Objects.equals(logoutTime, usersDao.logoutTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, username, organizationId, loginTime, logoutTime);
    }
}
