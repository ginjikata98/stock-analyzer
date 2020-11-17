import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Metrics {
    public static void main(String[] args) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate start = LocalDate.parse("18/12/2018", formatter);
        LocalDate today = LocalDate.parse("17/11/2020", formatter);

        double totalInvested = 202_350_675;
        double netWorth = 230_587_515;

        double vnIndexStart = 933.65;
        double vnIndexToday = 918.84;

        double year = ChronoUnit.DAYS.between(start, today) / 365F;

        double myROI = (Math.pow(netWorth / totalInvested, 1 / year) - 1) * 100;

        double vnIndexROI = (Math.pow(vnIndexToday / vnIndexStart, 1 / year) - 1) * 100;

        System.out.println(myROI);
        System.out.println(vnIndexROI);
        System.out.println(myROI - vnIndexROI);

    }
}
