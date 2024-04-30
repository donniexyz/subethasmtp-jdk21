/*
 * $Id: SimpleMessageListenerAdapter.java 320 2009-05-20 09:19:20Z lhoriman $
 * $URL: https://subethasmtp.googlecode.com/svn/trunk/src/org/subethamail/smtp/helper/SimpleMessageListenerAdapter.java $
 */
package org.subethamail.smtp.helper;

import org.subethamail.smtp.*;
import org.subethamail.smtp.helper.SmarterMessageListener.Receiver;
import org.subethamail.smtp.io.DeferredFileOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * MessageHandlerFactory implementation which adapts to a collection of
 * SmarterMessageListeners.  This is actually half-way between the
 * SimpleMessageListener interface and the raw MessageHandler.
 * <p>
 * The key point is that for any message, every accepted recipient will get a
 * separate delivery.
 *
 * @author Jeff Schnitzer
 */
@SuppressWarnings("unused")
public class SmarterMessageListenerAdapter implements MessageHandlerFactory {
    /**
     * 5 megs by default. The server will buffer incoming messages to disk
     * when they hit this limit in the DATA received.
     */
    private static final int DEFAULT_DATA_DEFERRED_SIZE = 1024 * 1024 * 5;

    private final Collection<SmarterMessageListener> listeners;
    private int dataDeferredSize;

    /**
     * Initializes this factory with a single listener.
     * <p>
     * Default data deferred size is 5 megs.
     */
    public SmarterMessageListenerAdapter(SmarterMessageListener listener) {
        this(Collections.singleton(listener), DEFAULT_DATA_DEFERRED_SIZE);
    }

    /**
     * Initializes this factory with the listeners.
     * <p>
     * Default data deferred size is 5 megs.
     */
    public SmarterMessageListenerAdapter(Collection<SmarterMessageListener> listeners) {
        this(listeners, DEFAULT_DATA_DEFERRED_SIZE);
    }

    /**
     * Initializes this factory with the listeners.
     * @param dataDeferredSize The server will buffer
     *        incoming messages to disk when they hit this limit in the
     *        DATA received.
     */
    public SmarterMessageListenerAdapter(Collection<SmarterMessageListener> listeners, int dataDeferredSize) {
        this.listeners = listeners;
        this.dataDeferredSize = dataDeferredSize;
    }

    /* (non-Javadoc)
     * @see org.subethamail.smtp.MessageHandlerFactory#create(org.subethamail.smtp.MessageContext)
     */
    public MessageHandler create(MessageContext ctx) {
        return new Handler(ctx);
    }

    /**
     * Class which implements the actual handler interface.
     */
    class Handler implements MessageHandler {
        MessageContext ctx;
        String from;
        List<Receiver> deliveries = new ArrayList<>();

        /** */
        public Handler(MessageContext ctx) {
            this.ctx = ctx;
        }

        /** */
        public void from(String from) throws RejectException {
            this.from = from;
        }

        /** */
        public void recipient(String recipient) throws RejectException {
            for (SmarterMessageListener listener : SmarterMessageListenerAdapter.this.listeners) {
                Receiver rec = listener.accept(this.from, recipient);

                if (rec != null)
                    this.deliveries.add(rec);
            }

            if (this.deliveries.isEmpty())
                throw new RejectException(553, "<" + recipient + "> address unknown.");
        }

        /** */
        public void data(InputStream data) throws TooMuchDataException, IOException {
            if (this.deliveries.size() == 1) {
                this.deliveries.get(0).deliver(data);
            } else {

                try (DeferredFileOutputStream dfos = new DeferredFileOutputStream(SmarterMessageListenerAdapter.this.dataDeferredSize)) {
                    int value;
                    while ((value = data.read()) >= 0) {
                        dfos.write(value);
                    }

                    for (Receiver rec : this.deliveries) {
                        rec.deliver(dfos.getInputStream());
                    }
                }
            }
        }

        /** */
        public void done() {
            for (Receiver rec : this.deliveries) {
                rec.done();
            }
        }
    }
}
