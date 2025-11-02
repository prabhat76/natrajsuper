package com.example.natraj.data

import android.content.Context
import com.example.natraj.Category
import com.example.natraj.Product
import com.example.natraj.data.woo.*

class WooRepository(private val context: Context) {
    private val api by lazy { WooClient.api(context) }

    suspend fun getCategories(): List<Category> {
        val list = api.getCategories(perPage = 100, hideEmpty = false)
        return list.map { wc ->
            Category(
                id = wc.id,
                name = wc.name,
                imageUrl = wc.image?.src ?: "",
                hasSpecialOffer = false,
                productCount = wc.count ?: 0
            )
        }
    }

    suspend fun getProducts(params: FilterParams, featured: Boolean? = null): List<Product> {
        val items = api.getProducts(
            perPage = params.perPage,
            page = params.page,
            category = params.categoryId,
            minPrice = params.minPrice,
            maxPrice = params.maxPrice,
            attribute = params.attribute,
            attributeTerm = params.attributeTerm,
            featured = featured
        )
        return items.map { mapProduct(it) }
    }

    suspend fun getAttributes(): List<WooAttribute> = api.getAttributes()

    suspend fun getAttributeTerms(attributeId: Int): List<WooAttributeTerm> = api.getAttributeTerms(attributeId)

    suspend fun placeOrder(
        billing: WooBilling,
        shipping: WooShipping,
        lineItems: List<WooOrderLineItem>,
        paymentMethod: String = "cod",
        paymentTitle: String = "Cash on Delivery",
        setPaid: Boolean = false
    ): WooOrderResponse {
        val body = WooCreateOrderRequest(
            payment_method = paymentMethod,
            payment_method_title = paymentTitle,
            set_paid = setPaid,
            billing = billing,
            shipping = shipping,
            line_items = lineItems
        )
        return api.createOrder(body)
    }

    private fun mapProduct(p: WooProduct): Product {
        // Choose price precedence: sale -> regular -> price
        val priceStr = p.sale_price?.takeIf { it.isNotBlank() }
            ?: p.regular_price?.takeIf { it.isNotBlank() }
            ?: p.price
        val price = priceStr?.toDoubleOrNull() ?: 0.0
        val mrp = p.regular_price?.toDoubleOrNull() ?: price
        val images = (p.images ?: emptyList()).mapNotNull { it.src }
        val firstImage = images.firstOrNull() ?: ""
        val categoryName = p.categories?.firstOrNull()?.name ?: ""

        return Product(
            id = p.id,
            name = p.name,
            price = price,
            originalPrice = mrp,
            imageUrl = firstImage,
            images = images,
            description = p.description ?: p.short_description.orEmpty(),
            category = categoryName,
            isFeatured = false
        )
    }
}
