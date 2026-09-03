package com.clover.sdk;

public enum PaymentType {
    /**
     * Order is created as open. The customer pays at the restaurant counter or via a Clover device.
     */
    PAY_AT_RESTAURANT,

    /**
     * Order is created and then marked as paid by recording a payment against it.
     * Use this when the customer has already paid online. Requires the Payments
     * permission on the API token.
     */
    PREPAID
}
