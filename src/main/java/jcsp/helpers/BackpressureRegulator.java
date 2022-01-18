/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jcsp.helpers;

import jcsp.lang.AltingChannelInput;
import jcsp.lang.Any2OneChannel;
import jcsp.lang.Channel;
import jcsp.lang.ChannelOutput;

/**
 *
 * @author erhannis
 */
public class BackpressureRegulator<T> implements ChannelOutput<T> {
    public final BackpressureBuffer<T> buffer;
    public final AltingChannelInput<T> in;
    private final ChannelOutput<T> out;
    public final int softmax;
    
    public BackpressureRegulator() {
        this(1000);
    }
    
    public BackpressureRegulator(int softmax) {
        this.softmax = softmax;
        this.buffer = new BackpressureBuffer<T>();
        Any2OneChannel<T> channel = Channel.<T> any2one(buffer);
        this.in = channel.in();
        this.out = JcspUtils.logDeadlock(channel.out());
    }

    public void write(T t) {
        out.write(t);
    }

    public void poison(int i) {
        out.poison(i);
    }
    
    /**
     * Says whether you should write to the channel.
     * (It's just `buffer.getCount() &lt; softmax`.)
     * 
     * @return whether you should write to the channel now
     */
    public boolean shouldWrite() {
        return buffer.getCount() < softmax;
    }
}
