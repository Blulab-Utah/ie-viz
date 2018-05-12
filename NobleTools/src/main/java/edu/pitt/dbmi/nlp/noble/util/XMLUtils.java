package edu.pitt.dbmi.nlp.noble.util;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class XMLUtils.
 */
public class XMLUtils {
	
	/**
	 * format XML into human readable form.
	 *
	 * @param doc the doc
	 * @param os the os
	 * @throws TransformerException the transformer exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeXML(Document doc, OutputStream os) 
		throws TransformerException, IOException{
		// write out xml file
		TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        //indent XML properly
        //formatXML(doc,doc.getDocumentElement(),"  ");

        //normalize document
        doc.getDocumentElement().normalize();

		 //write XML to file
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(os);
    
        transformer.transform(source, result);
        os.close();
	}
	
	/**
	 * create new DOM document
	 * @return empty DOM object
	 * @throws ParserConfigurationException in case of error
	 */
	public static Document createDocument() throws ParserConfigurationException{
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(); 
	}
	
	/**
	 * create an element from a document, that has some text content in it
	 * @param doc - document
	 * @param name - element name
	 * @param content - text content
	 * @return element object
	 */
	public static Element createElement(Document doc, String name, String content){
		Element e = doc.createElement(name);
		e.appendChild(doc.createTextNode(content));
		return e;
	}
	
	/**
	 * create an element from a document, that has some text content in it
	 * @param doc - document
	 * @param name - element name
	 * @param attribute - attribute
	 * @param value - value
	 * @return element object
	 */
	public static Element createElement(Document doc, String name, String attribute, String value){
		Element e = doc.createElement(name);
		e.setAttribute(attribute, value);
		return e;
	}
	
	/**
	 * create an element from a document, that has some text content in it
	 * @param doc - document
	 * @param name - element name
	 * @param attributes - attribute map
	 * @return element object
	 */
	public static Element createElement(Document doc, String name, Map<String,String> attributes){
		Element e = doc.createElement(name);
		for(String attribute: attributes.keySet())
			e.setAttribute(attribute,attributes.get(attribute));
		return e;
	}
	
	/**
	 * create an element from a document, that has some text content in it
	 * @param doc - document
	 * @param name - element name
	 * @param attributes - attribute map
	 * @param content - string content
	 * @return element object
	 */
	public static Element createElement(Document doc, String name, Map<String,String> attributes, String content){
		Element e = createElement(doc, name, attributes);
		e.appendChild(doc.createTextNode(content));
		return e;
	}
	
	
	/**
	 * parse XML document.
	 *
	 * @param in the in
	 * @return the document
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Document parseXML(InputStream in) throws IOException {
		Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//factory.setValidating(true);
		//factory.setNamespaceAware(true);

		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			//builder.setErrorHandler(new XmlErrorHandler());
			//builder.setEntityResolver(new XmlEntityResolver());
			document = builder.parse(in);
			
			// close input stream
			in.close();
		}catch(Exception ex){
			throw new IOException(ex.getMessage());
		}
		return document;
	}
	
	/**
	 * get single element by tag name.
	 *
	 * @param element the element
	 * @param tag the tag
	 * @return the element by tag name
	 */
	public static Element getElementByTagName(Element element, String tag){
		NodeList list = element.getChildNodes();
		for(int i=0;i<list.getLength();i++){
			Node node = list.item(i);
			if(node instanceof Element && ((Element)node).getTagName().equals(tag)){
				return (Element) node;
			}
		}
		return null;
	}
	
	/**
	 * get single element by tag name.
	 *
	 * @param element the element
	 * @param tag the tag
	 * @return the elements by tag name
	 */
	public static List<Element> getElementsByTagName(Element element, String tag){
		List<Element> elems = new ArrayList<Element>();
		NodeList list = element.getChildNodes();
		for(int i=0;i<list.getLength();i++){
			Node node = list.item(i);
			if(node instanceof Element){
				Element e = (Element) node;
				if(e.getTagName().equals(tag)){
					elems.add(e);
				}
			}
		}
		return elems;
	}
	
	/**
	 * get single element by tag name.
	 *
	 * @param element the element
	 * @return the child elements
	 */
	public static List<Element> getChildElements(Element element){
		return toElements(element.getChildNodes());
	}
	
	/**
	 * get single element by tag name.
	 *
	 * @param element the element
	 * @param tag the tag
	 * @return the child elements
	 */
	public static List<Element> getChildElements(Element element, String tag){
		return toElements(element.getChildNodes(),tag);
	}
	
	/**
	 * To elements.
	 *
	 * @param nodes the nodes
	 * @return the list
	 */
	public static List<Element> toElements(NodeList nodes){
		return toElements(nodes,null);
	}
	
	/**
	 * To elements.
	 *
	 * @param nodes the nodes
	 * @param filter the filter
	 * @return the list
	 */
	private static List<Element> toElements(NodeList nodes, String filter){
		List<Element> list = new ArrayList<Element>();
		for(int i=0;i<nodes.getLength();i++)
			if(nodes.item(i) instanceof Element){
				Element e = (Element)nodes.item(i);
				if(filter == null || e.getTagName().equals(filter)){
					list.add(e);
				}
			}
		return list;
	}
	
	/**
	 * get attribute map from the element
	 * @param el - element
	 * @return mapping of attributes
	 */
	public static Map<String,String> getAttributes(Element el){
		Map<String,String> list = new LinkedHashMap<String,String>();
		NamedNodeMap map = el.getAttributes();
		for(int i=0;i<map.getLength();i++){
			list.put(map.item(i).getNodeName(),map.item(i).getNodeValue());
		}
		return list;
	}
}
