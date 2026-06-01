package com.financialapp.finances.infrastructure.gateway;

import com.financialapp.finances.infrastructure.gateway.dto.BankAccountResponse;
import com.financialapp.finances.infrastructure.gateway.dto.GatewayApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "ms-banks-accounts", url = "${banks.service.url:http://localhost:8083}")
public interface BanksAccountsFeignClient {

    @GetMapping("/api/v1/banks/accounts")
    GatewayApiResponse<List<BankAccountResponse>> listAccounts(@RequestHeader("X-User-Id") Long userId);
}
