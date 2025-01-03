package es.uvigo.esei.dai.hybridserver;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Launcher {
    public static void main(String[] args) {
        HybridServer server;
        
        if (args.length == 0) {
            // Si no se pasa ningún parámetro, se usa el constructor por defecto
            server = new HybridServer();
        } else if (args.length == 1) {
            // Si se pasa un solo parámetro, se carga la configuración desde un archivo XML
            File configFile = new File(args[0]);

            try (FileReader reader = new FileReader(configFile)) {
                // Cargar la configuración desde el archivo XML usando XMLConfigurationLoader
                XMLConfigurationLoader configLoader = new XMLConfigurationLoader();
                Configuration configuration = configLoader.load(reader);
                System.out.println(configuration);

                // Crear el servidor usando la configuración cargada
                server = new HybridServer(configuration);
            } catch (Exception e) {
                System.err.println("Error al cargar el archivo de configuración: " + e.getMessage());
                return; 
            }
        } else {
            System.err.println("Error: demasiados parámetros. Usa solo un archivo de configuración o ninguno.");
            return;
        }

        // Iniciar el servidor
        server.start();
        System.out.println("Servidor iniciado en el puerto " + server.getPort());
    }
}
