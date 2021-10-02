/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcsp.lang;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A variant of CallChannel that doesn't take such a whopping load of boilerplate.
 * 
 * @author erhannis
 */
public class JoinFork<SERVER> extends Guard {
    private final AltingChannelInputInt ai;
    private final ChannelOutputInt ao;
    {
        Any2OneChannelInt a = Channel.any2oneInt();
        ai = a.in();
        ao = a.out();
    }
    private final AltingChannelInputInt bi;
    private final ChannelOutputInt bo;
    {
        Any2OneChannelInt b = Channel.any2oneInt();
        bi = b.in();
        bo = b.out();
    }
    private SERVER server;

    public JoinFork() {
        
    }
    
    // ...You know, I think this was only ever built to deal with one server, like, N-1.
    //   I don't know why it has a signature like it works with multiple servers.
    //TODO I think you could swap a's send/receive and send the SERVER, and it should work.
    //   I'm really leery of messing with it right now, though.
    //   ...Maybe with good reason - swapping send/receive would swap N-1 with 1-N
    public void accept(SERVER server) {
        this.server = server;
        ai.read();
        bi.read();
    }
    
    public SERVER join() {
        ao.write(0);
        return server;
    }
    
    public void fork() {
        bo.write(0);
    }
    
    public <T> T join(Function<SERVER,T> closure) {
        SERVER server = join();
        T t = closure.apply(server);
        fork();
        return t;
    }

    public <T> T join(Supplier<T> closure) {
        join();
        T t = closure.get();
        fork();
        return t;
    }

    @Override
    boolean enable(Alternative alt) {
        return ai.enable(alt);
    }

    @Override
    boolean disable() {
        return ai.disable();
    }
}
