package com.bng.ordering;

import com.clover.sdk.CloverClient;
import com.clover.sdk.PaymentType;
import com.clover.sdk.exception.CloverException;
import com.clover.sdk.model.response.Order;
import com.clover.sdk.model.response.OrderResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private CloverClient cloverClient;

    private static final String MOCK_ORDER_ID = "TEST-CLOVER-ORDER-ID";

    private static final String VALID_ORDER = """
        {
          "customerName": "Test User",
          "customerPhone": "6785551234",
          "items": [
            { "itemId": "pop-001", "name": "Chicken Dum Biryani", "price": 1899, "quantity": 2 },
            { "itemId": "sta-001", "name": "Chicken 65",           "price": 1499, "quantity": 1 }
          ],
          "orderType": "TAKEOUT"
        }
        """;

    @BeforeEach
    void setUp() throws Exception {
        Order mockOrder = new Order();
        mockOrder.setId(MOCK_ORDER_ID);
        mockOrder.setState("open");

        OrderResult mockResult = OrderResult.builder()
            .success(true)
            .orderId(MOCK_ORDER_ID)
            .orderState("open (pay at restaurant)")
            .paymentType(PaymentType.PAY_AT_RESTAURANT)
            .order(mockOrder)
            .build();

        Mockito.when(cloverClient.placeOrder(any())).thenReturn(mockResult);
        Mockito.when(cloverClient.getOrder(eq(MOCK_ORDER_ID))).thenReturn(mockOrder);
        Mockito.when(cloverClient.getOrder(Mockito.argThat(id -> !MOCK_ORDER_ID.equals(id))))
            .thenThrow(new CloverException("Order not found", 404, "{}"));
    }

    @Test
    void createOrderReturns201WithOrderId() throws Exception {
        mvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_ORDER))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.orderId").isString())
            .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
            .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    void getOrderByIdReturnsOrder() throws Exception {
        MvcResult result = mvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_ORDER))
            .andReturn();

        JsonNode json = mapper.readTree(result.getResponse().getContentAsString());
        String orderId = json.at("/data/orderId").asText();

        mvc.perform(get("/api/orders/" + orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.orderId").value(orderId));
    }

    @Test
    void missingCustomerNameReturns400() throws Exception {
        mvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "customerPhone": "6785551234",
                      "items": [{ "itemId": "pop-001", "name": "Biryani", "price": 1899, "quantity": 1 }]
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void unknownOrderReturns404() throws Exception {
        mvc.perform(get("/api/orders/BNG-NOTEXIST"))
            .andExpect(status().isNotFound());
    }
}
