import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;

public class FixedUpdateRedisCache {
    public static void main(String[] args) {
        // 连接数据库
        String dbUrl = "jdbc:mysql://localhost/exchange?useSSL=false&allowMultiQueries=true&useUnicode=true&characterEncoding=utf8";
        String dbUsername = "root";
        String dbPassword = "password";
        
        try {
            // 加载MySQL驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 创建数据库连接
            Connection dbConn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            System.out.println("Successfully connected to database exchange");
            
            // 1. 更新K线缓存 - 使用ZADD命令添加到有序集合
            System.out.println("\nUpdating K-line cache with ZADD...");
            updateKLineWithZADD(dbConn);
            
            // 关闭数据库连接
            dbConn.close();
            System.out.println("\nRedis cache updated successfully!");
            
        } catch (Exception e) {
            System.out.println("Failed to update Redis cache: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void updateKLineWithZADD(Connection dbConn) throws Exception {
        // 清空现有的K线数据
        executeCommand("redis-cli DEL _min_bars_");
        
        // 获取最近的K线数据
        PreparedStatement stmt = dbConn.prepareStatement(
            "SELECT startTime, openPrice, highPrice, lowPrice, closePrice, quantity FROM min_bars ORDER BY startTime ASC LIMIT 1440");
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            long startTime = rs.getLong("startTime");
            BigDecimal openPrice = rs.getBigDecimal("openPrice");
            BigDecimal highPrice = rs.getBigDecimal("highPrice");
            BigDecimal lowPrice = rs.getBigDecimal("lowPrice");
            BigDecimal closePrice = rs.getBigDecimal("closePrice");
            BigDecimal quantity = rs.getBigDecimal("quantity");
            
            // 构建K线JSON
            StringBuilder klineJson = new StringBuilder();
            klineJson.append("[").append(startTime).append(",").append(openPrice).append(",").append(highPrice)
                    .append(",").append(lowPrice).append(",").append(closePrice).append("]");
            
            // 使用ZADD添加到有序集合，使用时间戳作为分数
            String command = "redis-cli ZADD _min_bars_ " + startTime + " \"" + 
                            klineJson.toString().replace("\"", "\\\"") + "\"";
            executeCommand(command);
        }
        rs.close();
        stmt.close();
        
        System.out.println("K-line data updated in Redis using ZADD");
    }
    
    private static void executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec("cmd /c " + command);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Command failed: " + command);
            }
        } catch (Exception e) {
            System.out.println("Error executing command: " + command + ", " + e.getMessage());
        }
    }
}