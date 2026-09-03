package com.clover.sdk.model.response;

import com.clover.sdk.internal.CloverList;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    private String id;
    private String state;
    private Long total;
    private String note;
    private Long createdTime;
    private Long clientCreatedTime;
    private OrderType orderType;
    private Employee employee;
    private CloverList<LineItem> lineItems;
    private CloverList<Customer> customers;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Long getCreatedTime() { return createdTime; }
    public void setCreatedTime(Long createdTime) { this.createdTime = createdTime; }

    public Long getClientCreatedTime() { return clientCreatedTime; }
    public void setClientCreatedTime(Long clientCreatedTime) { this.clientCreatedTime = clientCreatedTime; }

    public OrderType getOrderType() { return orderType; }
    public void setOrderType(OrderType orderType) { this.orderType = orderType; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public CloverList<LineItem> getLineItems() { return lineItems; }
    public void setLineItems(CloverList<LineItem> lineItems) { this.lineItems = lineItems; }

    public CloverList<Customer> getCustomers() { return customers; }
    public void setCustomers(CloverList<Customer> customers) { this.customers = customers; }

    public List<LineItem> getLineItemList() {
        return lineItems != null ? lineItems.getElements() : Collections.emptyList();
    }

    public List<Customer> getCustomerList() {
        return customers != null ? customers.getElements() : Collections.emptyList();
    }

    public String getTotalFormatted() {
        if (total == null) return "$0.00";
        return String.format("$%.2f", total / 100.0);
    }

    @Override
    public String toString() {
        return "Order{id='" + id + "', state='" + state + "', total=" + getTotalFormatted() + "}";
    }
}
