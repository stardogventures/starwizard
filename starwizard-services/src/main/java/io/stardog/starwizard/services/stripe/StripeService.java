package io.stardog.starwizard.services.stripe;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import io.stardog.starwizard.services.common.AsyncService;
import io.stardog.starwizard.services.stripe.exceptions.UncheckedStripeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * StripeService is a wrapper around Stripe's Java API that makes it easier to use in the following ways:
 *   - more mockable/testable, does not rely on static invocations
 *   - wraps checked exceptions and throws UncheckedStripeExceptions instead
 *   - monetary amounts expressed as BigDecimals instead of cents
 *   - checks whether you are using a live stripe key in a non-prod environment, and logs a warning if you are
 */
@Singleton
public class StripeService {
    private final String signingSecret;
    private final Logger LOGGER = LoggerFactory.getLogger(StripeService.class);

    private StripeService(String apiKey, @Nullable String signingSecret) {
        Stripe.apiKey = apiKey;
        Stripe.setAppInfo("starwizard-services", "0.2.1", "https://github.com/stardogventures/starwizard");
        this.signingSecret = signingSecret;
    }

    public static StripeService of(String apiKey) {
        return new StripeService(apiKey, null);
    }

    public static StripeService of(String apiKey, String signingSecret) {
        return new StripeService(apiKey, signingSecret);
    }

    @Inject
    public StripeService(AsyncService asyncService,
                         @Named("stripeSecretKey") String apiKey,
                         @Named("stripeSigningSecret") String signingSecret,
                         @Named("env") String env) {
        this(apiKey, signingSecret);

        // in non-production environment, provide a warning if a live stripe key is in use
        if (!env.equals("prod") && apiKey.startsWith("sk_live_")) {
            asyncService.submit(() -> {
                try {
                    Thread.sleep(5000L);    // sleep long enough to get past normal startup phase, so this message will be visible
                } catch (InterruptedException e) {
                    // okay to swallow this
                }
                LOGGER.warn("***** WARNING: LIVE STRIPE KEY IN USE IN NON-PRODUCTION ENVIRONMENT " + env + " *****");
            });
        }
    }

    /**
     * Creates a new SetupIntent object.
     * @params params   Parameters to pass along to Stripe
     * @return created  Stripe Setup Intent object
     */
    public SetupIntent createSetupIntent(@Nullable Map<String, Object> params) {

        try{
            params = params != null ? params : new HashMap<>();
            return SetupIntent.create(params);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }

    }

    /**
     * Retrieves a payment method of the given ID. Throws an error if the given ID is not a valid payment method.
     * @return  Payment method object pulled from Stripe.
     */
    public PaymentMethod retrievePaymentMethod(String paymentMethodId) {

        try{
            return PaymentMethod.retrieve(paymentMethodId);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }

    }

    /**
     * Attaches the given payment method to the given customer.
     * @param paymentMethod     Payment method to attach to the customer
     * @param stripeCustomerId  Stripe ID of the customer to attach the payment method to
     */
    public void attachPaymentMethodToCustomer(PaymentMethod paymentMethod, String stripeCustomerId) {

        Preconditions.checkNotNull(paymentMethod);
        Preconditions.checkNotNull(stripeCustomerId);

        Map<String,Object> params = new HashMap<>();
        params.put("customer", stripeCustomerId);

        try{
            paymentMethod.attach(params);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }

    }


