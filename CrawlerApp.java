import java.util.Set;
import java.util.concurrent.*;

public class CrawlerApp {

    public static void main(String[] args) throws Exception {
        BlockingQueue<String> urlQueue = new LinkedBlockingQueue<>();
        Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
        DataBaseService dbService = new DataBaseService();
        urlQueue.put("https://cellphones.com.vn/iphone-14-pro-max.html");
        urlQueue.put("https://cellphones.com.vn/tecno-spark-20.html");

        long startTime = System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        try {
            for (int i = 0; i < 10; i++) {
                executorService.submit(new WebCrawler(urlQueue, visitedUrls, dbService));
            }
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            } finally {
                long endTime = System.currentTimeMillis();
                System.out.println("Time taken for crawling: " + (endTime - startTime)/1000/60 + " minutes.");
            }
        }
    }
}
