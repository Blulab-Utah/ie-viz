package edu.utah.blulab.db.models;

import java.sql.Timestamp;

/**
 * Created by Deep on 12/23/2016.
 */
public class MongoElementsDao {

    private Timestamp date;
    private String id;

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
