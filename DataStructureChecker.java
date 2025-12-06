import java.sql.*;

public class DataStructureChecker {
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
            
            // Check orders data
            System.out.println("\nOrders data (first 5 rows):");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM orders LIMIT 5");
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Print column headers
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i) + "\t");
            }
            System.out.println();
            
            // Print data rows
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
            rs.close();
            
            // Check ticks data
            System.out.println("\nTicks data (first 5 rows):");
            rs = stmt.executeQuery("SELECT * FROM ticks LIMIT 5");
            metaData = rs.getMetaData();
            columnCount = metaData.getColumnCount();
            
            // Print column headers
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i) + "\t");
            }
            System.out.println();
            
            // Print data rows
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
            rs.close();
            
            // Check min_bars data
            System.out.println("\nMin_bars data (first 5 rows):");
            rs = stmt.executeQuery("SELECT * FROM min_bars LIMIT 5");
            metaData = rs.getMetaData();
            columnCount = metaData.getColumnCount();
            
            // Print column headers
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i) + "\t");
            }
            System.out.println();
            
            // Print data rows
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
            rs.close();
            stmt.close();
            
            // Close connection
            conn.close();
            
        } catch (Exception e) {
            System.out.println("Failed to check data structure: " + e.getMessage());
            e.printStackTrace();
        }
    }
}