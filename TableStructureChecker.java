import java.sql.*;
import java.util.Properties;

public class TableStructureChecker {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost/exchange?useSSL=false&allowMultiQueries=true&useUnicode=true&characterEncoding=utf8";
        String username = "root";
        String password = "password";
        
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Create connection
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Successfully connected to database exchange");
            
            // Check orders table structure
            System.out.println("\nOrders table structure:");
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "orders", null);
            while (columns.next()) {
                System.out.println("Column: " + columns.getString("COLUMN_NAME") + 
                                  ", Type: " + columns.getString("TYPE_NAME") + 
                                  ", Size: " + columns.getString("COLUMN_SIZE"));
            }
            columns.close();
            
            // Check ticks table structure
            System.out.println("\nTicks table structure:");
            columns = metaData.getColumns(null, null, "ticks", null);
            while (columns.next()) {
                System.out.println("Column: " + columns.getString("COLUMN_NAME") + 
                                  ", Type: " + columns.getString("TYPE_NAME") + 
                                  ", Size: " + columns.getString("COLUMN_SIZE"));
            }
            columns.close();
            
            // Check min_bars table structure
            System.out.println("\nMin_bars table structure:");
            columns = metaData.getColumns(null, null, "min_bars", null);
            while (columns.next()) {
                System.out.println("Column: " + columns.getString("COLUMN_NAME") + 
                                  ", Type: " + columns.getString("TYPE_NAME") + 
                                  ", Size: " + columns.getString("COLUMN_SIZE"));
            }
            columns.close();
            
            // Close connection
            conn.close();
            
        } catch (Exception e) {
            System.out.println("Failed to check table structure: " + e.getMessage());
            e.printStackTrace();
        }
    }
}