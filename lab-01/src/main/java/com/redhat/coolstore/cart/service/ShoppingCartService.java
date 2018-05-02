package com.redhat.coolstore.cart.service;

import com.redhat.coolstore.cart.model.ShoppingCart;

public interface ShoppingCartService {

    public ShoppingCart calculateCartPrice(ShoppingCart sc);

    public ShoppingCart getShoppingCart(String cartId);

    public ShoppingCart addToCart(String cartId, String itemId, int quantity);

    public ShoppingCart removeFromCart(String cartId, String itemId, int quantity);

    public ShoppingCart checkoutShoppingCart(String cartId);

}
