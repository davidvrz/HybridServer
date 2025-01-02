/**
 *  HybridServer
 *  Copyright (C) 2024 Miguel Reboiro-Jato
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.uvigo.esei.dai.hybridserver;

import java.io.File;

import es.uvigo.esei.dai.hybridserver.sax.ConfigurationContentHandler;
import es.uvigo.esei.dai.hybridserver.sax.SAXParserImplementation;



public class XMLConfigurationLoader {
	
	// Implementar en la semana 9.
	public Configuration load(File xmlFile) throws Exception {
		
		ConfigurationContentHandler confContentHandler = new ConfigurationContentHandler();
		SAXParserImplementation.parseAndValidateWithExternalXSD(xmlFile.getPath(),"configuration.xsd", confContentHandler);
		//SAXParserImplementation.parseAndValidateWithInternalXSD(xmlFile.getPath(), confContentHandler); 
		return confContentHandler.getConfig();
	}
}



