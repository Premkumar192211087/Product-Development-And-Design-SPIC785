package com.example.stockpilot;

public class InvoiceModel {
    private int invoiceId;
    private String invoiceNumber;
    private int customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private int storeId;
    private String storeName;
    private String issueDate;
    private String dueDate;
    private String status;
    private String displayStatus;
    private double subtotal;
    private double tax;
    private double discount;
    private double total;
    private String formattedTotal;
    private String notes;
    private boolean isOverdue;
    private int daysOverdue;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public InvoiceModel() {}

    // Getters and Setters
    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public int getStoreId() { return storeId; }
    public void setStoreId(int storeId) { this.storeId = storeId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDisplayStatus() { return displayStatus; }
    public void setDisplayStatus(String displayStatus) { this.displayStatus = displayStatus; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getFormattedTotal() { return formattedTotal; }
    public void setFormattedTotal(String formattedTotal) { this.formattedTotal = formattedTotal; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isOverdue() { return isOverdue; }
    public void setOverdue(boolean overdue) { isOverdue = overdue; }

    public int getDaysOverdue() { return daysOverdue; }
    public void setDaysOverdue(int daysOverdue) { this.daysOverdue = daysOverdue; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
