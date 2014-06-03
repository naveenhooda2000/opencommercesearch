package org.opencommercesearch.feed;

/*
* Licensed to OpenCommerceSearch under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. OpenCommerceSearch licenses this
* file to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import atg.commerce.inventory.InventoryException;
import atg.nucleus.ServiceException;
import atg.repository.RepositoryException;
import atg.repository.RepositoryItem;
import atg.repository.RepositoryItemDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.opencommercesearch.api.ProductService;
import org.opencommercesearch.model.Product;
import org.opencommercesearch.model.Sku;
import org.opencommercesearch.repository.RuleBasedCategoryProperty;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class SearchFeedTest {

    private SearchFeed feed = new SearchFeed() {
        @Override
        protected void onFeedStarted(FeedType type, long feedTimestamp) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void onProductsSent(FeedType type, long feedTimestamp, Locale locale, List<Product> productList, Response response) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void onProductsSentError(FeedType type, long feedTimestamp, Locale locale, List<Product> productList, Exception ex) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void onProductsSentError(FeedType type, long feedTimestamp, Locale locale, List<Product> productList, Response response) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void onFeedFinished(FeedType type, long feedTimestamp) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void processProduct(RepositoryItem product, SearchFeedProducts products) throws RepositoryException, InventoryException {
            throw new UnsupportedOperationException();
        }
    };

    @Mock
    private RepositoryItem catalogOutdoor;
    @Mock
    private RepositoryItem catRoot;
    @Mock
    private RepositoryItem catRulesBased;
    @Mock
    private RepositoryItem catShoesFootwear;
    @Mock
    private RepositoryItem catMensShoesBoots;
    @Mock
    private RepositoryItem catMensRainBootsShoes;
    @Mock
    private RepositoryItem catMensRainShoes;
    @Mock
    private RepositoryItem catMensRainBoots;
    @Mock
    private RepositoryItem catMensClothing;
    @Mock
    private RepositoryItem catMensShoesFootwear;
    @Mock
    private RepositoryItem catSnowshoe;
    @Mock
    private RepositoryItem catSnowshoeAccessories;
    @Mock
    private RepositoryItem catSnowshoeFootwear;
    @Mock
    private RepositoryItem catSnowshoeBoots;
    @Mock
    private RepositoryItem prodMensBoot;
    @Mock
    private Sku sku;
    @Captor 
    private ArgumentCaptor<String> stringCaptor;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        /**
         * root
         *     Shoes & Footwear
         *         Men's Shoes & Boots
         *             Men's Rain Boots & Shoes
         *                 Men's Rain Shoes
         *                 Men's Rain Boots
         *     Men's Clothing
         *         Men's Shoes & Footwear
         *             Mens Rain Boots & Shoes
         *                 Men's Rain Shoes
         *                 Men's Rain Boots
         *    Snowshoe
         *         Snowshoe Accessories
         *             Snowshoe Footwear
         *                 Snowshoe boots
         */

        // catalogs
        when(catalogOutdoor.getRepositoryId()).thenReturn("outdoorCatalog");
        Set<RepositoryItem> categoryCatalogs = newSet(catalogOutdoor);

        // Root
        mockCategory(catRoot, "catRoot", "root", categoryCatalogs, null, "category");
        // Rules Based
        mockCategory(catRulesBased, "catRulesBased", "Rules Based", categoryCatalogs, newSet(catRoot), RuleBasedCategoryProperty.ITEM_DESCRIPTOR);
        // Shoes & Footwear
        mockCategory(catShoesFootwear, "outdoorCat4000003", "Shoes & Footwear", categoryCatalogs, newSet(catRoot), "category");
        // Men's Shoes & Boots
        mockCategory(catMensShoesBoots, "outdoorCat4100004", "Men's Shoes & Boots", categoryCatalogs, newSet(catShoesFootwear), "category");
        // Men's Clothing
        mockCategory(catMensClothing, "outdoorCat100003", "Men's Clothing", categoryCatalogs, newSet(catRoot), "category");
        // Men's Shoes & Footwear
        mockCategory(catMensShoesFootwear, "outdoorCat11000219", "Men's Shoes & Footwear", categoryCatalogs, newSet(catMensClothing), "category");
        // Men's Rain Boots & Shoes
        mockCategory(catMensRainBootsShoes, "outdoorCat41100024", "Men's Rain Boots & Shoes", categoryCatalogs, newSet(catMensShoesBoots, catMensShoesFootwear), "category");
        // Men's Rain Shoes
        mockCategory(catMensRainShoes, "outdoorCat41110026", "Men's Rain Shoes", categoryCatalogs, newSet(catMensRainBootsShoes), "category");
        // Men's Rain Boots
        mockCategory(catMensRainBoots, "outdoorCat41110025", "Men's Rain Boots", categoryCatalogs, newSet(catMensRainBootsShoes), "category");
        // Snowshoe
        mockCategory(catSnowshoe, "outdoorCat11000003", "Snowshoe", categoryCatalogs, newSet(catRoot), "category");
        // Snowshoe Accessories
        mockCategory(catSnowshoeAccessories, "outdoorCat111000028", "Snowshoe Accessories", categoryCatalogs, newSet(catSnowshoe), "category");
        // Snowshoe Footwear
        mockCategory(catSnowshoeFootwear, "outdoorCat111100030", "Snowshoe Footwear", categoryCatalogs, newSet(catSnowshoeAccessories), "category");
        // Snowshoe boots
        mockCategory(catSnowshoeBoots, "outdoorCat111110031", "Snowshoe Boots", categoryCatalogs, newSet(catSnowshoeFootwear), "category");
        
        
        when(prodMensBoot.getPropertyValue("parentCategories")).thenReturn(newSet(catMensRainShoes, catMensRainBoots, catSnowshoeBoots));

        // feed
        feed.setLoggingInfo(false);
        feed.setLoggingDebug(false);
        feed.setLoggingWarning(false);
        feed.setLoggingTrace(false);
        feed.setLoggingError(false);
    }

    @Test
    public void testDuplicateCategories() throws RepositoryException, InventoryException {
        //final Set<String> categories = new HashSet<String>();

        doCallRealMethod().when(sku).setAssigned(anyBoolean());
        when(sku.isAssigned()).thenCallRealMethod();

        feed.checkSkuAssigned(sku, prodMensBoot, newSet(catalogOutdoor));

        /*verify(sku, times(1)).addCategory("catRoot");
        verify(sku, times(1)).addCategory("outdoorCat4000003");
        verify(sku, times(1)).addCategory("outdoorCat4100004");
        verify(sku, times(1)).addCategory("outdoorCat41100024");
        verify(sku, times(1)).addCategory("outdoorCat41110026");
        verify(sku, times(1)).addCategory("outdoorCat41110025");
        verify(sku, times(1)).addCategory("outdoorCat100003");
        verify(sku, times(1)).addCategory("outdoorCat11000219");
        verify(sku, times(1)).addCategory("outdoorCat11000003");
        verify(sku, times(1)).addCategory("outdoorCat111000028");
        verify(sku, times(1)).addCategory("outdoorCat111100030");
        verify(sku, times(1)).addCategory("outdoorCat111110031");
        verify(sku, never()).addCategory(anyString()); */
        verify(sku).setAssigned(true);
    }

    @Test
    public void testCategoryNotInCurrentCatalog() throws RepositoryException, InventoryException {

        // @todo: seems like this test isn't useful anymore
        doCallRealMethod().when(sku).setAssigned(anyBoolean());
        when(sku.isAssigned()).thenCallRealMethod();

    	RepositoryItem otherCatalog = mock(RepositoryItem.class);
    	when(otherCatalog.getRepositoryId()).thenReturn("otherCatalog");
    	Set<RepositoryItem> categoryCatalogs = newSet(otherCatalog);

    	RepositoryItem rootOtherCategory = mock(RepositoryItem.class);
    	mockCategory(rootOtherCategory, "rootOtherCategory", "Root Other Category", categoryCatalogs, null, "category");

    	RepositoryItem otherCategory = mock(RepositoryItem.class);
    	mockCategory(otherCategory, "otherCategory", "Other Category", categoryCatalogs, newSet(rootOtherCategory), "category");

    	RepositoryItem product = mock(RepositoryItem.class);
    	when(product.getPropertyValue("parentCategories")).thenReturn(newSet(catSnowshoeBoots, otherCategory));

        feed.checkSkuAssigned(sku, product, newSet(catalogOutdoor));

        /*verify(sku, times(1)).addCategory("catRoot");
        verify(sku, times(1)).addCategory("outdoorCat11000003");
        verify(sku, times(1)).addCategory("outdoorCat111000028");
        verify(sku, times(1)).addCategory("outdoorCat111100030");
        verify(sku, times(1)).addCategory("outdoorCat111110031");
        verify(sku, times(5)).addCategory(anyString()); */
        verify(sku).setAssigned(true);
    }

    @Test
    public void testSendProducts() throws ServiceException {
        SearchFeed feed = new SearchFeed() {
            @Override
            protected void onFeedStarted(FeedType type, long feedTimestamp) {

            }

            @Override
            protected void onProductsSent(FeedType type, long feedTimestamp, Locale locale, List<Product> productList, Response response) {

            }

            @Override
            protected void onProductsSentError(FeedType type, long feedTimestamp, Locale locale, List<Product> productList, Exception ex) {

            }

            @Override
            protected void onProductsSentError(FeedType type, long feedTimestamp, Locale locale, List<Product> productList, Response response) {

            }

            @Override
            protected void onFeedFinished(FeedType type, long feedTimestamp) {

            }

            @Override
            protected void processProduct(RepositoryItem product, SearchFeedProducts products) throws RepositoryException, InventoryException {

            }
        };

        ProductService productService = mock(ProductService.class);
        Response response = mock(Response.class);
        when(productService.handle(any(Request.class))).thenReturn(response);
        when(response.getStatus())
                .thenReturn(Status.SUCCESS_CREATED)
                .thenReturn(Status.SERVER_ERROR_INTERNAL)
                .thenReturn(Status.SUCCESS_CREATED);
        feed.setProductService(productService);
        feed.setWorkerCount(1);
        feed.doStartService();

        SearchFeedProducts products = new SearchFeedProducts();
        Product p = new Product();
        p.addSku(new Sku());
        p.addSku(new Sku());
        p.addSku(new Sku());
        products.add(Locale.US, p);
        products.add(Locale.CANADA, p);

        feed.setIndexBatchSize(2);

        //Async returns zero.
        int sent = feed.sendProducts(products, SearchFeed.FeedType.FULL_FEED, 0, 2, true);
        assertEquals(0, sent);

        products = new SearchFeedProducts();
        products.add(Locale.US, p);
        products.add(Locale.CANADA, p);

        //Async returns product count when index batch is not reached
        sent = feed.sendProducts(products, SearchFeed.FeedType.FULL_FEED, 0, 3, true);
        assertEquals(2, sent);
        assertEquals(2, products.getProductCount());

        products = new SearchFeedProducts();
        products.add(Locale.US, p);
        products.add(Locale.CANADA, p);

        //Sync returns product count when index batch not reached
        sent = feed.sendProducts(products, SearchFeed.FeedType.FULL_FEED, 0, 3, false);
        assertEquals(2, sent);
        assertEquals(2, products.getProductCount());

        //Test sync with batch failure
        sent = feed.sendProducts(products, SearchFeed.FeedType.FULL_FEED, 0, 2, false);
        assertEquals(1, sent);
        assertEquals(0, products.getProductCount());
        assertEquals(1, feed.getCurrentFailedProductCount());

        products = new SearchFeedProducts();
        products.add(Locale.US, p);
        products.add(Locale.CANADA, p);

        //Test sync with batch reached
        sent = feed.sendProducts(products, SearchFeed.FeedType.FULL_FEED, 0, 2, false);
        assertEquals(2, sent);
        assertEquals(0, products.getProductCount());
    }

    @Test
    public void testRulesBasedCategory() throws RepositoryException, InventoryException {

        doCallRealMethod().when(sku).setAssigned(anyBoolean());
        when(sku.isAssigned()).thenCallRealMethod();

    	RepositoryItem product = mock(RepositoryItem.class);

    	when(product.getPropertyValue("parentCategories")).thenReturn(newSet(catSnowshoeBoots, catRulesBased));

        feed.checkSkuAssigned(sku, product, newSet(catalogOutdoor));

        /*verify(sku, times(1)).addCategory("catRoot");
        verify(sku, times(1)).addCategory("outdoorCat11000003");
        verify(sku, times(1)).addCategory("outdoorCat111000028");
        verify(sku, times(1)).addCategory("outdoorCat111100030");
        verify(sku, times(1)).addCategory("outdoorCat111110031");
        verify(sku, times(1)).addCategory("catRulesBased");
        verify(sku, times(6)).addCategory(anyString());*/
        verify(sku).setAssigned(true);
    }
        
    private void mockCategory(RepositoryItem category, String categoryId, String displayName, Set<RepositoryItem> categoryCatalogs, Set<RepositoryItem>  parentCategories, String itemDescriptorName) throws RepositoryException{
    	when(category.getRepositoryId()).thenReturn(categoryId);
        when(category.getItemDisplayName()).thenReturn(displayName);
        when(category.getPropertyValue("catalogs")).thenReturn(categoryCatalogs);
        when(category.getPropertyValue("fixedParentCategories")).thenReturn(parentCategories);
        RepositoryItemDescriptor itemDescriptor = mock(RepositoryItemDescriptor.class);
        when(itemDescriptor.getItemDescriptorName()).thenReturn(itemDescriptorName);
		when(category.getItemDescriptor()).thenReturn(itemDescriptor );
    }

    private Set<RepositoryItem> newSet(RepositoryItem... items) {
        Set<RepositoryItem> set = new HashSet<RepositoryItem>(items.length);
        Collections.addAll(set, items);
        return set;
    }
}
