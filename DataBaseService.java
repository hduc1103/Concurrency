import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;

public class DataBaseService {
    private final Connection connection;

    public DataBaseService() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/concurrency", "root", "123456789");
    }

    public synchronized void saveProductData(String url, String productName ,String productPrice) throws SQLException {
        System.out.println("start");
        String query = "INSERT INTO crawled_data (url, product_name, price) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, url);
            stmt.setString(2, productName);
            stmt.setString(3, productPrice);
            stmt.executeUpdate();
        }
        System.out.println("end");
    }
}
