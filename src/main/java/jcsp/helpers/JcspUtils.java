package jcsp.helpers;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jcsp.lang.Alternative;
import jcsp.lang.AltingAntidoteChannelInput;
import jcsp.lang.AltingAntidoteChannelInputInt;
import jcsp.lang.AltingChannelInput;
import jcsp.lang.AltingChannelInputInt;
import jcsp.lang.AntidoteChannelInput;
import jcsp.lang.AntidoteChannelInputInt;
import jcsp.lang.AntidoteChannelOutput;
import jcsp.lang.AntidoteChannelOutputInt;
import jcsp.lang.Any2AnyChannel;
import jcsp.lang.Any2OneChannel;
import jcsp.lang.CSProcess;
import jcsp.lang.Channel;
import jcsp.lang.ChannelInput;
import jcsp.lang.ChannelInputInt;
import jcsp.lang.ChannelOutput;
import jcsp.lang.ChannelOutputInt;
import jcsp.lang.DisableableTimer;
import jcsp.lang.Guard;
import jcsp.lang.PoisonException;
import jcsp.lang.ProcessManager;
import jcsp.util.ChannelDataStore;

/**
 * Utility methods for use in JCSP.
 *
 * @author erhannis
 *
 */
public class JcspUtils {

        public static class DeadlockLoggingChannelOutput<T> implements ChannelOutput<T> {

            private final ChannelOutput<T> out;

            public DeadlockLoggingChannelOutput(ChannelOutput<T> out) {
                this.out = out;
            }

            public void poison(int strength) {
                out.poison(strength);
            }

            public void write(T object) {
                write(object, null);
            }
            
            public void write(T object, String tag) {
                DeadlockLogger.DeadlockTag tag0;
                try {
                    // TODO This may be kinda heavy.
                    if (tag != null) {
                        throw new RuntimeException("Deadlock detected! " + tag);
                    } else {
                        throw new RuntimeException("Deadlock detected!");
                    }
                } catch (Exception e) {
                    tag0 = new DeadlockLogger.DeadlockTag(e);
                }
                DeadlockLogger.startOut.write(tag0);
                try {
                    out.write(object);
                } finally {
                    DeadlockLogger.stopOut.write(tag0);
                }
            }

        }

        public static class DeadlockLoggingChannelOutputInt implements ChannelOutputInt {

            private final ChannelOutputInt out;

            public DeadlockLoggingChannelOutputInt(ChannelOutputInt out) {
                this.out = out;
            }

            public void poison(int strength) {
                out.poison(strength);
            }

            public void write(int value) {
                write(value, null);
            }
            
            public void write(int value, String tag) {
                DeadlockLogger.DeadlockTag tag0;
                try {
                    // TODO This may be kinda heavy.
                    if (tag != null) {
                        throw new RuntimeException("Deadlock detected! " + tag);
                    } else {
                        throw new RuntimeException("Deadlock detected!");
                    }
                } catch (Exception e) {
                    tag0 = new DeadlockLogger.DeadlockTag(e);
                }
                DeadlockLogger.startOut.write(tag0);
                try {
                    out.write(value);
                } finally {
                    DeadlockLogger.stopOut.write(tag0);
                }
            }

        }    
    /**
     * Yeah, this is really weirdly nested. I know. Also, it's kindof a
     * singleton, but it's to be used specifically for debugging, so I'm less
     * inclined to be worried.
     *
     * @author erhannis
     *
     */
    private static class DeadlockLogger implements CSProcess {

        private static final ChannelOutput<DeadlockTag> startOut;
        private static final ChannelOutput<DeadlockTag> stopOut;

        static {
            Any2OneChannel<DeadlockTag> startChannel = Channel.<DeadlockTag>any2one();
            AltingChannelInput<DeadlockTag> startIn = startChannel.in();
            startOut = startChannel.out();

            Any2OneChannel<DeadlockTag> stopChannel = Channel.<DeadlockTag>any2one();
            AltingChannelInput<DeadlockTag> stopIn = stopChannel.in();
            stopOut = stopChannel.out();

            new ProcessManager(new DeadlockLogger(startIn, stopIn)).start();
        }

        private static class DeadlockTag {

            public final Exception exception;

            public DeadlockTag(Exception exception) {
                this.exception = exception;
            }
        }

        private static final int DEADLOCK_TIMEOUT = 10000;

        private final AltingChannelInput<DeadlockTag> startIn;
        private final AltingChannelInput<DeadlockTag> stopIn;

        private final HashMap<DeadlockTag, Long> times = new HashMap<DeadlockTag, Long>();

        private DeadlockLogger(AltingChannelInput<DeadlockTag> startIn, AltingChannelInput<DeadlockTag> stopIn) {
            this.startIn = startIn;
            this.stopIn = stopIn;
        }

