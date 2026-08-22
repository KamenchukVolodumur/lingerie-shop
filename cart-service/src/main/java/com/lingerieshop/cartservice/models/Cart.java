
package com.lingerieshop.cartservice.models;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("Cart")
public class Cart {
    @Id
    private String id;

    private List<CartItem> products = new ArrayList<>();

    @TimeToLive(unit = TimeUnit.DAYS)
    private Long timeToLive = 7L;

    public Cart(String id) {
        this.id = id;

    }
}