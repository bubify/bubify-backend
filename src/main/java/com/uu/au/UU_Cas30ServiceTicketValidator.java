package com.uu.au;

import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;

public class UU_Cas30ServiceTicketValidator extends Cas30ServiceTicketValidator {
    public UU_Cas30ServiceTicketValidator(final String casServerUrlPrefix) {
        super(casServerUrlPrefix);
    }

    @Override
    protected String getUrlSuffix() {
        return "serviceValidate";
    }
}
