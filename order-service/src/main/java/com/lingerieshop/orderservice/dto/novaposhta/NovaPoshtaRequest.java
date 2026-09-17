package com.lingerieshop.orderservice.dto.novaposhta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovaPoshtaRequest {
    private String apiKey;
    private String modelName;
    private String calledMethod;
    private Map<String, Object> methodProperties;
}
