package com.lingerieshop.orderservice.dto.novaposhta;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class NovaPoshtaResponse {
    private boolean success;
    private List<Map<String, Object>> data;
    private List<String> errors;
    private List<String> warnings;
}
