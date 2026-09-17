package com.lingerieshop.orderservice.services;

import com.lingerieshop.orderservice.clients.NovaPoshtaClient;
import com.lingerieshop.orderservice.dto.novaposhta.NovaPoshtaRequest;
import com.lingerieshop.orderservice.dto.novaposhta.NovaPoshtaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final NovaPoshtaClient novaPoshtaClient;

    @Value("${novaposhta.api.key:}")
    private String apiKey;

    // Простий in-memory кеш. В ідеалі використовувати Spring Cache (Redis/Caffeine)
    private final Map<String, List<Map<String, Object>>> citiesCache = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> branchesCache = new ConcurrentHashMap<>();

    public List<Map<String, String>> searchCities(String query) {
        String cacheKey = (query == null) ? "" : query.toLowerCase().trim();
        
        if (citiesCache.containsKey(cacheKey)) {
            return formatCitiesResponse(citiesCache.get(cacheKey));
        }

        Map<String, Object> props = new HashMap<>();
        props.put("CityName", query);
        props.put("Limit", 50);

        NovaPoshtaRequest request = NovaPoshtaRequest.builder()
                .apiKey(apiKey)
                .modelName("Address")
                .calledMethod("searchSettlements")
                .methodProperties(props)
                .build();

        NovaPoshtaResponse response = novaPoshtaClient.sendRequest(request);
        
        if (response != null && response.isSuccess() && response.getData() != null && !response.getData().isEmpty()) {
            List<Map<String, Object>> addresses = (List<Map<String, Object>>) response.getData().get(0).get("Addresses");
            if (addresses != null) {
                citiesCache.put(cacheKey, addresses);
                return formatCitiesResponse(addresses);
            }
        }
        
        return List.of();
    }

    public List<Map<String, String>> searchBranches(String cityRef, String query) {
        if (cityRef == null || cityRef.trim().isEmpty()) {
            return List.of();
        }

        String cacheKey = cityRef + "_" + (query == null ? "" : query.toLowerCase().trim());
        
        if (branchesCache.containsKey(cacheKey)) {
            return formatBranchesResponse(branchesCache.get(cacheKey));
        }

        Map<String, Object> props = new HashMap<>();
        props.put("CityRef", cityRef);
        if (query != null && !query.trim().isEmpty()) {
            props.put("FindByString", query);
        }
        props.put("Limit", 50);

        NovaPoshtaRequest request = NovaPoshtaRequest.builder()
                .apiKey(apiKey)
                .modelName("Address")
                .calledMethod("getWarehouses")
                .methodProperties(props)
                .build();

        NovaPoshtaResponse response = novaPoshtaClient.sendRequest(request);
        
        if (response != null && response.isSuccess() && response.getData() != null) {
            branchesCache.put(cacheKey, response.getData());
            return formatBranchesResponse(response.getData());
        }
        
        return List.of();
    }

    private List<Map<String, String>> formatCitiesResponse(List<Map<String, Object>> rawData) {
        return rawData.stream().map(data -> {
            Map<String, String> city = new HashMap<>();
            // searchSettlements повертає DeliveryCity, який потрібен для пошуку відділень
            city.put("ref", (String) data.get("DeliveryCity"));
            city.put("description", (String) data.get("Present"));
            return city;
        }).collect(Collectors.toList());
    }

    private List<Map<String, String>> formatBranchesResponse(List<Map<String, Object>> rawData) {
        return rawData.stream().map(data -> {
            Map<String, String> branch = new HashMap<>();
            branch.put("ref", (String) data.get("Ref"));
            branch.put("description", (String) data.get("Description"));
            return branch;
        }).collect(Collectors.toList());
    }
}
