import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Random;

public class GenerateTestData {
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
            
            Random random = new Random();
            
            // 生成一些测试订单
            System.out.println("Creating test orders...");
            for (int i = 0; i < 20; i++) {
                long userId = 1000 + random.nextInt(10);
                boolean isBuy = random.nextBoolean();
                BigDecimal price = new BigDecimal(20000 + random.nextInt(2000) + random.nextDouble()).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal quantity = new BigDecimal(0.01 + random.nextDouble() * 2).setScale(4, BigDecimal.ROUND_HALF_UP);
                
                String sql = "INSERT INTO orders (id, userId, direction, price, quantity, unfilledQuantity, status, sequenceId, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setLong(1, 10000 + i);
                stmt.setLong(2, userId);
                stmt.setString(3, isBuy ? "BUY" : "SELL");
                stmt.setBigDecimal(4, price);
                stmt.setBigDecimal(5, quantity);
                stmt.setBigDecimal(6, quantity);  // 初始未成交数量等于总数量
                stmt.setString(7, "PENDING");  // 待成交状态
                stmt.setLong(8, 1000 + i); // sequenceId
                stmt.setLong(9, System.currentTimeMillis() - random.nextInt(86400000)); // 随机时间
                stmt.setLong(10, System.currentTimeMillis() - random.nextInt(3600000));
                stmt.executeUpdate();
                stmt.close();
            }
            
            // 生成一些交易ticks
            System.out.println("Creating test ticks...");
            for (int i = 0; i < 30; i++) {
                long takerOrderId = 10000 + i; // 确保唯一
                long makerOrderId = 10000 + (i + 1) % 20; // 确保唯一
                BigDecimal price = new BigDecimal(20000 + random.nextInt(2000) + random.nextDouble()).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal quantity = new BigDecimal(0.01 + random.nextDouble() * 2).setScale(4, BigDecimal.ROUND_HALF_UP);
                boolean isBuy = random.nextBoolean();
                
                String sql = "INSERT INTO ticks (takerOrderId, makerOrderId, price, quantity, takerDirection, sequenceId, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setLong(1, takerOrderId);
                stmt.setLong(2, makerOrderId);
                stmt.setBigDecimal(3, price);
                stmt.setBigDecimal(4, quantity);
                stmt.setBoolean(5, isBuy); // takerDirection是BIT类型，使用setBoolean
                stmt.setLong(6, 1000 + i); // sequenceId
                stmt.setLong(7, System.currentTimeMillis() - random.nextInt(3600000));
                stmt.executeUpdate();
                stmt.close();
            }
            
            // 生成一些K线数据（分钟K线）
            System.out.println("Creating test K-line data...");
            for (int i = 0; i < 1440; i++) { // 最近24小时的分钟K线
                long timestamp = System.currentTimeMillis() - (1440 - i) * 60000; // 每分钟一条
                BigDecimal basePrice = new BigDecimal(21000 + random.nextInt(1000) - 500).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal high = basePrice.add(new BigDecimal(50 + random.nextInt(200)).setScale(2, BigDecimal.ROUND_HALF_UP));
                BigDecimal low = basePrice.subtract(new BigDecimal(50 + random.nextInt(200)).setScale(2, BigDecimal.ROUND_HALF_UP));
                BigDecimal range = high.subtract(low);
                BigDecimal close = low.add(range.multiply(new BigDecimal(Math.random()))).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal volume = new BigDecimal(0.01 + random.nextDouble() * 10).setScale(4, BigDecimal.ROUND_HALF_UP);
                
                String sql = "INSERT INTO min_bars (startTime, openPrice, highPrice, lowPrice, closePrice, quantity) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setLong(1, timestamp);
                stmt.setBigDecimal(2, basePrice);
                stmt.setBigDecimal(3, high);
                stmt.setBigDecimal(4, low);
                stmt.setBigDecimal(5, close);
                stmt.setBigDecimal(6, volume);
                stmt.executeUpdate();
                stmt.close();
            }
            
            // 关闭连接
            conn.close();
            System.out.println("Test data created successfully!");
            
        } catch (Exception e) {
            System.out.println("Failed to create test data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}