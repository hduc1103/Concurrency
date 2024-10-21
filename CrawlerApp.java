import java.util.Set;
import java.util.concurrent.*;

public class CrawlerApp {

    public static void main(String[] args) throws Exception {
        DataBaseService dbService = new DataBaseService();

        dbService.getLinks();

        BlockingQueue<String> urlQueue = dbService.getUrlQueue();

        Set<String> visitedUrls = ConcurrentHashMap.newKeySet();

        long startTime = System.currentTimeMillis();
        System.out.println("Start time: " + startTime);

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
                System.out.println("End time: " + endTime);
                System.out.println("Time taken for crawling: " + (endTime - startTime) / 1000 + " seconds.");
            }
        }
    }
}