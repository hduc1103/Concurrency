import java.util.Set;
import java.util.concurrent.*;

public class CrawlerApp {

    public static void main(String[] args) throws Exception {
        DataBaseService dbService = new DataBaseService();

        // Fetch links from the database
        dbService.getLinks();
        BlockingQueue<String> urlQueue = dbService.getUrlQueue();
        System.out.println("Initial total links fetched: " + urlQueue.size());

        // Set for tracking visited URLs
        Set<String> visitedUrls = ConcurrentHashMap.newKeySet();

        long startTime = System.currentTimeMillis();
        System.out.println("Crawling started at: " + startTime);

        // Executor Service with a fixed thread pool
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
                System.out.println("Crawling ended at: " + endTime);
                System.out.println("Total time taken for crawling: " + (endTime - startTime) / 1000 + " seconds.");
                System.out.println("Final queue size (should be 0 if all links processed): " + urlQueue.size());
                System.out.println("Total visited URLs: " + visitedUrls.size());
            }
        }
    }
}
