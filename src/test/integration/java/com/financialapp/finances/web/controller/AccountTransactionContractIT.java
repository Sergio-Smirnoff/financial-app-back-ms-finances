package com.financialapp.finances.web.controller;

import com.financialapp.finances.support.WireMockIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pins the exact JSON shape ms-banks consumes from the account-scoped transaction listing
 * ({@code GET /transactions?accountCbu=...}). Runs the full stack: a transaction is recorded
 * through the controller (ownership resolved over WireMock), then read back per-account.
 */
class AccountTransactionContractIT extends WireMockIntegrationTest {

    private static final String USER = "42";
    // The shared context means all IT classes share one H2 DB. This contract test reads back the
    // account listing by $.data[0], so it uses the USD account that no other test touches — keeping
    // its single recorded row isolated regardless of cross-class test ordering.
    private static final String OWNED_USD = "9998887776665554443332";
    private static final String EXTERNAL = "1111111111111111111111";

    @Test
    void accountListingExposesExactlyTheFieldsMsBanksExpects() throws Exception {
        // The account view resolves category display names by id, so the transaction must reference a
        // real subcategory ("Housing" → "Rent") for the category/subcategory fields to be populated.
        MvcResult cat = mvc.perform(post("/api/v1/finances/categories")
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Housing\"}"))
                .andExpect(status().isCreated()).andReturn();
        long parentId = ((Number) JsonPath.read(cat.getResponse().getContentAsString(), "$.data.id")).longValue();
        MvcResult sub = mvc.perform(post("/api/v1/finances/categories/" + parentId + "/subcategories")
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Rent\"}"))
                .andExpect(status().isCreated()).andReturn();
        long subId = ((Number) JsonPath.read(sub.getResponse().getContentAsString(), "$.data.id")).longValue();

        String body = """
                {"fromCbu":"%s","toCbu":"%s","amount":"100.00","currency":"USD","categoryId":%d,"description":"Rent","date":"2026-06-01"}
                """.formatted(OWNED_USD, EXTERNAL, subId);
        mvc.perform(post("/api/v1/finances/transactions")
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/finances/transactions").param("accountCbu", OWNED_USD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].transactionId").exists())
                .andExpect(jsonPath("$.data[0].accountCbu").value(OWNED_USD))
                .andExpect(jsonPath("$.data[0].amount").exists())
                .andExpect(jsonPath("$.data[0].currency").value("USD"))
                .andExpect(jsonPath("$.data[0].description").value("Rent"))
                .andExpect(jsonPath("$.data[0].category").value("Housing"))
                .andExpect(jsonPath("$.data[0].subcategory").value("Rent"))
                .andExpect(jsonPath("$.data[0].date").value("2026-06-01"));
    }
}
