package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private double roi;
    private String sticker;
    private double currentNetWorth;
    private double totalInvested;
    private double totalStockOwned;
    private double totalDeposit;
    private double year;
}