        public void run() {
            final DisableableTimer deadlockTimer = new DisableableTimer();

            HashMap<DeadlockTag, Long> loggedDeadlocks = new HashMap<DeadlockTag, Long>();

            Alternative alt = new Alternative(new Guard[]{startIn, stopIn, deadlockTimer});

            while (true) {
                switch (alt.priSelect()) {
                    case 0: // Start a timer
                    {
                        DeadlockTag tag = startIn.read();
                        times.put(tag, deadlockTimer.read() + DEADLOCK_TIMEOUT);
                        break;
                    }
                    case 1: // Stop a timer
                    {
                        DeadlockTag tag = stopIn.read();
                        if (loggedDeadlocks.containsKey(tag)) {
                            // Oops, wasn't a deadlock afterall
                            System.err.println("Nevermind; the following wasn't actually a deadlock.  Stopped for " + (deadlockTimer.read() - loggedDeadlocks.remove(tag)) + "ms");
                            tag.exception.printStackTrace();
                        }
                        times.remove(tag);
                        break;
                    }
                    case 2: // Deadlock
                        // Deadlock handling actually occurs in timer update
                        break;
                }

                // Update timer
                Iterator<Entry<DeadlockTag, Long>> it = times.entrySet().iterator();
                Long min = null;
                long now = deadlockTimer.read();
                while (it.hasNext()) {
                    Entry<DeadlockTag, Long> e = it.next();
                    if (e.getValue() < now) {
                        // Deadlock has occurred
                        e.getKey().exception.printStackTrace();
                        loggedDeadlocks.put(e.getKey(), e.getValue() - DEADLOCK_TIMEOUT);
                        it.remove();
                    } else {
                        if (min == null || e.getValue() < min) {
                            min = e.getValue();
                        }
                    }
                }
                if (min == null) {
                    deadlockTimer.turnOff();
                } else {
                    deadlockTimer.setAlarm(min + 50); // Plus anti-earlywakeup
                }
            } // Loop
        }
    }

    /**
     * Reads and discards all pending data in the channel.
     *
     * @param in
     */
    public static void clear(AltingChannelInputInt in) {
        while (in.pending()) {
            in.read();
        }
    }

    /**
     * Reads and discards all pending data in the channel.
     *
     * @param in
     */
    public static void clear(AltingChannelInput in) {
        while (in.pending()) {
            in.read();
        }
    }

    /**
     * This wraps the channel in a layer that monitors for deadlock.<br/>
     * 1. If a call to `.write(T)` on the underlying channel blocks for more
     * than 10 seconds, a watchdog process (a static singleton, shared across
     * all calls to `logDeadlock`) prints "Deadlock detected!" and the stack
     * trace of the call that blocked.<br/>
     * 2. If a call presumed deadlocked subsequently completes, the following is
     * logged: "Nevermind; the following wasn't actually a deadlock. Stopped for
     * {time}ms".<br/>
     * 3. Note: wrapping a channel like this is somewhat expensive. Every
     * message sent causes an exception and stack trace to be preemptively
     * created, and two extra channel calls made to the single coordinating
     * process. However - in practice it's useful enough, and fast enough, that
     * I've left it all over production code with no apparent problem.<br/>
     *
     * @param <T>
     * @param out
     * @return wrapped ChannelOutput
     */
    public static <T> DeadlockLoggingChannelOutput<T> logDeadlock(final ChannelOutput<T> out) {
        return new DeadlockLoggingChannelOutput<T>(out);
    }

    /**
     * See {@link #logDeadlock(jcsp.lang.ChannelOutput)}
     *
     * @param out
     * @return
     */
    public static DeadlockLoggingChannelOutputInt logDeadlock(final ChannelOutputInt out) {
        return new DeadlockLoggingChannelOutputInt(out);
    }

    public static void logDeadlock(Runnable r) {
        DeadlockLogger.DeadlockTag tag;
        try {
            // TODO This may be kinda heavy.
            throw new RuntimeException("Deadlock detected!");
        } catch (Exception e) {
            tag = new DeadlockLogger.DeadlockTag(e);
        }
        DeadlockLogger.startOut.write(tag);
        try {
            r.run();
        } finally {
            DeadlockLogger.stopOut.write(tag);
        }
    }

    public static <T> T logDeadlock(Supplier<T> r) {
        DeadlockLogger.DeadlockTag tag;
        try {
            // TODO This may be kinda heavy.
            throw new RuntimeException("Deadlock detected!");
        } catch (Exception e) {
            tag = new DeadlockLogger.DeadlockTag(e);
        }
        DeadlockLogger.startOut.write(tag);
        try {
            return r.get();
        } finally {
            DeadlockLogger.stopOut.write(tag);
        }
    }

    /**
     * Wraps `out` in a channel that swallows PoisonExceptions.
     *
     * @param <T>
     * @param out
     * @return
     */
    public static <T> ChannelOutput<T> antidote(final ChannelOutput<T> out) {
        return new AntidoteChannelOutput<T>(out);
    }

    /**
     * See {@link #antidote(jcsp.lang.ChannelOutput)}
     *
     * @param out
     * @return
     */
    public static ChannelOutputInt antidote(final ChannelOutputInt out) {
        return new AntidoteChannelOutputInt(out);
    }

