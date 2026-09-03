package com.clover.sdk.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Payment {
    private String id;
    private Long amount;
    private Long tipAmount;
    private Long cashTendered;
    private String result;
    private Long createdTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    public Long getTipAmount() { return tipAmount; }
    public void setTipAmount(Long tipAmount) { this.tipAmount = tipAmount; }

    public Long getCashTendered() { return cashTendered; }
    public void setCashTendered(Long cashTendered) { this.cashTendered = cashTendered; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public Long getCreatedTime() { return createdTime; }
    public void setCreatedTime(Long createdTime) { this.createdTime = createdTime; }

    public String getAmountFormatted() {
        if (amount == null) return "$0.00";
        return String.format("$%.2f", amount / 100.0);
    }

    @Override
    public String toString() {
        return "Payment{id='" + id + "', amount=" + getAmountFormatted() + ", result='" + result + "'}";
    }
}
