import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReBalancing {
    public static void main(String[] args) {
        try {
            Set<String> winners = FileUtils.readLines(new File("winner.txt"), Charset.defaultCharset())
                    .stream().map(winner -> winner.replaceAll("\\s+", ""))
                    .collect(Collectors.toSet());

            List<String> holdings = FileUtils.readLines(new File("currentHolding.txt"), Charset.defaultCharset());

            List<String> stocksToSell = new ArrayList<>();

            for (String holding : holdings) {
                if (!winners.contains(holding)) {
                    stocksToSell.add(holding);
                } else {
                    winners.remove(holding);
                }
            }

            System.out.println("Stocks to buy: " + winners);
            System.out.println("Stocks to sell: " + stocksToSell);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
