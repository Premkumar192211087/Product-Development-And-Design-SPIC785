package com.example.stockpilot;

import java.util.Map;
import java.util.List;

public class FinancialReportModel {
    public int totalSales;
    public double totalRevenue;
    public double totalReturns;
    public double netProfit;

    public int paidInvoices;
    public int unpaidInvoices;
    public int partialInvoices;

    public Map<String, Double> paymentMethods;

    public List<ChartPoint> chartData;

    public static class ChartPoint {
        public String period;
        public float revenue;

        public ChartPoint(String period, float revenue) {
            this.period = period;
            this.revenue = revenue;
        }
    }
}
