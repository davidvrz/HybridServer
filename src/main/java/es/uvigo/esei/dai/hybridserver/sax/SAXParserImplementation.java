package es.uvigo.esei.dai.hybridserver.sax;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

//import org.omg.CORBA.portable.InputStream;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class SAXParserImplementation {

//	public static void parseAndValidateWithInternalXSD(
//			String xmlPath, ContentHandler handler
//			) throws ParserConfigurationException, SAXException, IOException {
//			// Construcción del parser del documento. Se activa
//			// la validación y comprobación de namespaces
//			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
//			parserFactory.setValidating(true);
//			parserFactory.setNamespaceAware(true);
//			// Se añade el manejador de errores y se activa la validación
//			// por schema
//			SAXParser parser = parserFactory.newSAXParser();
//			parser.setProperty(
//			"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
//			XMLConstants.W3C_XML_SCHEMA_NS_URI
//			);
//			XMLReader xmlReader = parser.getXMLReader();
//			xmlReader.setContentHandler(handler);
//			xmlReader.setErrorHandler(new SimpleErrorHandler());
//			// Parsing
//			try (FileReader fileReader = new FileReader(new File(xmlPath))) {
//			xmlReader.parse(new InputSource(fileReader));
//			}
//			}

	// Procesado y validación con un XSD externo de un documento con SAX
	public static void parseAndValidateWithExternalXSD(String xmlPath, String schemaPath, ContentHandler handler)
			throws ParserConfigurationException, SAXException, IOException {

		// Construcción del schema
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(new File(schemaPath));

		// Construcción del parser del documento.
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setValidating(false);
		parserFactory.setNamespaceAware(true);
		parserFactory.setSchema(schema);

		// Se añade el manejador de errores
		SAXParser parser = parserFactory.newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		xmlReader.setContentHandler(handler);
		xmlReader.setErrorHandler(new SimpleErrorHandler());

		// Parsing
		try (FileReader fileReader = new FileReader(new File(xmlPath))) {
			xmlReader.parse(new InputSource(fileReader));
		}

	}

	public static void parseAndValidateXSD(String xmlContent, String xsdContent)
			throws ParserConfigurationException, SAXException, IOException {

		// Construcción del schema
		
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		StreamSource ss = new StreamSource(new StringReader(xsdContent));
		Schema schema = schemaFactory.newSchema(ss);

		// Construcción del parser del documento.
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setValidating(false);
		parserFactory.setNamespaceAware(true);
		parserFactory.setSchema(schema);

		// Se añade el manejador de errores
		SAXParser parser = parserFactory.newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		xmlReader.setErrorHandler(new SimpleErrorHandler());

		// Parsing
		ByteArrayInputStream i = new ByteArrayInputStream(xmlContent.getBytes());
		InputSource input = new InputSource(i);
	    xmlReader.parse(input);
		

	}

}
