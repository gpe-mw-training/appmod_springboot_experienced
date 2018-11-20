package com.redhat.coolstore.cart.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.redhat.coolstore.cart.model.Product;
import com.redhat.coolstore.cart.model.ShoppingCart;
import com.redhat.coolstore.cart.model.ShoppingCartItem;

@Component
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private PriceCalculationService priceCalculationService;

    private Map<String, ShoppingCart> cartDB = new HashMap<>();

    @Override
    public ShoppingCart calculateCartPrice(ShoppingCart sc) {
        priceCalculationService.priceShoppingCart(sc);
        cartDB.put(sc.getId(), sc);
        return sc;
    }

    @Override
    public ShoppingCart getShoppingCart(String cartId) {
        ShoppingCart sc = cartDB.get(cartId);
        if (sc == null) {
            sc = new ShoppingCart();
            sc.setId(cartId);
            cartDB.put(cartId, sc);
        }
        return sc;
    }

    @Override
    public ShoppingCart addToCart(String cartId, String itemId, int quantity) {
        ShoppingCart sc = getShoppingCart(cartId);
        if (quantity <= 0) {
            return sc;
        }
        Product product;
        product = getProduct(itemId);
        if (product == null) {
            return sc;
        }
        Optional<ShoppingCartItem> cartItem = sc.getShoppingCartItemList().stream().filter(sci -> sci.getProduct().getItemId().equals(itemId)).findFirst();
        if (cartItem.isPresent()) {
            cartItem.get().setQuantity(cartItem.get().getQuantity() + quantity);
        } else {
            ShoppingCartItem newCartItem = new ShoppingCartItem();
            newCartItem.setProduct(product);
            newCartItem.setQuantity(quantity);
            newCartItem.setPrice(product.getPrice());
            sc.addShoppingCartItem(newCartItem);
        }
        calculateCartPrice(sc);
        cartDB.put(sc.getId(), sc);
        return sc;
    }

    @Override
    public ShoppingCart removeFromCart(String cartId, String itemId, int quantity) {
        ShoppingCart sc = getShoppingCart(cartId);
        if (quantity <= 0) {
            return sc;
        }
        Optional<ShoppingCartItem> cartItem = sc.getShoppingCartItemList().stream().filter(sci -> sci.getProduct().getItemId().equals(itemId)).findFirst();
        if (cartItem.isPresent()) {
            if (cartItem.get().getQuantity() <= quantity) {
                sc.removeShoppingCartItem(cartItem.get());
            } else {
                cartItem.get().setQuantity(cartItem.get().getQuantity() - quantity);
            }
        }
        calculateCartPrice(sc);
        cartDB.put(sc.getId(), sc);
        return sc;
    }

    @Override
    public ShoppingCart checkoutShoppingCart(String cartId) {
        ShoppingCart sc = new ShoppingCart();
        sc.setId(cartId);
        cartDB.put(sc.getId(), sc);
        return sc;
    }

    private Product getProduct(String itemId) {
        return catalogService.getProduct(itemId);
    }
}
