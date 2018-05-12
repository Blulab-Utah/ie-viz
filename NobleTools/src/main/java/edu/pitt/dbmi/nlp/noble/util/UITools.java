package edu.pitt.dbmi.nlp.noble.util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class UITools {
	public static final File DEFAULT_PROPERTIES_LOCATION = new File(System.getProperty("user.home")+File.separator+".noble"+File.separator+"properties");
	
	/**
     * Display a lastFile in the system browser. If you want to display a lastFile, you
     * must include the absolute path name.
     *
     * @param url the lastFile's url (the url must start with either "http://" or  "lastFile://").
     * @throws Exception - in case of error
     */
     public static void browseURLInSystemBrowser(String url) throws Exception{
    	 Desktop desktop = Desktop.getDesktop();
    	 if( !desktop.isSupported( Desktop.Action.BROWSE ) ) {
    		 throw new Exception("Could not open "+url+" as the system browser is not supported");
    	 }
		 java.net.URI uri = new java.net.URI( url );
		 desktop.browse( uri );
     }
     /**
      * show error dialog
      * @param owner - owner panel
      * @param ex -exception
      */
     public static void showErrorDialog(Component owner,Exception ex){
    	showErrorDialog(owner,"",ex);
     }
     /**
      * show error dialog
      * @param owner - owner panel
      * @param error - message
      */
     public static void showErrorDialog(Component owner,String error){
    	showErrorDialog(owner,error,null);
     }
     
     /**
      * show error dialog
      * @param owner - owner panel
      * @param error - error message
      * @param ex - exception
      */
     public static void showErrorDialog(Component owner,String error, Exception ex){
    	 String msg = error;
    	 if(ex != null){
        	 ex.printStackTrace();
        	 msg = error+" "+ex.getMessage();
    	 }
    	 JOptionPane.showMessageDialog(owner,msg,"Error",JOptionPane.ERROR_MESSAGE);

     }
     
     /**
      * save UI settings map for a given class
      * @param p - property map of settings
      * @param cls - class that this belongs to
      */
	public static void saveSettings(Properties p, Class cls){
		saveSettings(p, new File(DEFAULT_PROPERTIES_LOCATION,cls.getSimpleName()+".properties"));
		
	}
     
     
     /**
      * save UI settings map for a given class
      * @param p - property map of settings
      * @param file - file that contains the settings
      */
	public static void saveSettings(Properties p, File file){
		if(!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		try{
			FileOutputStream fos = new FileOutputStream(file);
			p.store(fos,file.getName());
			fos.close();
		}catch(IOException ex){
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * load UI settings map for a given class
	 * @param cls - class that has the mapping
	 * @return - properties
	 */
	
	public static Properties loadSettings(Class cls){
		return loadSettings(new File(DEFAULT_PROPERTIES_LOCATION,cls.getSimpleName()+".properties"));	
	}
	
	/**
	 * load UI settings map for a given class
	 * @param file - file that contains the properties
	 * @return - properties
	 */
	
	public static Properties loadSettings(File file){
		Properties p = new Properties();
		if(file.exists()){
			try{
				FileInputStream fis = new FileInputStream(file);
				p.load(fis);
				fis.close();
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
		return p;
	}
}
