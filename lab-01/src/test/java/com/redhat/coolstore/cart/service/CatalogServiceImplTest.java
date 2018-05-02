package com.redhat.coolstore.cart.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.redhat.coolstore.cart.model.Product;

public class CatalogServiceImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort());

    private CatalogServiceImpl catalogService;

    @Before
    public void beforeTest() throws Exception {
        catalogService = new CatalogServiceImpl();
        ReflectionTestUtils.setField(catalogService, "catalogServiceUrl", "http://localhost:" + wireMockRule.port(), null);
    }
    
    @Test
    public void getProduct() throws Exception {

        // read the test json file
        InputStream isresp = Thread.currentThread().getContextClassLoader().getResourceAsStream("catalog-response.json");

        // stub the WireMock server to return a response
        stubFor(get(urlEqualTo("/product/111111")).willReturn(
                aResponse().withStatus(200).withHeader("Content-type", "application/json").withBody(IOUtils.toString(isresp, Charset.defaultCharset()))));

        Product product = catalogService.getProduct("111111");

        assertThat(product, notNullValue());
        assertThat(product.getItemId(), notNullValue());
        assertThat(product.getItemId(), equalTo("111111"));
        assertThat(product.getPrice(), equalTo(new Double(100.0)));
        verify(getRequestedFor(urlEqualTo("/product/111111")));
    }  
    
    @Test
    public void getProductWhenCatalogServerRespondsWithNotFound() throws Exception {

        stubFor(get(urlEqualTo("/product/111111")).willReturn(
                aResponse().withStatus(404)));

        Product product = catalogService.getProduct("111111");

        assertThat(product, nullValue());
        verify(getRequestedFor(urlEqualTo("/product/111111")));
    }    
}
