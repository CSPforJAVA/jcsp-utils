package jcsp.lang;

import java.util.concurrent.ConcurrentHashMap;
import jcsp.helpers.FCClient;
import jcsp.helpers.FCServer;
import jcsp.helpers.JcspUtils;
import jcsp.helpers.TCServer;
import jcsp.lang.Alternative;
import jcsp.lang.AltingChannelInput;
import jcsp.lang.Any2AnyChannel;
import jcsp.lang.Any2OneChannel;
import jcsp.lang.Channel;
import jcsp.lang.Guard;
import jcsp.lang.SharedChannelInput;
import jcsp.lang.SharedChannelOutput;

/**
 * Kindof like a call channel, but embodies a single method, and is much simpler
 * to use.  More specifically, this is like AltingFunctionChannel, but the server
 * gets a value and a response channel, so that it can work on multiple tasks
 * and later return the result to the appropriate client.
 *
 * @author erhannis
 *
 * @param <T>
 * @param <U>
 * @see jcsp.lang.AltingFunctionChannel
 */
public class AltingTaskChannel<T, U> extends AltingTCServer<T, U> implements FCClient<T, U> {

    private final AltingChannelInput<TCServer.TaskItem<T, U>> requestChannelIn;
    private final ChannelOutput<TCServer.TaskItem<T, U>> requestChannelOut;
    
    private final Integer immunity;

    public AltingTaskChannel() {
        this(false);
    }

    public AltingTaskChannel(int immunity) {
        this(false, immunity);
    }

    public AltingTaskChannel(boolean logDeadlock) {
        immunity = null;
        Any2OneChannel<TCServer.TaskItem<T, U>> requestChannel = Channel.any2one();
        requestChannelIn = requestChannel.in();
        if (logDeadlock) {
            requestChannelOut = JcspUtils.logDeadlock(requestChannel.out());
        } else {
            requestChannelOut = requestChannel.out();
        }
    }

    public AltingTaskChannel(boolean logDeadlock, int immunity) {
        this.immunity = immunity;
        Any2OneChannel<TCServer.TaskItem<T, U>> requestChannel = Channel.any2one(immunity);
        requestChannelIn = requestChannel.in();
        if (logDeadlock) {
            requestChannelOut = JcspUtils.logDeadlock(requestChannel.out());
        } else {
            requestChannelOut = requestChannel.out();
        }
    }

    
    private ConcurrentHashMap<AltingChannelInput<U>, String> activeTasks = new ConcurrentHashMap<AltingChannelInput<U>, String>();
    
    public U call(T t) {
        Any2OneChannel<U> taskChannel;
        if (immunity != null) {
            taskChannel = Channel.any2one(immunity);
        } else {
            taskChannel = Channel.any2one();
        }
        
        requestChannelOut.write(new TCServer.TaskItem<T, U>(t, JcspUtils.logDeadlock(taskChannel.out())));
        AltingChannelInput<U> taskIn = taskChannel.in();
        activeTasks.put(taskIn, "");
        U u;
        try {
            u = taskIn.read();
        } finally {
            activeTasks.remove(taskIn);
        }
        return u;
    }

    public TaskItem<T, U> read() {
        return requestChannelIn.read();
    }

    public AltingTCServer<T, U> getServer() {
        return this;
    }

    public FCClient<T, U> getClient() {
        return this;
    }

    //TODO The semantics of this might not be QUITE right, in that this could be called from the Client or the Server, and behave the same rather than differently.  Not sure.
    public void poison(int strength) {
        requestChannelIn.poison(strength);
        requestChannelOut.poison(strength);
        for (AltingChannelInput<U> task : activeTasks.keySet()) {
            task.poison(strength);
        }
    }

    @Override
    boolean enable(Alternative alt) {
        return requestChannelIn.enable(alt);
    }

    @Override
    boolean disable() {
        return requestChannelIn.disable();
    }
}
