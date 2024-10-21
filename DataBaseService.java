import java.sql.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DataBaseService {
    private final Connection connection;
    private final BlockingQueue<String> urlQueue = new LinkedBlockingQueue<>();

    public DataBaseService() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/concurrency", "root", "123456789");
    }

    public synchronized void saveProductData(String url, String productName, String type, String productPrice) throws SQLException {
        String query = "INSERT INTO crawled_data (url, product_name, type, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, url);
            stmt.setString(2, productName);
            stmt.setString(3, type);
            stmt.setString(4, productPrice);
            stmt.executeUpdate();
        }
    }

    public void saveLink(String link) throws SQLException {
        String query = "INSERT INTO links (url) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, link);
            stmt.executeUpdate();
        }
    }

    public void getLinks() throws SQLException {
        String query = "SELECT * FROM links";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet resultSet = stmt.executeQuery();

            // Process the result set and add URLs to the BlockingQueue
            while (resultSet.next()) {
                String url = resultSet.getString("url");

                // Add the URL to the queue
                urlQueue.add(url);
            }
        }
    }

    public BlockingQueue<String> getUrlQueue() {
        return urlQueue;
    }
}
