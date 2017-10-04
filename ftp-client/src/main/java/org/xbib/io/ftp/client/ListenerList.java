package org.xbib.io.ftp.client;

import java.io.Serializable;
import java.util.EventListener;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 */

public class ListenerList implements Serializable, Iterable<EventListener> {
    private static final long serialVersionUID = -1934227607974228213L;

    private final CopyOnWriteArrayList<EventListener> listeners;

    public ListenerList() {
        listeners = new CopyOnWriteArrayList<>();
    }

    public void addListener(EventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(EventListener listener) {
        listeners.remove(listener);
    }

    public int getListenerCount() {
        return listeners.size();
    }

    /**
     * Return an {@link Iterator} for the {@link EventListener} instances.
     *
     * @return an {@link Iterator} for the {@link EventListener} instances
     */
    @Override
    public Iterator<EventListener> iterator() {
        return listeners.iterator();
    }

}