    /**
     * Run a one-off charge, either from a customer or a source token.
     * @param amount    amount of the charge
     * @param currency  currency of the charge
     * @param token token (should either be a source token, or a customer identifier)
     * @param description   description of the charge
     * @param metadata  additional metadata to pass
     * @return  the stripe Charge object
     * @throws UncheckedStripeException if something went wrong
     */
    public Charge createCharge(BigDecimal amount, Currency currency, String token, @Nullable String description, @Nullable Map<String,Object> metadata) {
        Preconditions.checkNotNull(amount);
        Preconditions.checkNotNull(currency);

        Map<String,Object> chargeParams = new HashMap<>();
        chargeParams.put("amount", amount.multiply(new BigDecimal("100")).longValue());
        chargeParams.put("currency", currency.getCurrencyCode());
        if (description != null) {
            chargeParams.put("description", description);
        }
        if (token.startsWith("cus_")) {
            chargeParams.put("customer", token);
        } else {
            chargeParams.put("source", token);
        }
        if (metadata != null) {
            chargeParams.put("metadata", metadata);
        }

        try {
            return Charge.create(chargeParams);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }

    /**
     * Refund some amount of a particular charge.
     * @param chargeId  charge id
     * @param amount    amount to refund
     * @param metadata  metadata to attach to the refund
     * @return  refund object
     * @throws UncheckedStripeException if something went wrong
     */
    public Refund refundCharge(String chargeId, BigDecimal amount, @Nullable Map<String,Object> metadata) {
        Preconditions.checkNotNull(chargeId);
        Preconditions.checkNotNull(amount);

        Map<String,Object> refundParams = new HashMap<>();
        refundParams.put("charge", chargeId);
        refundParams.put("amount", amount.multiply(new BigDecimal("100")).longValue());
        if (metadata != null) {
            refundParams.put("metadata", metadata);
        }

        try {
            return Refund.create(refundParams);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }

    @Deprecated
    public Customer createCustomer(@Nullable String token, String name, @Nullable String email, @Nullable Map<String,Object> metadata) {
        return createCustomer(name, email, token, null, metadata);
    }

    /**
     * Create a customer based on a charge token.
     * @param name  customer name
     * @param email customer email
     * @param token charge token
     * @param metadata  additional metadata to save for the customer
     * @return  stripe Customer object
     * @throws UncheckedStripeException if something went wrong
     */
    public Customer createCustomer(String name, @Nullable String email, @Nullable String token, @Nullable Address shippingAddress, @Nullable Map<String,Object> metadata) {
        Preconditions.checkNotNull(name);

        Map<String,Object> params = new HashMap<>();
        params.put("description", name);
        if (email != null) {
            params.put("email", email);
        }
        if (token != null) {
            params.put("source", token);
        }

        if (metadata != null) {
            params.put("metadata", metadata);
        }
        if (shippingAddress != null) {
            Map<String,Object> shippingAddressMap = new HashMap<>();
            shippingAddressMap.put("line1", shippingAddress.getLine1());
            shippingAddressMap.put("line2", shippingAddress.getLine2());
            shippingAddressMap.put("city", shippingAddress.getCity());
            shippingAddressMap.put("state", shippingAddress.getState());
            shippingAddressMap.put("country", shippingAddress.getCountry());
            shippingAddressMap.put("postal_code", shippingAddress.getPostalCode());
            params.put("shipping", ImmutableMap.of("name", name, "address", shippingAddressMap));
        }

        try {
            return Customer.create(params);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }

    /**
     * Return a customer
     * @param customerId    customer id
     * @return  customer data
     */
    public Customer getCustomer(String customerId) {
        Preconditions.checkNotNull(customerId);
        try {
            return Customer.retrieve(customerId);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }

    /**
     * Return a customer
     * @param customerId    customer id
     * @param customerId    parameters for the retrieve operation
     * @return  customer data
     */
    public Customer getCustomer(String customerId, Map<String, Object> params) {

        Preconditions.checkNotNull(customerId);
        Preconditions.checkNotNull(params);

        try {
            return Customer.retrieve(customerId, params, null);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }

    /**
     * Update a customer
     * @param customer  customer to update
     * @param update    update data
     * @return  updated customer
     */
    public Customer updateCustomer(Customer customer, Map<String,Object> update) {
        Preconditions.checkNotNull(customer);
        Preconditions.checkNotNull(update);
        try {
            return customer.update(update);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }

    /**
     * Change the credit card source for an existing customer by updating their source field
     * @param customerId    customerId
     * @param token new source token
     * @throws UncheckedStripeException if something went wrong
     */
    public void updateCustomerSource(String customerId, String token) {
        Preconditions.checkNotNull(customerId);
        Preconditions.checkNotNull(token);

        try {
            Customer cust = Customer.retrieve(customerId);

            Map<String, Object> params = ImmutableMap.of("source", token);
            cust.update(params);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }

    /**
     * Create a subscription for a customer, passing a list of planIds to subscribe to (quantity 1 for each)
     * @param customerId    customer id
     * @param planIds   plan ids to subscribe to
     * @param extraParams   any extra parameters to pass (Stripe has a lot of options)
     * @return  newly created subscription
     */
    public Subscription createSubscription(String customerId, Collection<String> planIds, @Nullable Map<String,Object> extraParams) {
        Map<String,Long> planQuantities = new LinkedHashMap<>();
        for (String planId : planIds) {
            planQuantities.put(planId, 1L);
        }
        return createSubscription(customerId, planQuantities, extraParams);
    }

    /**
     * Create a subscription for a customer with quantities more than 1
     * @param customerId    customer id
     * @param planQuantities    map of plan ids to quantities
     * @param extraParams   extra parameters
     * @return  newly created subscription
     */
    public Subscription createSubscription(String customerId, Map<String,Long> planQuantities, @Nullable Map<String,Object> extraParams) {
        Preconditions.checkNotNull(customerId);
        Preconditions.checkNotNull(planQuantities);

        Map<String,Object> params = new HashMap<>();
        params.put("customer", customerId);
        List<Map<String,Object>> items = new ArrayList<>();
        for (Map.Entry<String,Long> q : planQuantities.entrySet()) {
            items.add(q.getValue() > 1
                    ? ImmutableMap.of("plan", q.getKey(), "quantity", q.getValue())
                    : ImmutableMap.of("plan", q.getKey()));
        }
        params.put("items", items);
        if (extraParams != null) {
            params.putAll(extraParams);
        }
        try {
            return Subscription.create(params);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }

    /**
     * Update a subscription.
     * @param subscription  subscription to update
     * @param update    update data
     * @return  updated subscription
     */
    public Subscription updateSubscription(Subscription subscription, Map<String,Object> update) {
        Preconditions.checkNotNull(subscription);
        Preconditions.checkNotNull(update);

        try {
            return subscription.update(update);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }

    /**
     * Cancel a subscription.
     * @param subscription  subscription to cancel
     * @param params    parameters
     * @return  cancelled subscription
     */
    public Subscription cancelSubscription(Subscription subscription, @Nullable Map<String,Object> params) {
        Preconditions.checkNotNull(subscription);

        try {
            return subscription.cancel(params);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }

    /**
     * Retrieve a subscription
     * @param subscriptionId    subscription id
     * @return  Subscription data
     */
    public Subscription getSubscription(String subscriptionId) {
        Preconditions.checkNotNull(subscriptionId);
        try {
            return Subscription.retrieve(subscriptionId);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }

    public Plan getPlan(String planId) {
        Preconditions.checkNotNull(planId);
        try {
            return Plan.retrieve(planId);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }

    public PlanCollection findAllPlans() {
        try {
            return Plan.list(ImmutableMap.of());
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }

    /**
     * Record usage for a particular subscription item and a particular timestamp
     * @param subscriptionItemId    subscription item
     * @param timestamp timestamp to record the usage on
     * @param quantity  quantity of usage
     * @return  the usage record
     */
    public UsageRecord recordUsage(String subscriptionItemId, Instant timestamp, long quantity) {
        Preconditions.checkNotNull(subscriptionItemId);
        Preconditions.checkNotNull(timestamp);

        try {
            Map<String, Object> params = ImmutableMap.of(
                    "quantity", quantity,
                    "timestamp", timestamp.getEpochSecond()
            );
            return UsageRecord.createOnSubscriptionItem(subscriptionItemId, params, RequestOptions.builder().build());
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }

    /**
     * Given a particular payout, build a map of the charge ids, and the amount of each charge, that correspond to
     * that payout.
     * @param payoutId  payout id
     * @return  map of charge ids to payout amounts
     * @throws UncheckedStripeException if something went wrong
     */
    public Map<String, BigDecimal> getPayoutChargeIdsAndFees(String payoutId) {
        try {
            Map<String,Object> params = new HashMap<>();
            params.put("payout", payoutId);
            params.put("limit", 100);

            ImmutableMap.Builder<String,BigDecimal> result = ImmutableMap.builder();
            BalanceTransactionCollection list = BalanceTransaction.list(params);
            for (BalanceTransaction trans : list.autoPagingIterable()) {
                String chargeId = trans.getSource();
                BigDecimal fee = new BigDecimal(trans.getFee()).multiply(new BigDecimal(".01"));
                if (chargeId.startsWith("re_") || chargeId.startsWith("ch_")) {
                    result.put(chargeId, fee);
                }
            }
            return result.build();

        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }

    /**
     * Return Stripe event information
     * @param eventId   event id
     * @return  Stripe event object
     */
    public Event getEvent(String eventId) {
        try {
            return Event.retrieve(eventId);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }

    /**
     * Process a webhook given the raw payload and the signature header.
     * @param payload   payload content
     * @param signature contents of Stripe-Signature header
     * @return  the Stripe event object
     */
    public Event processWebhook(String payload, String signature) {
        try {
            return Webhook.constructEvent(payload, signature, signingSecret);
        } catch (SignatureVerificationException e) {
            throw new UncheckedStripeException(e);
        }
    }

    /**
     * Return an invoice with the given id
     * @param id    invoice id
     * @return  invoice object
     * @throws UncheckedStripeException if the invoice id is invalid
     */
    public Invoice getInvoice(String id) {
        try {
            return Invoice.retrieve(id);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }

    /**
     * Return a number of invoices
     * @param params    stripe parameters
     * @return  collection of invoices
     */
    public InvoiceCollection listInvoices(Map<String,Object> params) {
        try {
            return Invoice.list(params);
        } catch (StripeException e) {
            throw new UncheckedStripeException(e);
        }
    }
}
