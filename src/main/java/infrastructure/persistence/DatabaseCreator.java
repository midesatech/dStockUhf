
package infrastructure.persistence;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
public class DatabaseCreator {
    public static void ensureDatabaseExists(String url, String user, String pass){
        try{
            // url like jdbc:mariadb://localhost:3306/inventario
            String baseUrl = url.substring(0, url.lastIndexOf('/'));
            String dbName = url.substring(url.lastIndexOf('/')+1).split("\\?")[0];
            try(Connection c = DriverManager.getConnection(baseUrl + "/", user, pass); Statement s = c.createStatement()){
                s.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
            }
        }catch(Exception ex){
            System.out.println("DB ensure error: " + ex.getMessage());
        }
    }
}
