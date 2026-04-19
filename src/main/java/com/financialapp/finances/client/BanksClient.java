package com.financialapp.finances.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "ms-banks", url = "${banks.service.url:http://localhost:8083}")
public interface BanksClient {

    @PatchMapping("/api/v1/banks/accounts/{id}/balance/adjust")
    void adjustBalance(@PathVariable("id") Long id, @RequestParam("delta") BigDecimal delta);
}
