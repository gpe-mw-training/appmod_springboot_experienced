package com.redhat.coolstore.cart.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import com.redhat.coolstore.cart.model.Product;
import com.redhat.coolstore.cart.model.ShoppingCart;
import com.redhat.coolstore.cart.model.ShoppingCartItem;

public class ShoppingCartServiceImplTest {

    private ShoppingCartServiceImpl shoppingCartService;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private PriceCalculationService priceCalculationService;

    @Mock
    private CatalogService catalogService;

    @Before
    public void setup() {
        initMocks();
        shoppingCartService = new ShoppingCartServiceImpl();
        ReflectionTestUtils.setField(shoppingCartService, null, catalogService, CatalogService.class);
        ReflectionTestUtils.setField(shoppingCartService, null, priceCalculationService, PriceCalculationService.class);
    }

    @Test
    public void testGetNewShoppingCart() {
        ShoppingCart sc = shoppingCartService.getShoppingCart("123456");

        assertThat(sc, notNullValue());
        assertThat(sc.getId(), equalTo("123456"));
        assertThat(sc.getCartItemTotal(), equalTo(0.0));
        assertThat(sc.getShippingTotal(), equalTo(0.0));
        assertThat(sc.getCartTotal(), equalTo(0.0));
        assertThat(sc.getShoppingCartItemList().size(), equalTo(0));
    }

    @Test
    public void testAddNewItemToCart() {

        ShoppingCart sc = shoppingCartService.addToCart("123456", "p1", 1);
        assertThat(sc.getShoppingCartItemList().size(), equalTo(1));
        assertThat(sc.getCartItemTotal(), equalTo(100.0));
        ShoppingCartItem sci = sc.getShoppingCartItemList().get(0);
        assertThat(sci.getProduct(), notNullValue());
        assertThat(sci.getProduct().getItemId(), equalTo("p1"));
        assertThat(sci.getProduct().getPrice(), equalTo(100.0));
        assertThat(sci.getPrice(), equalTo(100.0));
        assertThat(sci.getQuantity(), equalTo(1));
        verify(priceCalculationService).priceShoppingCart(sc);
        verify(catalogService).getProduct("p1");

        //make sure the cart store is up to date
        sc = shoppingCartService.getShoppingCart("123456");
        assertThat(sc.getShoppingCartItemList().size(), equalTo(1));
        assertThat(sc.getCartItemTotal(), equalTo(100.0));
    }

    @Test
    public void testAddExistingItemToCart() {

        shoppingCartService.addToCart("123456", "p1", 1);
        ShoppingCart sc = shoppingCartService.addToCart("123456", "p1", 2);
        assertThat(sc.getShoppingCartItemList().size(), equalTo(1));
        ShoppingCartItem sci = sc.getShoppingCartItemList().get(0);
        assertThat(sci.getProduct(), notNullValue());
        assertThat(sci.getProduct().getItemId(), equalTo("p1"));
        assertThat(sci.getProduct().getPrice(), equalTo(100.0));
        assertThat(sci.getPrice(), equalTo(100.0));
        assertThat(sci.getQuantity(), equalTo(3));
        verify(priceCalculationService, times(2)).priceShoppingCart(any(ShoppingCart.class));
        verify(catalogService, times(2)).getProduct("p1");

        //make sure the cart store is up to date
        sc = shoppingCartService.getShoppingCart("123456");
        assertThat(sc.getShoppingCartItemList().size(), equalTo(1));
        assertThat(sc.getShoppingCartItemList().get(0).getQuantity(), equalTo(3));
    }

    @Test
    public void testAddItemToCartWithNegativeOrNullQuantity() {

        shoppingCartService.addToCart("123456", "p1", 1);
        ShoppingCart sc = shoppingCartService.addToCart("123456", "p1", 0);
        assertThat(sc.getShoppingCartItemList().size(), equalTo(1));
        ShoppingCartItem sci = sc.getShoppingCartItemList().get(0);
        assertThat(sci.getProduct(), notNullValue());
        assertThat(sci.getProduct().getItemId(), equalTo("p1"));
        assertThat(sci.getProduct().getPrice(), equalTo(100.0));
        assertThat(sci.getPrice(), equalTo(100.0));
        assertThat(sci.getQuantity(), equalTo(1));
        verify(priceCalculationService).priceShoppingCart(any(ShoppingCart.class));
        verify(catalogService).getProduct("p1");

        ShoppingCart sc1 = shoppingCartService.addToCart("123456", "p1", -1);
        assertThat(sc1.getShoppingCartItemList().size(), equalTo(1));
        ShoppingCartItem sci1 = sc1.getShoppingCartItemList().get(0);
        assertThat(sci1.getQuantity(), equalTo(1));
        verify(priceCalculationService).priceShoppingCart(any(ShoppingCart.class));
        verify(catalogService).getProduct("p1");
    }

    @Test
    public void testAddItemToCartWithOtherProducts() {

        shoppingCartService.addToCart("123456", "p1", 1);
        ShoppingCart sc = shoppingCartService.addToCart("123456", "p2", 2);
        assertThat(sc.getShoppingCartItemList().size(), equalTo(2));
        Optional<ShoppingCartItem> sci = sc.getShoppingCartItemList().stream().filter(i -> i.getProduct().getItemId().equals("p2")).findFirst();
        assertThat(sci.isPresent(), equalTo(true));
        assertThat(sci.get().getQuantity(), equalTo(2));
        verify(priceCalculationService, times(2)).priceShoppingCart(any(ShoppingCart.class));
        verify(catalogService).getProduct("p1");
        verify(catalogService).getProduct("p2");

        //make sure the cart store is up to date
        sc = shoppingCartService.getShoppingCart("123456");
        assertThat(sc.getShoppingCartItemList().size(), equalTo(2));
    }

