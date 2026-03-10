package com.store.workflowService.update.infra.adapters.out.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "userAuthClient", url = "${api.url}")
public interface UserAuthClient {

    @GetMapping(value = "/me", produces = "application/json")
    String getUserInfo(@RequestHeader("Authorization") String bearerToken);
}