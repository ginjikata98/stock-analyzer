import java.io.IOException;

public class StockCrawler {
    public static void main(String[] args) throws InterruptedException, IOException {
        Downloader.downloadFromTxt("stock.txt", "dividend");
        FileMover.moveAllFile("stock");
    }
}
