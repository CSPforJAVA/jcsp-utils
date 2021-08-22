package jcsp.helpers;

import java.util.ArrayList;
import jcsp.lang.Alternative;

import jcsp.lang.AltingChannelInput;
import jcsp.lang.AltingFunctionChannel;
import jcsp.lang.Any2OneChannel;
import jcsp.lang.CSProcess;
import jcsp.lang.Channel;
import jcsp.lang.ChannelInput;
import jcsp.lang.ChannelOutput;
import jcsp.lang.Guard;
import jcsp.lang.One2OneChannel;
import jcsp.lang.Parallel;
import jcsp.lang.PoisonException;
import jcsp.plugNplay.ProcessWrite;

/**
 * Stores an object.
 *
 * @author erhannis
 *
 * @param <T>
 */
public class CacheProcess<T> extends SynchronousSplitter<T> {

    private T val = null;
    private final AltingFunctionChannel<Void, T> getFChannel;
    public final FCClient<Void, T> getFC;

    public CacheProcess() {
        super();
        getFChannel = new AltingFunctionChannel<Void, T>();
        getFC = getFChannel.getClient();
    }

    public CacheProcess(AltingChannelInput<T> in) {
        super(in);
        getFChannel = new AltingFunctionChannel<Void, T>();
        getFC = getFChannel.getClient();
    }

    public CacheProcess(int immunity) {
        super(immunity);
        getFChannel = new AltingFunctionChannel<Void, T>(immunity);
        getFC = getFChannel.getClient();
    }

    public CacheProcess(int immunity, AltingChannelInput<T> in) {
        super(immunity, in);
        getFChannel = new AltingFunctionChannel<Void, T>(immunity);
        getFC = getFChannel.getClient();
    }

    public void run() {
        try {
            Guard[] guards = new Guard[ins.size() + 1];
            guards[0] = getFChannel;
            for (int i = 0; i < guards.length - 1; i++) {
                guards[i + 1] = ins.get(i);
            }
            Alternative alt = new Alternative(guards);
            while (true) {
                int i = alt.priSelect();
                if (i == 0) {
                    getFChannel.startRead();
                    getFChannel.endRead(val);
                } else {
                    AltingChannelInput<T> in = ins.get(i - 1);
                    T value = in.startRead();
                    this.val = value;
                    synchronized (writers) {
                        for (ProcessWrite writer : writers) {
                            writer.value = value;
                        }
                        writersProcess.run();
                    }
                    in.endRead();
                }
            }
        } catch (PoisonException e) {
            this.poison(e.getStrength());
            throw e;
        }
    }

    public void poison(int strength) {
        super.poison(strength);
        getFChannel.poison(strength);
    }

    public T get() {
        return getFChannel.call(null);
    }
}
