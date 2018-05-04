package com.redhat.coolstore.cart.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redhat.coolstore.cart.model.ShoppingCart;
import com.redhat.coolstore.cart.service.ShoppingCartService;

@RestController
@RequestMapping("/cart")
public class CartEndpoint {

	private static final Logger LOG = LoggerFactory.getLogger(CartEndpoint.class);
	
	@Autowired
    private ShoppingCartService shoppingCartService;
	
	@GetMapping("/{cartId}")
	public ShoppingCart getCart(@PathVariable String cartId) {

		return shoppingCartService.getShoppingCart(cartId);	
	}	
	
    @PostMapping("/{cartId}/{itemId}/{quantity}")
    public ShoppingCart add(@PathVariable String cartId, 
    							@PathVariable String itemId, 
    							@PathVariable int quantity) {

    		return shoppingCartService.addToCart(cartId, itemId, quantity);
    }
    
    @DeleteMapping("/{cartId}/{itemId}/{quantity}")
    public ShoppingCart delete(@PathVariable String cartId, 
    								@PathVariable String itemId, 
    								@PathVariable int quantity) {

        return shoppingCartService.removeFromCart(cartId, itemId, quantity);
    }
    
    @PostMapping("/checkout/{cartId}")
    public ShoppingCart checkout(@PathVariable String cartId) {

        ShoppingCart cart = shoppingCartService.checkoutShoppingCart(cartId);
        LOG.info("ShoppingCart " + cart + " checked out");
        
        return cart;
    }    
}