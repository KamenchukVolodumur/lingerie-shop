package com.lingerieshop.cartservice.models;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Long productId;
    private Long productVariantId;
    private int quantity;

}