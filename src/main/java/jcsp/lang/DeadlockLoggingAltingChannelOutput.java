package jcsp.lang;

import jcsp.helpers.JcspUtils;
import jcsp.helpers.JcspUtils._DeadlockLogger;

public class DeadlockLoggingAltingChannelOutput<T> extends AltingChannelOutput<T> {

    private final AltingChannelOutput<T> out;

    public DeadlockLoggingAltingChannelOutput(AltingChannelOutput<T> out) {
        this.out = out;
    }

    public void poison(int strength) {
        out.poison(strength);
    }

    public void write(T object) {
        write(object, null);
    }

    public void write(T object, String tag) {
        JcspUtils._DeadlockLogger.DeadlockTag tag0;
        try {
            // TODO This may be kinda heavy.
            if (tag != null) {
                throw new RuntimeException("Deadlock detected! " + tag);
            } else {
                throw new RuntimeException("Deadlock detected!");
            }
        } catch (Exception e) {
            tag0 = new _DeadlockLogger.DeadlockTag(e);
        }
        _DeadlockLogger.startOut.write(tag0);
        try {
            out.write(object);
        } finally {
            _DeadlockLogger.stopOut.write(tag0);
        }
    }

    @Override
    public boolean pending() {
        return out.pending();
    }

    @Override
    boolean enable(Alternative alt) {
        return out.enable(alt);
    }

    @Override
    boolean disable() {
        return out.disable();
    }
}
