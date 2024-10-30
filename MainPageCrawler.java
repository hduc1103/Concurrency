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
            js.executeScript("window.scrollBy(0, 500);");

            try {
                WebElement showMoreButton = driver.findElement(By.cssSelector("a.button btn-show-more button__show-more-product"));
                if (showMoreButton.isDisplayed()) {
                    showMoreButton.click();
                    TimeUnit.SECONDS.sleep(5);
                }
            } catch (Exception e) {
            }

            Thread.sleep(10000);

            long newHeight = (long) js.executeScript("return document.body.scrollHeight");

            if (newHeight == lastHeight) {
                break;
            }
            lastHeight = newHeight;
        }

        String pageSource = driver.getPageSource();
        Document doc = Jsoup.parse(pageSource);
        Elements links = doc.select("div.product-info a");
        System.out.println(links.size());
        for (Element link : links) {
            String productLink = link.attr("href");
            System.out.println(productLink);
            dataBaseService.saveLink(productLink);
        }

        driver.quit();
        return null;
    }

}
