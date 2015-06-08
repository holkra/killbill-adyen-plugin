package org.killbill.billing.plugin.adyen.client.payment.converter.impl;

import org.killbill.billing.plugin.adyen.client.model.PaymentType;

/**
 * @author ehaertel
 */
public class AmexConverter extends CreditCardConverter {

    private static final long serialVersionUID = -4115035959530897618L;

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.AMEX;
    }

}