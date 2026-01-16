package com.example.MovieBooking.service;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    @Value("${stripe.api.key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        // Initialize the Stripe SDK with your secret key
        Stripe.apiKey = secretKey;
    }

    /**
     * Creates a PaymentIntent in Stripe.
     * @param amount The total amount in the smallest currency unit (e.g., cents for USD, paise for INR).
     * @param currency The currency code (e.g., "usd", "inr").
     * @return The PaymentIntent object.
     */
    public PaymentIntent createPaymentIntent(BigDecimal amount, String currency, String bookingId) throws StripeException {
        // Stripe expects amounts as Long in the smallest currency unit
        // Example: $10.00 = 1000 cents
        long amountInSmallestUnit = amount.multiply(new BigDecimal(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInSmallestUnit)
                .setCurrency(currency)
                // Metadata helps you identify the booking in your dashboard or webhooks
                .putMetadata("booking_id", bookingId)
                .build();

        return PaymentIntent.create(params);
    }
}