package com.lingerieshop.orderservice.clients;

import com.lingerieshop.orderservice.dto.novaposhta.NovaPoshtaRequest;
import com.lingerieshop.orderservice.dto.novaposhta.NovaPoshtaResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "novaPoshtaClient", url = "https://api.novaposhta.ua/v2.0")
public interface NovaPoshtaClient {

    @PostMapping("/json/")
    NovaPoshtaResponse sendRequest(@RequestBody NovaPoshtaRequest request);
}
