package com.clover.sdk.api;

import com.clover.sdk.exception.CloverException;
import com.clover.sdk.internal.CloverHttpClient;
import com.clover.sdk.internal.CloverList;
import com.clover.sdk.model.response.Employee;
import com.clover.sdk.model.response.Merchant;
import com.clover.sdk.model.response.OrderType;
import com.clover.sdk.model.response.Tender;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;

public class MerchantApi {

    private final CloverHttpClient http;

    public MerchantApi(CloverHttpClient http) {
        this.http = http;
    }

    public Merchant getMerchant() throws CloverException {
        String path = "/v3/merchants/" + http.getMerchantId() + "?expand=address";
        JsonNode node = http.get(path);
        try {
            return http.mapper.treeToValue(node, Merchant.class);
        } catch (Exception e) {
            throw new CloverException("Failed to parse merchant response", e);
        }
    }

    public List<OrderType> getOrderTypes() throws CloverException {
        String path = "/v3/merchants/" + http.getMerchantId() + "/order_types";
        JsonNode node = http.get(path);
        return parseElements(node, new TypeReference<CloverList<OrderType>>() {});
    }

    public List<Tender> getTenders() throws CloverException {
        String path = "/v3/merchants/" + http.getMerchantId() + "/tenders";
        JsonNode node = http.get(path);
        return parseElements(node, new TypeReference<CloverList<Tender>>() {});
    }

    public List<Employee> getEmployees() throws CloverException {
        String path = "/v3/merchants/" + http.getMerchantId() + "/employees";
        JsonNode node = http.get(path);
        return parseElements(node, new TypeReference<CloverList<Employee>>() {});
    }

    /**
     * Selects the best tender for recording a prepaid payment:
     * prefers "Cash" (Clover's generic external-payment tender), then first available.
     */
    String resolveTenderId() throws CloverException {
        List<Tender> tenders = getTenders();
        if (tenders.isEmpty()) {
            throw new CloverException("No tenders found for merchant — cannot record prepaid payment");
        }
        return tenders.stream()
                .filter(Tender::isCash)
                .findFirst()
                .orElse(tenders.get(0))
                .getId();
    }

    /**
     * Returns the first employee ID, or null if no employees exist or the token
     * lacks the Employees permission.
     */
    String resolveEmployeeId() {
        try {
            List<Employee> employees = getEmployees();
            return employees.isEmpty() ? null : employees.get(0).getId();
        } catch (CloverException e) {
            return null;
        }
    }

    /**
     * Selects the best order type for an order:
     * prefers dine-in/in-store types, falls back to the default, then first visible.
     */
    String resolveOrderTypeId() throws CloverException {
        List<OrderType> types = getOrderTypes();
        if (types.isEmpty()) return null;

        OrderType preferred = types.stream()
                .filter(t -> !Boolean.TRUE.equals(t.getIsHidden()))
                .filter(t -> {
                    String label = t.getDisplayLabel();
                    return label != null && label.toLowerCase().matches(".*?(dine.?in|in.?store|walk.?in|counter).*");
                })
                .findFirst()
                .orElse(null);

        if (preferred != null) return preferred.getId();

        return types.stream()
                .filter(t -> !Boolean.TRUE.equals(t.getIsHidden()))
                .filter(t -> Boolean.TRUE.equals(t.getIsDefault()))
                .findFirst()
                .or(() -> types.stream().filter(t -> !Boolean.TRUE.equals(t.getIsHidden())).findFirst())
                .map(OrderType::getId)
                .orElse(types.get(0).getId());
    }

    private <T> List<T> parseElements(JsonNode node, TypeReference<CloverList<T>> typeRef) throws CloverException {
        try {
            CloverList<T> page = http.mapper.readValue(http.mapper.treeAsTokens(node), typeRef);
            return page.getElements();
        } catch (Exception e) {
            throw new CloverException("Failed to parse Clover response", e);
        }
    }
}
