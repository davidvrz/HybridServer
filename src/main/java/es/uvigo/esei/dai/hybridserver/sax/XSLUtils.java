package es.uvigo.esei.dai.hybridserver.sax;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XSLUtils {

	public static void transformWithXSLT(Source xmlSource, Source xsltSource, Result result) throws TransformerException {
	    final TransformerFactory tFactory = TransformerFactory.newInstance();
	    final Transformer transformer = tFactory.newTransformer(xsltSource);

	    transformer.transform(xmlSource, result);
	}

	public static String transformToString(File xml) throws TransformerException, IOException {

		final TransformerFactory tFactory = TransformerFactory.newInstance();
	    final Transformer transformer = tFactory.newTransformer();

	    try (final StringWriter writer = new StringWriter()) {
	      transformer.transform(new StreamSource(xml), new StreamResult(writer));

	      return writer.toString();
	    }
	}

	public static String transformWithMemoryXSLT(String xmlContent, String xsltContent) 
			throws TransformerException, IOException {
		
		TransformerFactory tFactory = TransformerFactory.newInstance();
		ByteArrayInputStream arrayXslt = new ByteArrayInputStream(xsltContent.getBytes());
		Transformer transformer = tFactory.newTransformer(new StreamSource(arrayXslt));
		ByteArrayInputStream arrayXml = new ByteArrayInputStream(xmlContent.getBytes());
		try (final StringWriter writer = new StringWriter()) {
			transformer.transform(new StreamSource(arrayXml), new StreamResult(writer));
			return writer.toString();
	    }
	}

}
