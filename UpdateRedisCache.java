import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;

public class UpdateRedisCache {
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
            
            // 1. 更新订单簿缓存
            System.out.println("\nUpdating order book cache...");
            updateOrderBook(dbConn);
            
            // 2. 更新ticks缓存
            System.out.println("\nUpdating ticks cache...");
            updateTicks(dbConn);
            
            // 3. 更新K线缓存
            System.out.println("\nUpdating K-line cache...");
            updateKLine(dbConn);
            
            // 关闭数据库连接
            dbConn.close();
            System.out.println("\nRedis cache updated successfully!");
            
        } catch (Exception e) {
            System.out.println("Failed to update Redis cache: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void updateOrderBook(Connection dbConn) throws Exception {
        // 获取买入订单
        List<Map<String, Object>> buyOrders = new ArrayList<>();
        PreparedStatement buyStmt = dbConn.prepareStatement(
            "SELECT price, SUM(unfilledQuantity) as totalQuantity FROM orders WHERE direction = 'BUY' AND status = 'PENDING' GROUP BY price ORDER BY price DESC LIMIT 5");
        ResultSet buyRs = buyStmt.executeQuery();
        while (buyRs.next()) {
            Map<String, Object> order = new HashMap<>();
            order.put("price", buyRs.getBigDecimal("price"));
            order.put("quantity", buyRs.getBigDecimal("totalQuantity"));
            buyOrders.add(order);
        }
        buyRs.close();
        buyStmt.close();
        
        // 获取卖出订单
        List<Map<String, Object>> sellOrders = new ArrayList<>();
        PreparedStatement sellStmt = dbConn.prepareStatement(
            "SELECT price, SUM(unfilledQuantity) as totalQuantity FROM orders WHERE direction = 'SELL' AND status = 'PENDING' GROUP BY price ORDER BY price ASC LIMIT 5");
        ResultSet sellRs = sellStmt.executeQuery();
        while (sellRs.next()) {
            Map<String, Object> order = new HashMap<>();
            order.put("price", sellRs.getBigDecimal("price"));
            order.put("quantity", sellRs.getBigDecimal("totalQuantity"));
            sellOrders.add(order);
        }
        sellRs.close();
        sellStmt.close();
        
        // 获取市场价格
        BigDecimal marketPrice = BigDecimal.ZERO;
        if (buyOrders.size() > 0 && sellOrders.size() > 0) {
            BigDecimal bestBuyPrice = (BigDecimal) buyOrders.get(0).get("price");
            BigDecimal bestSellPrice = (BigDecimal) sellOrders.get(0).get("price");
            marketPrice = bestBuyPrice.add(bestSellPrice).divide(new BigDecimal(2));
        }
        
        // 构建订单簿JSON
        StringBuilder orderBookJson = new StringBuilder();
        orderBookJson.append("{\"sequenceId\":1000,\"marketPrice\":").append(marketPrice).append(",\"buy\":[");
        for (int i = 0; i < buyOrders.size(); i++) {
            if (i > 0) orderBookJson.append(",");
            Map<String, Object> order = buyOrders.get(i);
            orderBookJson.append("{\"price\":").append(order.get("price")).append(",\"quantity\":").append(order.get("quantity")).append("}");
        }
        orderBookJson.append("],\"sell\":[");
        for (int i = 0; i < sellOrders.size(); i++) {
            if (i > 0) orderBookJson.append(",");
            Map<String, Object> order = sellOrders.get(i);
            orderBookJson.append("{\"price\":").append(order.get("price")).append(",\"quantity\":").append(order.get("quantity")).append("}");
        }
        orderBookJson.append("]}");
        
        System.out.println("Order Book JSON: " + orderBookJson.toString());
        
        // 使用redis-cli更新缓存
        executeCommand("redis-cli SET _orderbook_ \"" + orderBookJson.toString().replace("\"", "\\\"") + "\"");
    }
    
    private static void updateTicks(Connection dbConn) throws Exception {
        // 获取最近的交易记录
        PreparedStatement stmt = dbConn.prepareStatement(
            "SELECT t.createdAt, t.takerDirection, t.price, t.quantity FROM ticks t ORDER BY t.createdAt DESC LIMIT 8");
        ResultSet rs = stmt.executeQuery();
        
        StringBuilder ticksJson = new StringBuilder();
        ticksJson.append("[");
        boolean first = true;
        while (rs.next()) {
            if (!first) ticksJson.append(",");
            long timestamp = rs.getLong("createdAt");
            boolean takerDirection = rs.getBoolean("takerDirection");
            BigDecimal price = rs.getBigDecimal("price");
            BigDecimal quantity = rs.getBigDecimal("quantity");
            
            ticksJson.append("[").append(timestamp).append(",").append(takerDirection ? 1 : 0)
                      .append(",").append(price).append(",").append(quantity).append("]");
            first = false;
        }
        ticksJson.append("]");
        rs.close();
        stmt.close();
        
        System.out.println("Ticks JSON: " + ticksJson.toString());
        
        // 清空并更新Redis中的ticks列表
        executeCommand("redis-cli DEL _ticks_");
        executeCommand("redis-cli LPUSH _ticks_ \"" + ticksJson.toString().replace("\"", "\\\"") + "\"");
    }
    
    private static void updateKLine(Connection dbConn) throws Exception {
        // 获取最近的K线数据
        PreparedStatement stmt = dbConn.prepareStatement(
            "SELECT startTime, openPrice, highPrice, lowPrice, closePrice, quantity FROM min_bars ORDER BY startTime DESC LIMIT 100");
        ResultSet rs = stmt.executeQuery();
        
        StringBuilder klineJson = new StringBuilder();
        klineJson.append("[");
        boolean first = true;
        while (rs.next()) {
            if (!first) klineJson.append(",");
            long startTime = rs.getLong("startTime");
            BigDecimal openPrice = rs.getBigDecimal("openPrice");
            BigDecimal highPrice = rs.getBigDecimal("highPrice");
            BigDecimal lowPrice = rs.getBigDecimal("lowPrice");
            BigDecimal closePrice = rs.getBigDecimal("closePrice");
            BigDecimal quantity = rs.getBigDecimal("quantity");
            
            klineJson.append("[").append(startTime).append(",").append(openPrice).append(",").append(highPrice)
                    .append(",").append(lowPrice).append(",").append(closePrice).append("]");
            first = false;
        }
        klineJson.append("]");
        rs.close();
        stmt.close();
        
        System.out.println("K-line JSON: " + klineJson.toString());
        
        // 更新Redis中的K线数据
        executeCommand("redis-cli SET _min_bars_ \"" + klineJson.toString().replace("\"", "\\\"") + "\"");
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