    //TODO AltingChannelOutput?
    /**
     * Wraps an AltingChannelInput, and if any method throws a PoisonException,
     * it basically pretends nothing happened. The only difference is that if
     * `read` or `startRead` are called, they return null. //TODO Should they
     * block, instead?
     *
     * @param <T>
     * @param in
     * @return
     */
    public static <T> AltingChannelInput<T> antidote(final AltingChannelInput<T> in) {
        return new AltingAntidoteChannelInput<T>(in);
    }

    /**
     * Wraps an AltingChannelInputInt, and if any method throws a
     * PoisonException, it basically pretends nothing happened. The only
     * difference is that if `read` or `startRead` are called, they return 0.
     * //TODO Should they block, instead?
     *
     * @param in
     * @return
     */
    public static AltingChannelInputInt antidote(final AltingChannelInputInt in) {
        return new AltingAntidoteChannelInputInt(in);
    }

    /**
     * See {@link #antidote(jcsp.lang.AltingChannelInput)}
     *
     * @param in
     * @return
     */
    public static <T> ChannelInput<T> antidote(final ChannelInput<T> in) {
        return new AntidoteChannelInput<T>(in);
    }

    /**
     * See {@link #antidote(jcsp.lang.AltingChannelInputInt)}
     *
     * @param in
     * @return
     */
    public static ChannelInputInt antidote(final ChannelInputInt in) {
        return new AntidoteChannelInputInt(in);
    }

    /**
     * Spawns a task with one channel out. Like so:<br/>
     * <br/>
     * <pre>
     *  AltingChannelInput&lt;String&gt; in = JcspUtils.spawn(out -&gt; {
     *      out.write("line 1");
     *      out.write("line 2");
     *      out.write("line 3");
     *  });
     * </pre>
     *
     * //TODO Should this be in a particular class?
     *
     * @param task
     * @return AltingChannelInput receiving the data the spawned task is
     * emitting.
     */
    public static <T> AltingChannelInput<T> spawn(final Consumer<ChannelOutput<T>> task) {
        // TODO Could buffer, so the thread can die after writing. I don't think
        // that should be default, though.
        Any2OneChannel<T> resultChannel = Channel.<T>any2one();
        AltingChannelInput<T> resultIn = resultChannel.in();
        final ChannelOutput<T> resultOut = resultChannel.out();
        new ProcessManager(new CSProcess() {
            public void run() {
                task.accept(resultOut);
            }
        }).start();
        return resultIn;
    }

    private static class VentProcess implements CSProcess {

        public static final ChannelOutput<AltingChannelInput> ventOut;

        static {
            Any2OneChannel<AltingChannelInput> ventChannel = Channel.<AltingChannelInput>any2one();
            AltingChannelInput<AltingChannelInput> ventIn = ventChannel.in();
            ventOut = ventChannel.out();

            new ProcessManager(new VentProcess(ventIn)).start();
        }

        private final AltingChannelInput<AltingChannelInput> ventIn;

        private VentProcess(AltingChannelInput<AltingChannelInput> ventIn) {
            this.ventIn = ventIn;
        }

        public void run() {
            ArrayList<AltingChannelInput> vents = new ArrayList<AltingChannelInput>();

            Alternative alt = new Alternative(new Guard[]{ventIn});
            while (true) {
                try {
                    int choice = alt.fairSelect();
                    if (choice == 0) {
                        // New vent
                        vents.add(ventIn.read());
                        // TODO This could probably be more efficient.
                        Guard[] guards = new Guard[vents.size() + 1];
                        guards[0] = ventIn;
                        for (int i = 0; i < vents.size(); i++) {
                            guards[i + 1] = vents.get(i);
                        }
                        alt = new Alternative(guards);
                    } else {
                        choice--;
                        vents.get(choice).read();
                    }
                } catch (Exception e) {
                    System.err.println("Something unexpected went wrong in JcspUtils.VentProcess!");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * If you have to shut down a process, and no longer care about the data
     * it's receiving, but don't want to block the things sending it, you can
     * vent the channel here. Passing a channel here adds it to a list of
     * processes to be monitored and automatically read, discarding the results.
     *
     * @param in
     */
    public static void vent(AltingChannelInput in) {
        VentProcess.ventOut.write(in);
    }

    /**
     * Provides a buffered Output for a given Output. WARNING: Does this by
     * starting a new process to handle the exchange. Beware the associated
     * thread clutter.
     *
     * @param co
     * @return
     */
    public static <T> ChannelOutput<T> buffer(final ChannelOutput<T> target, ChannelDataStore<T> buffer) {
        Any2AnyChannel<T> c = Channel.<T>any2any(buffer);
        final ChannelInput<T> ci = c.in();
        ChannelOutput<T> co = c.out();
        new ProcessManager(new CSProcess() {
            public void run() {
                while (true) {
                    //TODO Deadlock log?
                    target.write(ci.read());
                }
            }
        }).start();
        return co;
    }
}
