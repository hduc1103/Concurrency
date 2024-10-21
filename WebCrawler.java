import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class WebCrawler implements Runnable {
    private final BlockingQueue<String> urlQueue;
    private final Set<String> visitedUrls;
    private final DataBaseService dbService;

    public WebCrawler(BlockingQueue<String> urlQueue, Set<String> visitedUrls, DataBaseService dbService) {
        this.urlQueue = urlQueue;
        this.visitedUrls = visitedUrls;
        this.dbService = dbService;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String url = urlQueue.take();
                System.out.println("Processing URL: " + url);

                if (visitedUrls.contains(url)) continue;

                try {
                    Document doc = Jsoup.connect(url).get();

                    String product_name = doc.select("div.box-product-name h1").first().text();
                    String price = doc.select("span.item-variant-price").first() != null
                            ? doc.select("span.item-variant-price").first().text()
                            : doc.select("span.data-v-97d76036").first().text();

                    String product_type = doc.select("strong.item-variant-name").first().text();

                    System.out.println(product_name + " " + product_type + " " + price);

                    dbService.saveProductData(url, product_name, product_type, price);

                    visitedUrls.add(url);

                    Elements links = doc.select("a[href]");
                    for (Element link : links) {
                        String nextUrl = link.absUrl("href");

                        if (!visitedUrls.contains(nextUrl) && !nextUrl.isEmpty()) {
                            urlQueue.put(nextUrl);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error crawling " + url + ": " + e.getMessage());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted.");
        }
    }
}
