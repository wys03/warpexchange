import java.sql.*;
import java.util.Properties;

public class DatabaseInspector {
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
            
            // Get metadata
            DatabaseMetaData metaData = conn.getMetaData();
            System.out.println("\nDatabase information:");
            System.out.println("Database product name: " + metaData.getDatabaseProductName());
            System.out.println("Database product version: " + metaData.getDatabaseProductVersion());
            
            // Get all tables
            System.out.println("\nTables in database:");
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("- " + tableName);
                
                // Get row count
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
                if (rs.next()) {
                    System.out.println("  Row count: " + rs.getInt(1));
                }
                rs.close();
                stmt.close();
            }
            tables.close();
            
            // Close connection
            conn.close();
        } catch (Exception e) {
            System.out.println("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}