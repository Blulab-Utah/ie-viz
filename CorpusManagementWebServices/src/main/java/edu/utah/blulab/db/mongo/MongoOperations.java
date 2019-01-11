package edu.utah.blulab.db.mongo;

import com.mongodb.*;
import edu.utah.blulab.db.models.FileContentsDao;
import edu.utah.blulab.db.models.MongoElementsDao;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoOperations {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static MongoElementsDao storeInputData(FileContentsDao fileContentsDao) throws Exception {
        //Mongo context = new MongoClient("155.101.208.236",27017);


        DBCollection collection = getCollection();


        if (null == collection)
            throw new Exception("Collection not found");


        String date = dateFormat.format(new Date());
        BasicDBObject doc = new BasicDBObject("timeStamp", date)
                .append("corpusName", fileContentsDao.getCorpusName())
                .append("fileName", fileContentsDao.getFileName())
                .append("inputDoc", fileContentsDao.getInputContent())
                .append("corpusName", fileContentsDao.getCorpusName());
        collection.insert(doc);


        MongoElementsDao mongoElementsDao = new MongoElementsDao();
        mongoElementsDao.setDate(Timestamp.valueOf(date));
        mongoElementsDao.setId(doc.get("_id").toString());
        return mongoElementsDao;
    }

    private static DBCollection getCollection() throws Exception {
        Mongo context = new MongoClient("155.101.208.201", 27017);
        DB db = context.getDB("Ieviz");// Use DB

        if (null == db)
            throw new Exception("Database not found");

        return db.getCollection("IevizInputDocs");
    }

    public static List<FileContentsDao> queryCollectionByFilter(List<String> corpusNameList) throws Exception {

        DBCollection collection = getCollection();
        List<DBObject> rawObjects = new ArrayList<>();

        for(String corpusName: corpusNameList)
        {
            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("corpusName", corpusName);
            DBCursor cursor = collection.find(whereQuery);
            while(cursor.hasNext()) {
                DBObject value = cursor.next();
                rawObjects.add(value);
            }
        }

        List<FileContentsDao> fileContentsDaoList = new ArrayList<>();
        for(DBObject dbObject: rawObjects)
        {
            FileContentsDao fileContentsDao = new FileContentsDao();
            fileContentsDao.setCorpusName(String.valueOf(dbObject.get("corpusName")));
            fileContentsDao.setFileName(String.valueOf(dbObject.get("fileName")));
            fileContentsDao.setInputContent(String.valueOf(dbObject.get("inputDoc")));
            fileContentsDaoList.add(fileContentsDao);
        }
        return fileContentsDaoList;
    }

    public static List<String> getOntologies() throws Exception {
        DBCollection collection = getCollection();
        List<DBObject> rawObjects = new ArrayList<>();
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("corpusName", "ontologies");
        DBCursor cursor = collection.find(whereQuery);
        while (cursor.hasNext()) {
            DBObject value = cursor.next();
            rawObjects.add(value);
        }

        List<String> ontList = new ArrayList<>();
        for (DBObject dbObject : rawObjects) {
            ontList.add(String.valueOf(dbObject.get("fileName")));
        }
        return ontList;
    }

    public static List<FileContentsDao> queryCollectionByOntologies(List<String> ontList) throws Exception {
        DBCollection collection = getCollection();
        List<DBObject> rawObjects = new ArrayList<>();

        for(String ontName: ontList)
        {
            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("fileName", ontName);
            DBCursor cursor = collection.find(whereQuery);
            while(cursor.hasNext()) {
                DBObject value = cursor.next();
                rawObjects.add(value);
            }
        }

        List<FileContentsDao> fileContentsDaoList = new ArrayList<>();
        for(DBObject dbObject: rawObjects)
        {
            FileContentsDao fileContentsDao = new FileContentsDao();
            fileContentsDao.setCorpusName(String.valueOf(dbObject.get("corpusName")));
            fileContentsDao.setFileName(String.valueOf(dbObject.get("fileName")));
            fileContentsDao.setInputContent(String.valueOf(dbObject.get("inputDoc")));
            fileContentsDaoList.add(fileContentsDao);
        }
        return fileContentsDaoList;
    }
}
