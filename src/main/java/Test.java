import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Test {
    public static void main(String[] args) throws IOException {

        try (Stream<Path> paths = Files.walk(Paths.get("dividend"))) {
            List<File> files = paths
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .map(File::new)
                    .collect(Collectors.toList());

            Map<String, Map<String, String>> stockDividendMap = new HashMap<>();

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

            System.out.println(stockDividendMap);
        }
    }
}
