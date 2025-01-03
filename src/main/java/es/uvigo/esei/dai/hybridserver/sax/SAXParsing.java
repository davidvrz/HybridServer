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

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class SAXParsing {
  	public static void parseAndValidateWithExternalXSD(String xmlPath, String schemaPath, ContentHandler handler)
  			throws ParserConfigurationException, SAXException, IOException {
	    // Construcción del schema
	    final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	    final Schema schema = schemaFactory.newSchema(new File(schemaPath));
	
	    // Construcción del parser del documento. Se establece el esquema y se activa
	    // la validación y comprobación de namespaces
	    final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
	    parserFactory.setValidating(false);
	    parserFactory.setNamespaceAware(true);
	    parserFactory.setSchema(schema);
	
	    // Se añade el manejador de errores
	    final SAXParser parser = parserFactory.newSAXParser();
	    final XMLReader xmlReader = parser.getXMLReader();
	    xmlReader.setContentHandler(handler);
	    xmlReader.setErrorHandler(new SimpleErrorHandler());
	
	    // Parsing
	    try (FileReader fileReader = new FileReader(new File(xmlPath))) {
	      xmlReader.parse(new InputSource(fileReader));
	    }
  	}
  
	public static void parseAndValidateWithMemoryXSD(String xmlContent, String xsdContent)
			throws ParserConfigurationException, SAXException, IOException {

        // Construcción del schema desde el contenido en memoria del XSD
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        StreamSource ss = new StreamSource(new StringReader(xsdContent));
        Schema schema = schemaFactory.newSchema(ss);

        // Construcción del parser SAX
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setValidating(false); // Validación con el schema, no DTD
        parserFactory.setNamespaceAware(true);
        parserFactory.setSchema(schema);

        // Crear el parser y el lector XML
        SAXParser parser = parserFactory.newSAXParser();
        XMLReader xmlReader = parser.getXMLReader();
        xmlReader.setErrorHandler(new SimpleErrorHandler());  // Manejador de errores

        // Parsing del XML contenido en memoria
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());
        InputSource inputSource = new InputSource(inputStream);
        xmlReader.parse(inputSource);

	}
}