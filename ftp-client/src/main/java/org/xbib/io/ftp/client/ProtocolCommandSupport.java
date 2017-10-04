package org.xbib.io.ftp.client;

import java.io.Serializable;
import java.util.EventListener;

/**
 * ProtocolCommandSupport is a convenience class for managing a list of
 * ProtocolCommandListeners and firing ProtocolCommandEvents.  You can
 * simply delegate ProtocolCommandEvent firing and listener
 * registering/unregistering tasks to this class.
 *
 *
 * @see ProtocolCommandEvent
 * @see ProtocolCommandListener
 */
public class ProtocolCommandSupport implements Serializable {
    private static final long serialVersionUID = -8017692739988399978L;

    private final Object object;
    private final ListenerList listenerList;

    /***
     * Creates a ProtocolCommandSupport instance using the indicated source
     * as the source of ProtocolCommandEvents.
     *
     * @param source  The source to use for all generated ProtocolCommandEvents.
     ***/
    public ProtocolCommandSupport(Object source) {
        listenerList = new ListenerList();
        object = source;
    }


    /***
     * Fires a ProtocolCommandEvent signalling the sending of a command to all
     * registered listeners, invoking their
     * {@link ProtocolCommandListener#protocolCommandSent protocolCommandSent() }
     *  methods.
     *
     * @param command The string representation of the command type sent, not
     *      including the arguments (e.g., "STAT" or "GET").
     * @param message The entire command string verbatim as sent to the server,
     *        including all arguments.
     ***/
    public void fireCommandSent(String command, String message) {
        ProtocolCommandEvent event;

        event = new ProtocolCommandEvent(object, command, message);

        for (EventListener listener : listenerList) {
            ((ProtocolCommandListener) listener).protocolCommandSent(event);
        }
    }

    /***
     * Fires a ProtocolCommandEvent signalling the reception of a command reply
     * to all registered listeners, invoking their
     * {@link ProtocolCommandListener#protocolReplyReceived protocolReplyReceived() }
     *  methods.
     *
     * @param replyCode The integer code indicating the natureof the reply.
     *   This will be the protocol integer value for protocols
     *   that use integer reply codes, or the reply class constant
     *   corresponding to the reply for protocols like POP3 that use
     *   strings like OK rather than integer codes (i.e., POP3Repy.OK).
     * @param message The entire reply as received from the server.
     ***/
    public void fireReplyReceived(int replyCode, String message) {
        ProtocolCommandEvent event;
        event = new ProtocolCommandEvent(object, replyCode, message);

        for (EventListener listener : listenerList) {
            ((ProtocolCommandListener) listener).protocolReplyReceived(event);
        }
    }

    /***
     * Adds a ProtocolCommandListener.
     *
     * @param listener  The ProtocolCommandListener to add.
     ***/
    public void addProtocolCommandListener(ProtocolCommandListener listener) {
        listenerList.addListener(listener);
    }

    /***
     * Removes a ProtocolCommandListener.
     *
     * @param listener  The ProtocolCommandListener to remove.
     ***/
    public void removeProtocolCommandListener(ProtocolCommandListener listener) {
        listenerList.removeListener(listener);
    }


    /***
     * Returns the number of ProtocolCommandListeners currently registered.
     *
     * @return The number of ProtocolCommandListeners currently registered.
     ***/
    public int getListenerCount() {
        return listenerList.getListenerCount();
    }

}

