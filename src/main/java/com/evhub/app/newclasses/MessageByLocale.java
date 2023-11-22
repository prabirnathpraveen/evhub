package com.evhub.app.newclasses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageByLocale {

    @Autowired
    MessageSource messageSource;

    /**
     * It returns the Locale associated with the current thread if available,
     * otherwise it returns the default Locale.
     * 
     * @return locale of {Locale} type.
     */
    public Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

    /**
     * It returns the message according to region's language if available, otherwise
     * it returns the default message.
     * 
     * @param msg it is the actual message to be shown.
     * @return message with associated Locale of {String} type.
     */
    public String getMessage(String msg) {
        return messageSource.getMessage(msg, null, getLocale());
    }

}
// package com.evhub.app.locale;
//
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.MessageSource;
// import org.springframework.context.i18n.LocaleContextHolder;
// import org.springframework.stereotype.Component;
//
// import java.util.Locale;
//
/// **
// * This class is used for the process in which software adapt a specific
// region's language by adding locale-specific components and translating text.
// * <br><br>{@link #getLocale()} It returns the Locale associated with the
// current thread if available, otherwise it returns the default Locale.
// * <br><br>{@link #getMessage(String)} It returns the message according to
// region's language if available, otherwise it returns the default message.
// */
// @Component
// public class MessageByLocale {
//
// @Autowired
// MessageSource messageSource;
//
// /**
// * It returns the Locale associated with the current thread if available,
// otherwise it returns the default Locale.
// * @return locale of {Locale} type.
// */
// public Locale getLocale() {
// return LocaleContextHolder.getLocale();
// }
//
// /**
// * It returns the message according to region's language if available,
// otherwise it returns the default message.
// * @param msg it is the actual message to be shown.
// * @return message with associated Locale of {String} type.
// */
// public String getMessage(String msg) {
// return messageSource.getMessage(msg, null, getLocale());
// }
//
// }