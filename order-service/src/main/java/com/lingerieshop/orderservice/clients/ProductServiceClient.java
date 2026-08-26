
package com.lingerieshop.orderservice.clients;

import com.lingerieshop.orderservice.dto.DeductStockRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product-service", url = "${app.services.product-service-url}")
public interface ProductServiceClient {

    @PostMapping("/api/v1/products/deduct")
    void deductStock(@RequestBody List<DeductStockRequestDTO> items);

    @PostMapping("/api/v1/products/release")
    void releaseStock(@RequestBody List<DeductStockRequestDTO> items);
}
