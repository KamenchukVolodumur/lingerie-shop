package com.lingerieshop.cartservice.repositories;

import com.lingerieshop.cartservice.models.Cart;
import org.springframework.data.repository.CrudRepository;

public interface CartRepository extends CrudRepository<Cart, String> {
}
