/**
 * This class is used to send data to the servlet in meaningful way
 * Author: Eugene Tseytlin (University of Pittsburgh)
 */

package edu.pitt.dbmi.nlp.noble.util;

import java.io.Serializable;
import java.util.Properties;

/**
 * This class should be used as a container class for client/server 
 * communications via servlets. This way objects as well as key-value pairs
 * can be sent using Post method.
 */
public class Parcel implements Serializable {
	private String action, user,session;
	private Properties props;
	private Object payload;
	
	/**
	 * Send parcel with action and one parameter.
	 *
	 * @param action the action
	 * @param payload the payload
	 */
	public Parcel(String action, Object payload){
		this(action,null,null,payload);
	}
	
	/**
	 * Send parcel with all parameters.
	 *
	 * @param action the action
	 * @param user the user
	 * @param args the args
	 * @param payload the payload
	 */
	public Parcel(String action, String user, Properties args, Object payload){
		this.action = action;
		this.props = args;
		this.payload = payload;
		this.user = user;
	}
	
	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	public String getAction(){
		return action;	
	}
	
	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	public Properties getProperties(){
		if(props == null)
			props = new Properties();
		return props;	
	}
	
	/**
	 * Gets the payload.
	 *
	 * @return the payload
	 */
	public Object getPayload(){
		return payload;	
	}
	
	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	public String getUsername(){
		return user;
	}
	
	/**
	 * Gets the session.
	 *
	 * @return the session
	 */
	public String getSession(){
		return session;	
	}
	
	/**
	 * Sets the session.
	 *
	 * @param s the new session
	 */
	public void setSession(String s){
		session = s;	
	}
}
