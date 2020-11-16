import org.apache.commons.io.FileUtils;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StockAnalyzer {
    public static void main(String[] args) throws IOException {

        try (Stream<Path> paths = Files.walk(Paths.get("stock"))) {
            List<String> files = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().matches("^\\w{3}\\.txt$"))
                    .map(Path::toString)
                    .collect(Collectors.toList());

            StringBuilder winners = new StringBuilder();

            files.forEach(file -> {
                AnalysisResult result = txtAnalyzer(file, false);
                if (result.getRoi() > 7)
                    winners.append(result.getSticker() + "\n");
            });

            File result = new File("winner.txt");
            FileUtils.writeStringToFile(result, winners.toString(), Charset.defaultCharset());
        }
    }

    public static AnalysisResult txtAnalyzer(String file, boolean verbose) {
        File txt = new File(file);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
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
            sticker = firstDayData[0];
            LocalDate firstDay = LocalDate.parse(firstDayData[1], formatter);

            String[] lastDayData = lines.get(lines.size() - 1).replaceAll("\\s+", "").split(",");
            LocalDate lastDay = LocalDate.parse(lastDayData[1], formatter);
            currentPrice = Double.parseDouble(lastDayData[5]) * 1000;

            for (String line : lines) {
                String[] data = line.replaceAll("\\s+", "").split(",");
                LocalDate date = LocalDate.parse(data[1], formatter);
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
                System.out.printf("===============  %s  ==================%n", sticker.toUpperCase());
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
