package edu.pitt.dbmi.nlp.noble.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.jdbm.DB;
import org.apache.jdbm.DBMaker;

/**
 * This class wraps JDBM http://jdbm.sourceforge.net/ HTree
 * To create a persistent hashtable on disk
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author tseytlin
 */

public class JDBMMap<K extends Comparable, V> implements Map<K, V> {
    private Map<K, V> map;
    private String name, filename;
    private DB db;
    private boolean readonly;
    public static final String JDBM_SUFFIX = ".d.0";


    /**
     * create an instance of persistent hash map.
     *
     * @param filename  the filename
     * @param tablename the tablename
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public JDBMMap(String filename, String tablename) throws IOException {
        this(filename, tablename, false);
    }

    /**
     * create an instance of persistent hash map.
     *
     * @param filename  the filename
     * @param tablename the tablename
     * @param readonly  the readonly
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public JDBMMap(String filename, String tablename, boolean readonly) throws IOException {
        this.name = tablename;
        this.filename = filename;
        this.readonly = readonly;
        String f = filename + "_" + tablename;

        // check parent directory
        if (!new File(f).getParentFile().exists())
            throw new IOException("Location " + new File(f).getParentFile().getAbsolutePath() + " does not exist!");

        // if file doesn't exist, make it read-only
//		if(readonly && !new File(f+JDBM_SUFFIX ).exists())
//			readonly = false;

        // init record manager
        DBMaker d = DBMaker.openFile(f);
        // set options
        d.disableTransactions();
        d.closeOnExit();
        d.enableMRUCache();
        d.useRandomAccessFile();
        //d.enableHardCache();
        //d.disableCache();
        if (readonly) {
            d.disableLocking();
            d.readonly();
        }
        db = d.make();

        // create or load hashtable from given file
        map = db.getHashMap(tablename);
        if (map == null)
            try {
                map = db.createHashMap(tablename);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    /**
     * Checks if is read only.
     *
     * @return true, if is read only
     */
    public boolean isReadOnly() {
        return readonly;
    }

    /**
     * Gets the table name.
     *
     * @return the table name
     */
    public String getTableName() {
        return name;
    }

    /**
     * Gets the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return filename;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        dispose();
    }


    /**
     * Dispose.
     */
    public void dispose() {
        db.close();
    }

    /**
     * commit transaction.
     */
    public void commit() {
        db.commit();
    }

    /**
     * Compact.
     */
    public void compact() {
        db.defrag(true);
    }

    /**
     * remove all records at once.
     */
    public void clear() {
        map.clear();
    }


    /**
     * contains key.
     *
     * @param key the key
     * @return true, if successful
     */
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /**
     * this is very expensive call to check for values.
     *
     * @param e the e
     * @return true, if successful
     */
    public boolean containsValue(Object e) {
        return map.containsValue(e);
    }


    /**
     * this is a very expensive call to get all of the entry set.
     *
     * @return the sets the
     */
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }


    /**
     * get value for given key.
     *
     * @param key the key
     * @return the v
     */
    public V get(Object key) {
        return map.get(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }


    /**
     * this is an expensive call to get all of the keys.
     *
     * @return the sets the
     */
    public Set<K> keySet() {
        return map.keySet();
    }

    /**
     * put values into the table.
     *
     * @param key   the key
     * @param value the value
     * @return the v
     */
    public V put(K key, V value) {
        return map.put(key, value);
    }

    /**
     * put all values.
     *
     * @param m the m
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }


    /**
     * remove entry from hashtable.
     *
     * @param key the key
     * @return the v
     */
    public V remove(Object key) {
        return map.remove(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size() {
        return map.size();
    }

    /**
     * this is an expensive call to get all of the values.
     *
     * @return the collection
     */
    public Collection<V> values() {
        return map.values();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return map.toString();
    }
}
