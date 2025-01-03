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

import java.io.IOException;
import java.io.Reader;

import javax.xml.parsers.ParserConfigurationException;

import es.uvigo.esei.dai.hybridserver.sax.ConfigurationContentHandler;
import es.uvigo.esei.dai.hybridserver.sax.SAXParsing;

import org.xml.sax.SAXException;

public class XMLConfigurationLoader {
	public Configuration load(Reader reader) throws Exception {
        // Crear el handler para procesar la configuración
        ConfigurationContentHandler handler = new ConfigurationContentHandler();
		System.out.println("1111111111111111111");

        // Primero validamos y parseamos el XML con el XSD
        try {
            // Validamos y parseamos el archivo XML con el handler
            SAXParsing.parseAndValidateWithExternalXSD(reader, "configuration.xsd", handler);

            // Aquí, el handler ya ha procesado el XML, y podemos obtener la configuración
            Configuration config = handler.getConfig();
            
            if (config == null) {
                throw new Exception("La configuración está vacía.");
            }

            return config;
        } catch (SAXException | IOException | ParserConfigurationException e) {
            System.err.println("Error de validación o parsing: " + e.getMessage());
            throw new Exception("Error al cargar el archivo de configuración: " + e.getMessage());
        }
    }
}




