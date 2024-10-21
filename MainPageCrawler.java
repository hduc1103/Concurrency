import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class MainPageCrawler {

    private static final DataBaseService dataBaseService;

    static {
        try {
            dataBaseService = new DataBaseService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Void LinkCrawler(String main_link) throws InterruptedException, SQLException {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\DucMH2\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver(options);
        driver.get(main_link);

        JavascriptExecutor js = (JavascriptExecutor) driver;

        long lastHeight = (long) js.executeScript("return document.body.scrollHeight");

        while (true) {
            // Scroll down the page
            js.executeScript("window.scrollBy(0, 500);");

            // Look for the "Xem thêm" button and click if it is present
            try {
                WebElement showMoreButton = driver.findElement(By.cssSelector("a.button btn-show-more button__show-more-product"));
                if (showMoreButton.isDisplayed()) {
                    showMoreButton.click();
                    // Wait for the page to load after clicking
                    TimeUnit.SECONDS.sleep(5);
                }
            } catch (Exception e) {
                // Button not found or not clickable; proceed with scrolling
            }

            // Wait for the page to load more content
            Thread.sleep(10000);

            // Get the new height after scrolling
            long newHeight = (long) js.executeScript("return document.body.scrollHeight");

            // Check if the page has finished loading (no more new content)
            if (newHeight == lastHeight) {
                break;
            }
            lastHeight = newHeight;
        }

        // Get the page source and parse it with Jsoup
        String pageSource = driver.getPageSource();
        Document doc = Jsoup.parse(pageSource);
        Elements links = doc.select("div.product-info a");
        System.out.println(links.size());
        for (Element link : links) {
            String productLink = link.attr("href");
            System.out.println(productLink);
            dataBaseService.saveLink(productLink);
        }

        // Quit the driver after completion
        driver.quit();
        return null;
    }

}
