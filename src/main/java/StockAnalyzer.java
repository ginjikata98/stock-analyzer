import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StockAnalyzer {
    private static final Map<String, Map<String, String>> stockDividendMap = new HashMap<>();

    public static void main(String[] args) throws IOException {

        generateDividendMap();

        try (Stream<Path> paths = Files.walk(Paths.get("stock"))) {
            List<String> files = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().matches("^\\w{3}\\.txt$"))
                    .map(Path::toString)
                    .collect(Collectors.toList());

            StringBuilder winners = new StringBuilder();

            files.forEach(file -> {
                AnalysisResult result = txtAnalyzer(file, true);
                if (result.getRoi() > 7)
                    winners.append(result.getSticker() + "\n");
            });

            File result = new File("winner.txt");
            FileUtils.writeStringToFile(result, winners.toString(), Charset.defaultCharset());
        }
    }

    public static void generateDividendMap() throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get("dividend"))) {
            List<File> files = paths
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .map(File::new)
                    .collect(Collectors.toList());


            for (File file : files) {
                Document document = Jsoup.parse(file, "UTF-8");
                Elements dividends = document.select("#events > table:nth-child(5) > tbody > tr > td:nth-child(2) > table > tbody > tr > td:nth-child(1)");
                Elements dividendDays = document.select("#events > table:nth-child(5) > tbody > tr > td:nth-child(2) > table > tbody > tr > td:nth-child(2)");
                Pattern moneyPattern = Pattern.compile("\\(\\d++");
                Pattern splitPattern = Pattern.compile("\\d+/\\d+");

                Map<String, String> dividendMap = new HashMap<>();

                for (int i = 0; i < dividendDays.size(); i++) {
                    String line = dividends.get(i).text();

                    Matcher moneyMatcher = moneyPattern.matcher(line);
                    Matcher splitMatcher = splitPattern.matcher(line);

                    String dividend = "";
                    if (moneyMatcher.find(0))
                        dividend = moneyMatcher.group(0).replace("(", "");
                    else if (splitMatcher.find(0))
                        dividend = splitMatcher.group(0);
                    else
                        continue;

                    dividendMap.put(dividendDays.get(i).text(), dividend);
                }

                stockDividendMap.put(file.getName().split("\\.")[0], dividendMap);
            }
        }
    }

    public static AnalysisResult txtAnalyzer(String file, boolean verbose) {
        File txt = new File(file);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Set<Integer> buyingDays = new HashSet<>(Arrays.asList(6, 7, 8, 9));
        boolean isBought = false;
        double deposit = 20_000_000F;
        double balance = 0;
        double totalInvested = 0F;
        int totalStockOwned = 0;
        double currentPrice = 0;
        double totalDeposit = 0;
        String sticker = "";

        try {
            List<String> lines = FileUtils.readLines(txt, Charset.defaultCharset());
            Collections.reverse(lines);
            lines.remove(lines.size() - 1);

            String[] firstDayData = lines.get(0).replaceAll("\\s+", "").split(",");
            sticker = firstDayData[0].toUpperCase();
            LocalDate firstDay = LocalDate.parse(firstDayData[1], formatter);

            Map<String, String> dividendMap = stockDividendMap.get(sticker);

            String[] lastDayData = lines.get(lines.size() - 1).replaceAll("\\s+", "").split(",");
            LocalDate lastDay = LocalDate.parse(lastDayData[1], formatter);
            currentPrice = Double.parseDouble(lastDayData[5]) * 1000;

            for (String line : lines) {
                String[] data = line.replaceAll("\\s+", "").split(",");
                LocalDate date = LocalDate.parse(data[1], formatter);

                String dividendPayday = date.format(formatter2);
                String dividendData = dividendMap.get(dividendPayday);

                if (dividendData != null) {
                    if (dividendData.contains("/")) {
                        double first = Double.parseDouble(dividendData.split("/")[0]);
                        double second = Double.parseDouble(dividendData.split("/")[1]);
                        int newStock = (int) (Math.floor(totalStockOwned / first) * second);
                        totalStockOwned += newStock;
                    } else {
                        int dividendMoney = totalStockOwned * Integer.parseInt(dividendData);
                        balance += dividendMoney;
                    }
                }

                if (buyingDays.contains(date.getDayOfMonth()) && !isBought) {
                    double moneyOnHand = deposit + balance;
                    totalDeposit += deposit;
                    double price = Double.parseDouble(data[5]);
                    int stockToBuy = (int) (Math.floor(moneyOnHand / price / 10_000D) * 10);
                    totalStockOwned += stockToBuy;
                    double investedAmount = stockToBuy * price * 1000;
                    balance = moneyOnHand - investedAmount;
                    totalInvested += investedAmount;
                    isBought = true;
                } else if (!buyingDays.contains(date.getDayOfMonth())) {
                    isBought = false;
                }
            }

            double currentNetWorth = totalStockOwned * currentPrice + balance;

            double year = ChronoUnit.DAYS.between(firstDay, lastDay) / 365F;

            double roi = (Math.pow(currentNetWorth / totalInvested, 1 / year) - 1) * 100;

            if (verbose) {
                System.out.printf("========================================%n");
                System.out.printf("===============  %s  ==================%n", sticker);
                System.out.printf("========================================%n");
                System.out.printf("ROI: %f%n", roi);
                System.out.printf("TOTAL NET WORTH: %f%n", currentNetWorth);
                System.out.printf("TOTAL MONEY INVESTED: %f%n", totalInvested);
                System.out.printf("TOTAL STOCKS OWNED: %d%n", totalStockOwned);
                System.out.printf("TOTAL MONEY DEPOSIT: %f%n", totalDeposit);
                System.out.printf("YEARS INVESTED: %f%n", year);
                System.out.printf("========================================%n");
                System.out.printf("========================================%n");
                System.out.printf("%n%n");
            }

            return new AnalysisResult(roi, sticker, currentNetWorth, totalInvested, totalStockOwned, totalDeposit, year);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
