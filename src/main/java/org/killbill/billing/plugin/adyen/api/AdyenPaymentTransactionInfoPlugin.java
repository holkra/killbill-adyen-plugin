/*
 * Copyright 2014 Groupon, Inc
 *
 * Groupon licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.adyen.api;

import java.math.BigDecimal;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.payment.plugin.api.PaymentPluginStatus;
import org.killbill.billing.plugin.adyen.client.model.PaymentModificationResponse;
import org.killbill.billing.plugin.adyen.client.model.PaymentServiceProviderResult;
import org.killbill.billing.plugin.adyen.client.model.PurchaseResult;
import org.killbill.billing.plugin.adyen.client.payment.exception.ModificationFailedException;
import org.killbill.billing.plugin.adyen.dao.gen.tables.records.AdyenResponsesRecord;
import org.killbill.billing.plugin.api.PluginProperties;
import org.killbill.billing.plugin.api.payment.PluginPaymentTransactionInfoPlugin;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

public class AdyenPaymentTransactionInfoPlugin extends PluginPaymentTransactionInfoPlugin {

    public AdyenPaymentTransactionInfoPlugin(final UUID kbPaymentId,
                                             final UUID kbTransactionPaymentPaymentId,
                                             final TransactionType transactionType,
                                             final BigDecimal amount,
                                             final Currency currency,
                                             final DateTime utcNow,
                                             final PaymentPluginStatus paymentPluginStatus) {
        super(kbPaymentId,
              kbTransactionPaymentPaymentId,
              transactionType,
              amount,
              currency,
              paymentPluginStatus,
              null,
              null,
              null,
              null,
              utcNow,
              utcNow,
              ImmutableList.<PluginProperty>of());
    }

    public AdyenPaymentTransactionInfoPlugin(final UUID kbPaymentId,
                                             final UUID kbTransactionPaymentPaymentId,
                                             final TransactionType transactionType,
                                             final BigDecimal amount,
                                             final Currency currency,
                                             final DateTime utcNow,
                                             final PurchaseResult purchaseResult) {
        super(kbPaymentId,
              kbTransactionPaymentPaymentId,
              transactionType,
              amount,
              currency,
              getPaymentPluginStatus(purchaseResult.getResult()),
              purchaseResult.getResultCode(),
              purchaseResult.getReason(),
              purchaseResult.getPspReference(),
              purchaseResult.getAuthCode(),
              utcNow,
              utcNow,
              PluginProperties.buildPluginProperties(purchaseResult.getFormParameter()));
    }

    public AdyenPaymentTransactionInfoPlugin(final UUID kbPaymentId,
                                             final UUID kbTransactionPaymentPaymentId,
                                             final TransactionType transactionType,
                                             final BigDecimal amount,
                                             @Nullable final Currency currency,
                                             final PaymentServiceProviderResult pspResult,
                                             final DateTime utcNow,
                                             final PaymentModificationResponse paymentModificationResponse) {
        super(kbPaymentId,
              kbTransactionPaymentPaymentId,
              transactionType,
              amount,
              currency,
              getPaymentPluginStatus(pspResult),
              paymentModificationResponse.getResponse(),
              null,
              paymentModificationResponse.getPspReference(),
              null,
              utcNow,
              utcNow,
              PluginProperties.buildPluginProperties(paymentModificationResponse.getAdditionalData()));
    }

    public AdyenPaymentTransactionInfoPlugin(final UUID kbPaymentId,
                                             final UUID kbTransactionPaymentPaymentId,
                                             final TransactionType transactionType,
                                             final BigDecimal amount,
                                             final Currency currency,
                                             final PaymentServiceProviderResult pspResult,
                                             final DateTime utcNow,
                                             final ModificationFailedException e) {
        super(kbPaymentId,
              kbTransactionPaymentPaymentId,
              transactionType,
              amount,
              currency,
              getPaymentPluginStatus(pspResult),
              e.getLocalizedMessage(),
              null,
              null,
              null,
              utcNow,
              utcNow,
              ImmutableList.<PluginProperty>of());
    }

    public AdyenPaymentTransactionInfoPlugin(final AdyenResponsesRecord record) {
        super(UUID.fromString(record.getKbPaymentId()),
              UUID.fromString(record.getKbPaymentTransactionId()),
              TransactionType.valueOf(record.getTransactionType()),
              record.getAmount(),
              Strings.isNullOrEmpty(record.getCurrency()) ? null : Currency.valueOf(record.getCurrency()),
              Strings.isNullOrEmpty(record.getPspResult()) ? null : getPaymentPluginStatus(PaymentServiceProviderResult.getPaymentResultForId(record.getPspResult())),
              record.getResultCode(),
              record.getRefusalReason(),
              record.getPspReference(),
              record.getAuthCode(),
              new DateTime(record.getCreatedDate(), DateTimeZone.UTC),
              new DateTime(record.getCreatedDate(), DateTimeZone.UTC),
              AdyenModelPluginBase.buildPluginProperties(record.getAdditionalData()));
    }

    private static PaymentPluginStatus getPaymentPluginStatus(final PaymentServiceProviderResult pspResult) {
        switch (pspResult) {
            case INITIALISED:
            case REDIRECT_SHOPPER:
            case RECEIVED:
            case PENDING:
            case AUTHORISED:
                return PaymentPluginStatus.PENDING;
            case REFUSED:
            case ERROR:
            case CANCELLED:
                return PaymentPluginStatus.ERROR;
            default:
                return PaymentPluginStatus.UNDEFINED;
        }
    }
}