    @Test
    public void testAddNonExistingItemToCartWithOtherProducts() {

        shoppingCartService.addToCart("123456", "p1", 1);
        ShoppingCart sc = shoppingCartService.addToCart("123456", "p3", 2);
        assertThat(sc.getShoppingCartItemList().size(), equalTo(1));
        verify(priceCalculationService, times(1)).priceShoppingCart(any(ShoppingCart.class));
        verify(catalogService).getProduct("p1");
        verify(catalogService).getProduct("p3");
    }

    @Test
    public void testRemoveExistingItemFromCartWithNegativeOrNullQuantity() {

        shoppingCartService.addToCart("123456", "p1", 3);
        ShoppingCart sc = shoppingCartService.removeFromCart("123456", "p1", 0);
        assertThat(sc.getShoppingCartItemList().size(), equalTo(1));
        ShoppingCartItem sci = sc.getShoppingCartItemList().get(0);
        assertThat(sci.getProduct(), notNullValue());
        assertThat(sci.getProduct().getItemId(), equalTo("p1"));
        assertThat(sci.getProduct().getPrice(), equalTo(100.0));
        assertThat(sci.getQuantity(), equalTo(3));
        verify(priceCalculationService).priceShoppingCart(any(ShoppingCart.class));

        ShoppingCart sc1 = shoppingCartService.removeFromCart("123456", "p1", -2);
        assertThat(sc.getShoppingCartItemList().size(), equalTo(1));
        ShoppingCartItem sci1 = sc1.getShoppingCartItemList().get(0);
        assertThat(sci1.getQuantity(), equalTo(3));
        verify(priceCalculationService).priceShoppingCart(any(ShoppingCart.class));
    }

    @Test
    public void testRemoveExistingItemFromCart() {

        shoppingCartService.addToCart("123456", "p1", 3);
        ShoppingCart sc = shoppingCartService.removeFromCart("123456", "p1", 2);
        assertThat(sc.getShoppingCartItemList().size(), equalTo(1));
        ShoppingCartItem sci = sc.getShoppingCartItemList().get(0);
        assertThat(sci.getProduct(), notNullValue());
        assertThat(sci.getProduct().getItemId(), equalTo("p1"));
        assertThat(sci.getProduct().getPrice(), equalTo(100.0));
        assertThat(sci.getPrice(), equalTo(100.0));
        assertThat(sci.getQuantity(), equalTo(1));
        verify(priceCalculationService, times(2)).priceShoppingCart(any(ShoppingCart.class));
        verify(catalogService).getProduct("p1");

        //make sure the cart store is up to date
        sc = shoppingCartService.getShoppingCart("123456");
        assertThat(sc.getShoppingCartItemList().size(), equalTo(1));
    }

    @Test
    public void testRemoveAllItemsFromCart() {
        shoppingCartService.addToCart("123456", "p1", 3);
        ShoppingCart sc = shoppingCartService.removeFromCart("123456", "p1", 3);
        assertThat(sc.getShoppingCartItemList().size(), equalTo(0));
        verify(priceCalculationService, times(2)).priceShoppingCart(any(ShoppingCart.class));
        verify(catalogService).getProduct("p1");

        //make sure the cart store is up to date
        sc = shoppingCartService.getShoppingCart("123456");
        assertThat(sc.getShoppingCartItemList().size(), equalTo(0));
    }

    @Test
    public void testCheckoutCart() {
        shoppingCartService.addToCart("123456", "p1", 3);
        ShoppingCart sc = shoppingCartService.checkoutShoppingCart("123456");
        assertThat(sc, notNullValue());
        assertThat(sc.getId(), equalTo("123456"));
        assertThat(sc.getCartItemTotal(), equalTo(0.0));
        assertThat(sc.getShippingTotal(), equalTo(0.0));
        assertThat(sc.getCartTotal(), equalTo(0.0));
        assertThat(sc.getShoppingCartItemList().size(), equalTo(0));

        //make sure we also updated the cart store
        sc = shoppingCartService.getShoppingCart("123456");
        assertThat(sc, notNullValue());
        assertThat(sc.getId(), equalTo("123456"));
        assertThat(sc.getCartItemTotal(), equalTo(0.0));
        assertThat(sc.getShippingTotal(), equalTo(0.0));
        assertThat(sc.getCartTotal(), equalTo(0.0));
        assertThat(sc.getShoppingCartItemList().size(), equalTo(0));
    }

    private void initMocks() {
        Product p1 = new Product();
        p1.setItemId("p1");
        p1.setPrice(100.0);
        when(catalogService.getProduct("p1")).thenReturn(p1);

        Product p2 = new Product();
        p2.setItemId("p2");
        p2.setPrice(100.0);
        when(catalogService.getProduct("p2")).thenReturn(p2);

        when(catalogService.getProduct("p3")).thenReturn(null);

        doAnswer(invocation -> {
            ShoppingCart sc = invocation.getArgument(0, ShoppingCart.class);
            sc.setCartItemTotal(100.0);
            sc.setCartTotal(120.0);
            sc.setShippingTotal(20.0);
            return null;
        }).when(priceCalculationService).priceShoppingCart(any(ShoppingCart.class));
    }

}
