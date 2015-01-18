package pl.polsl.java.lab4.tomasz.domaracki.JavaBillsCollectionServerDemo;

import java.io.IOException;
import pl.polsl.java.lab4.tomasz.domaracki.JavaBillsCollectionServerModel.Server;
/**
 * Class presenting the use of classes in project
 *
 * @author tomaszdomaracki
 * @version 1.0.0
 */
public class JavaBillsCollectionServerDemo {
    
    /**
     * 
     * @param args 
     */
    public static void main(String args[]){
       
        try{
            new Server();
        }
        catch(IOException ex){
            System.err.println(ex.getMessage());
        }
    }
}
