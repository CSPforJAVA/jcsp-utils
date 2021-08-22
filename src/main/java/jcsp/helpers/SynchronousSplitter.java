package jcsp.helpers;

import java.util.ArrayList;
import jcsp.lang.Alternative;

import jcsp.lang.AltingChannelInput;
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
 * Writes from `in` to all registered out channels. Outputs are written in
 * parallel. `Synchronous` in that the `in` read does not complete until all
 * outs have completed writing, giving an illusion that `in` is a non-buffered
 * channel writing to all of a fixed set of outputs.
 *
 * Should be safe to register a new channel while running, though possibly bad
 * practice. While currently writing, registration will block, and vice versa.
 *
 * @author erhannis
 *
 * @param <T>
 */
public class SynchronousSplitter<T> implements CSProcess, ChannelOutput<T> {

    final ArrayList<ChannelOutput<T>> outs;
    final ArrayList<AltingChannelInput<T>> ins;
    final ChannelOutput<T> internalOut;
    final ArrayList<ProcessWrite> writers = new ArrayList<ProcessWrite>();
    final Parallel writersProcess = new Parallel();
    final Integer immunity;
    Integer poisonStrength = null;

    public SynchronousSplitter() {
        this.immunity = null;
        this.outs = new ArrayList<ChannelOutput<T>>();
        this.ins = new ArrayList<AltingChannelInput<T>>();
        Any2OneChannel<T> internalChannel = Channel.<T>any2one();
        this.ins.add(internalChannel.in());
        this.internalOut = internalChannel.out();
    }

    public SynchronousSplitter(int immunity) {
        this.immunity = immunity;
        this.outs = new ArrayList<ChannelOutput<T>>();
        this.ins = new ArrayList<AltingChannelInput<T>>();
        Any2OneChannel<T> internalChannel = Channel.<T>any2one(immunity);
        this.ins.add(internalChannel.in());
        this.internalOut = internalChannel.out();
    }

    public SynchronousSplitter(AltingChannelInput<T> in) {
        this();
        this.ins.add(in);
    }

    public SynchronousSplitter(int immunity, AltingChannelInput<T> in) {
        this(immunity);
        this.ins.add(in);
    }

    public AltingChannelInput<T> register() {
        //TODO Simply throw exception if already poisoned?
        One2OneChannel<T> newChannel = (immunity != null ? Channel.<T>one2one(immunity) : Channel.<T>one2one());
        AltingChannelInput<T> newIn = newChannel.in();
        ChannelOutput<T> newOut = newChannel.out();
        synchronized (writers) {
            ProcessWrite writer = new ProcessWrite(newOut);
            outs.add(newOut);
            writers.add(writer);
            writersProcess.addProcess(writer);
            
            if (poisonStrength != null) {
                newOut.poison(poisonStrength);
            }
        }
        return newIn;
    }

    public void register(ChannelOutput<T> out) {
        //TODO Simply throw exception if already poisoned?
        synchronized (writers) {
            ProcessWrite writer = new ProcessWrite(out);
            outs.add(out);
            writers.add(writer);
            writersProcess.addProcess(writer);
            
            if (poisonStrength != null) {
                out.poison(poisonStrength);
            }
        }
    }

    public void write(T val) {
        this.internalOut.write(val);
    }

    public void run() {
        try {
            Guard[] guards = new Guard[ins.size()];
            for (int i = 0; i < guards.length; i++) {
                guards[i] = ins.get(i);
            }
            Alternative alt = new Alternative(guards);
            while (true) {
                AltingChannelInput<T> in = ins.get(alt.priSelect());
                T value = in.startRead();
                synchronized (writers) {
                    for (ProcessWrite writer : writers) {
                        writer.value = value;
                    }
                    writersProcess.run();
                }
                in.endRead();
            }
        } catch (PoisonException e) {
            this.poison(e.getStrength());
            throw e;
        }
    }

    public void poison(int strength) {
        synchronized (writers) {
            if (strength > 0) {
                this.poisonStrength = strength;
            }
            for (ChannelOutput<T> out : outs) {
                try {
                    out.poison(strength);
                } catch (Exception e) {
                }
            }
        }
        for (ChannelInput<T> in : ins) {
            try {
                in.poison(strength);
            } catch (Exception e) {
            }
        }
    }
}
