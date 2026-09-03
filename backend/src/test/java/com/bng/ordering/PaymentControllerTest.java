package com.bng.ordering;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void paymentIsApproved() throws Exception {
        mvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "orderId": "BNG-TEST0001",
                      "amount": 42.97,
                      "paymentMethod": "CARD",
                      "cardToken": "tok_test_4242"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("APPROVED"))
            .andExpect(jsonPath("$.data.paymentId").isString());
    }

    @Test
    void missingOrderIdReturns400() throws Exception {
        mvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "amount": 19.99,
                      "paymentMethod": "CASH"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }
}
