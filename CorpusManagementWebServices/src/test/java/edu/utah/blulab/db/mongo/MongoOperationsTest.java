package edu.utah.blulab.db.mongo;

import com.mongodb.*;
import edu.utah.blulab.db.models.FileContentsDao;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MongoOperationsTest {

    private static DBCollection collection = null;
    @BeforeClass
    public static void setup()
    {
        Mongo context = new MongoClient("155.101.208.201", 27017);
        DB db = context.getDB("Ieviz");// Use DB

        if (null == db)
            try {
                throw new Exception("Database not found");
            } catch (Exception e) {
                e.printStackTrace();
            }

        collection= db.getCollection("IevizInputDocs");
    }

    @Test
    public void testCollectionNotNull()
    {
        Assert.assertNotNull(collection);
    }

    @Test
    public void testQueryCollectionByFilter()
    {
        List<String> corpusNameList = new ArrayList<>();
        corpusNameList.add("c1");
        corpusNameList.add("c2");
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
        Assert.assertNotEquals(rawObjects.size(),0);

        List<FileContentsDao> fileContentsDaoList = new ArrayList<>();
        for(DBObject dbObject: rawObjects)
        {
            FileContentsDao fileContentsDao = new FileContentsDao();
            fileContentsDao.setCorpusName(String.valueOf(dbObject.get("corpusName")));
            fileContentsDao.setFileName(String.valueOf(dbObject.get("fileName")));
            fileContentsDao.setInputContent(String.valueOf(dbObject.get("inputDoc")));
            fileContentsDaoList.add(fileContentsDao);
        }

        Assert.assertFalse(fileContentsDaoList.isEmpty());
    }
}
