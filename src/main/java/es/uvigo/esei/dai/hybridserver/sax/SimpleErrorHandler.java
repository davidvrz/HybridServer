package es.uvigo.esei.dai.hybridserver.sax;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SimpleErrorHandler implements ErrorHandler {
	@Override
	public void warning(SAXParseException exception) throws SAXException {
	    throw exception;
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
	    throw exception;  // Lanzar excepción fatal
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
	    throw exception;  // Lanzar excepción
	}
}