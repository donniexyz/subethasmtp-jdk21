package org.subethamail.smtp.auth;

import lombok.Getter;

/**
 * This a convenient class that saves you setting up the factories that we know
 * about; you can always add more afterward. Currently, this factory supports:
 * <p>
 * PLAIN LOGIN
 *
 * @author Jeff Schnitzer
 */
@Getter
public class EasyAuthenticationHandlerFactory extends MultipleAuthenticationHandlerFactory {
    /**
     * Just hold on to this so that the caller can get it later, if necessary
     */
    UsernamePasswordValidator validator;

    /** */
    public EasyAuthenticationHandlerFactory(UsernamePasswordValidator validator) {
        this.validator = validator;

        this.addFactory(new PlainAuthenticationHandlerFactory(this.validator));
        this.addFactory(new LoginAuthenticationHandlerFactory(this.validator));
    }

}
