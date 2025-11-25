package com.example.stockpilot;

public class InvoiceStatistics {
    private int totalInvoices;
    private int paidInvoices;
    private int unpaidInvoices;
    private int overdueInvoices;
    private double totalAmount;
    private double paidAmount;
    private double outstandingAmount;

    // Constructors
    public InvoiceStatistics() {}

    // Getters and Setters
    public int getTotalInvoices() { return totalInvoices; }
    public void setTotalInvoices(int totalInvoices) { this.totalInvoices = totalInvoices; }

    public int getPaidInvoices() { return paidInvoices; }
    public void setPaidInvoices(int paidInvoices) { this.paidInvoices = paidInvoices; }

    public int getUnpaidInvoices() { return unpaidInvoices; }
    public void setUnpaidInvoices(int unpaidInvoices) { this.unpaidInvoices = unpaidInvoices; }

    public int getOverdueInvoices() { return overdueInvoices; }
    public void setOverdueInvoices(int overdueInvoices) { this.overdueInvoices = overdueInvoices; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public double getOutstandingAmount() { return outstandingAmount; }
    public void setOutstandingAmount(double outstandingAmount) { this.outstandingAmount = outstandingAmount; }
}

