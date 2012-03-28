package edu.asu.commons.event;

import java.util.Collection;

/**
 * $ Id: Exp $
 * 
 * Interface marking a bundle of event processors.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 454 $
 */
public interface EventHandlingModule {

    public void bind(EventChannel channel);

    public void unbind();

    public abstract static class Base implements EventHandlingModule {

        private EventChannel channel;

        private boolean bound;

        protected abstract Collection<EventProcessor<?>> getProcessors();

        public synchronized void bind(EventChannel channel) {
            if (bound) {
                return;
            }
            this.channel = channel;
            // channel.add(this, getProcessors());
            for (EventProcessor<?> processor : getProcessors()) {
                channel.add(this, processor);
            }
            bound = true;
        }

        public synchronized void unbind() {
            if (bound) {
                channel.remove(this);
                bound = false;
                channel = null;
            }
        }
    }
}
