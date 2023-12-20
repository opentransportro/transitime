/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Delegate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.transitclock.gtfs.GtfsFareAttribute;

import java.io.Serializable;

/**
 * Contains data from the fareattributes.txt GTFS file. This class is for reading/writing that data
 * to the db.
 *
 * @author SkiBu Smith
 */
@EqualsAndHashCode
@ToString
@Getter
@Document(collection = "FareAttributes")
public class FareAttribute implements Serializable {
    @Data
    public static class Key {
        private final int configRev;
        private final String fareId;
    }

    @Id
    @Delegate
    private final Key key;

    private final float price;

    private final String currencyType;

    private final String paymentMethod;

    private final String transfers;

    private final Integer transferDuration;

    public FareAttribute(int configRev, GtfsFareAttribute gf) {
        this.key = new Key(configRev, gf.getFareId());
        this.price = gf.getPrice();
        this.currencyType = gf.getCurrencyType();
        this.paymentMethod = gf.getPaymentMethod();
        this.transfers = gf.getTransfers();
        this.transferDuration = gf.getTransferDuration();
    }
}
