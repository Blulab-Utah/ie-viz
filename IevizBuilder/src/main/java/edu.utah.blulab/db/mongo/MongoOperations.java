package edu.utah.blulab.db.mongo;

import com.mongodb.*;
import edu.utah.blulab.db.models.FileContentsDao;
import edu.utah.blulab.db.models.MongoElementsDao;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MongoOperations {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static MongoElementsDao storeInputData(FileContentsDao fileContentsDao) throws Exception {
        Mongo context = new MongoClient("155.101.208.236",27017);
        DB db = context.getDB("Ieviz");// Use DB

        if (null == db)
            throw new Exception("Database not found");

        DBCollection collection = db.getCollection("IevizInputDocs");


        if (null == collection)
            throw new Exception("Collection not found");


        String date = dateFormat.format(new Date());
        BasicDBObject doc = new BasicDBObject("timeStamp", date).
                append("inputDoc", fileContentsDao.getInputContent());
        collection.insert(doc);


        MongoElementsDao mongoElementsDao = new MongoElementsDao();
        mongoElementsDao.setDate(Timestamp.valueOf(date));
        mongoElementsDao.setId(doc.get("_id").toString());
        return mongoElementsDao;
    }
}
