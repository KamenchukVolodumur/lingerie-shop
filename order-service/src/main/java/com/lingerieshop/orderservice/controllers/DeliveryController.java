package com.lingerieshop.orderservice.controllers;

import com.lingerieshop.orderservice.services.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/cities")
    public ResponseEntity<List<Map<String, String>>> getCities(
            @RequestParam(required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(deliveryService.searchCities(query));
    }

    @GetMapping("/branches")
    public ResponseEntity<List<Map<String, String>>> getBranches(
            @RequestParam String cityRef,
            @RequestParam(required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(deliveryService.searchBranches(cityRef, query));
    }
}
