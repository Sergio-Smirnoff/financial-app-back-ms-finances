package com.financialapp.finances.web.controller;

import com.financialapp.finances.support.WireMockIntegrationTest;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests for {@link TransactionController}. Every endpoint is driven with a
 * real JSON payload and the response is asserted. Account ownership is resolved through the real
 * Feign client against WireMock (TP1 strategy) — never mocked. The classpath stub
 * ({@code wiremock/mappings/banks-accounts.json}) makes user 42 own ARS {@code 0001112223334445556667}
 * and USD {@code 9998887776665554443332}; per-test {@code stubFor} overrides cover failures.
 */
class TransactionControllerIT extends WireMockIntegrationTest {

    private static final String USER = "42";
    private static final String OWNED_ARS = "0001112223334445556667";
    private static final String EXTERNAL = "1111111111111111111111";

    /** Records a valid ARS expense (owned source, external destination) and returns its id. */
    private long recordExpense(String amount, String description, String date) throws Exception {
        String body = """
                {"fromCbu":"%s","toCbu":"%s","amount":"%s","currency":"ARS","categoryId":5,"description":"%s","date":"%s"}
                """.formatted(OWNED_ARS, EXTERNAL, amount, description, date);
        MvcResult res = mvc.perform(post("/api/v1/finances/transactions")
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(res.getResponse().getContentAsString(), "$.data.id")).longValue();
    }

    @Test
    void record_validExpense_returns201WithExpenseKind() throws Exception {
        String body = """
                {"fromCbu":"%s","toCbu":"%s","amount":"150.00","currency":"ARS","categoryId":5,"description":"Rent","date":"2026-06-01"}
                """.formatted(OWNED_ARS, EXTERNAL);

        mvc.perform(post("/api/v1/finances/transactions")
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.fromCbu").value(OWNED_ARS))
                .andExpect(jsonPath("$.data.toCbu").value(EXTERNAL))
                .andExpect(jsonPath("$.data.amount").value("150.00"))
                .andExpect(jsonPath("$.data.currency").value("ARS"))
                .andExpect(jsonPath("$.data.kind").value("EXPENSE"))
                .andExpect(jsonPath("$.data.description").value("Rent"));
    }

    @Test
    void record_unsupportedCurrency_returns400() throws Exception {
        String body = """
                {"fromCbu":"%s","toCbu":"%s","amount":"10.00","currency":"GBP","categoryId":5,"description":"x","date":"2026-06-01"}
                """.formatted(OWNED_ARS, EXTERNAL);

        mvc.perform(post("/api/v1/finances/transactions")
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void record_touchesNoOwnedAccount_returns422() throws Exception {
        // ms-banks reports the user owns nothing → neither side is owned.
        wireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/v1/banks/accounts")).atPriority(1)
                .willReturn(WireMock.okJson("{\"success\":true,\"message\":\"ok\",\"data\":[],\"errors\":null,\"timestamp\":\"2026-06-02T00:00:00Z\"}")));
        String body = """
                {"fromCbu":"%s","toCbu":"%s","amount":"10.00","currency":"ARS","categoryId":5,"description":"x","date":"2026-06-01"}
                """.formatted(OWNED_ARS, EXTERNAL);

        mvc.perform(post("/api/v1/finances/transactions")
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void record_downstreamBanksFailure_propagatesStatus() throws Exception {
        wireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/v1/banks/accounts")).atPriority(1)
                .willReturn(WireMock.aResponse().withStatus(500)));
        String body = """
                {"fromCbu":"%s","toCbu":"%s","amount":"10.00","currency":"ARS","categoryId":5,"description":"x","date":"2026-06-01"}
                """.formatted(OWNED_ARS, EXTERNAL);

        mvc.perform(post("/api/v1/finances/transactions")
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Communication error between services"));
    }

    @Test
    void update_changesDescription_returns200() throws Exception {
        long id = recordExpense("20.00", "Before", "2026-06-01");

        mvc.perform(put("/api/v1/finances/transactions/" + id)
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"After\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value((int) id))
                .andExpect(jsonPath("$.data.description").value("After"));
    }

    @Test
    void update_noEditableField_returns400() throws Exception {
        long id = recordExpense("20.00", "x", "2026-06-01");

        mvc.perform(put("/api/v1/finances/transactions/" + id)
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_removesTransaction_returns200() throws Exception {
        long id = recordExpense("20.00", "ToDelete", "2026-06-01");

        mvc.perform(delete("/api/v1/finances/transactions/" + id).header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted"));
    }

    @Test
    void list_byAccountCbu_returnsAccountView() throws Exception {
        recordExpense("33.00", "AccountScoped", "2026-06-03");

        mvc.perform(get("/api/v1/finances/transactions").param("accountCbu", OWNED_ARS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", not(empty())))
                .andExpect(jsonPath("$.data[0].accountCbu").value(OWNED_ARS))
                .andExpect(jsonPath("$.data[0].transactionId").exists())
                .andExpect(jsonPath("$.data[0].currency").exists());
    }

    @Test
    void list_byUser_returnsUserTransactions() throws Exception {
        recordExpense("44.00", "UserScoped", "2026-06-04");

        mvc.perform(get("/api/v1/finances/transactions").header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", not(empty())));
    }

    @Test
    void list_withoutUserOrAccount_returns400() throws Exception {
        mvc.perform(get("/api/v1/finances/transactions"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void summary_withoutRange_returnsPerCurrencyTotals() throws Exception {
        recordExpense("75.00", "Summed", "2026-06-05");

        mvc.perform(get("/api/v1/finances/transactions/summary").header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ARS.totalIncome").exists())
                .andExpect(jsonPath("$.data.ARS.totalExpense").exists())
                .andExpect(jsonPath("$.data.ARS.balance").exists());
    }

    @Test
    void summary_withRange_returnsPerCurrencyTotals() throws Exception {
        recordExpense("75.00", "RangedSum", "2026-06-06");

        mvc.perform(get("/api/v1/finances/transactions/summary")
                        .header("X-User-Id", USER).param("from", "2026-01-01").param("to", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ARS").exists());
    }

    @Test
    void summary_partialRange_returns400() throws Exception {
        mvc.perform(get("/api/v1/finances/transactions/summary")
                        .header("X-User-Id", USER).param("from", "2026-01-01"))
                .andExpect(status().isBadRequest());
    }
}
