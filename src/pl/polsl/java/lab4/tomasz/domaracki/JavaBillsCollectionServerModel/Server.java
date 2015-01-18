
package pl.polsl.java.lab4.tomasz.domaracki.JavaBillsCollectionServerModel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class designed to become a model in MVC architecture
 * Create connection between client and server
 *
 * @author tomaszdomaracki
 * @version 1.0.0
 */
public class Server {
    /**
     * 
     */
    static final int PORT = 9991;
    
    /**
     * 
     * @throws java.io.IOException 
     */
    public Server() throws IOException{
        
        ServerSocket server = new ServerSocket(PORT);
        
        while(true){
            Socket socket = server.accept();
            new ConnectionHandler(socket);
        }
    }
}
