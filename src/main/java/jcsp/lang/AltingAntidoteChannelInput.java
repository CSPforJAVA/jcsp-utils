/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcsp.lang;

/**
 * Wraps an AltingChannelInput, and if any method throws a PoisonException, it
 * basically pretends nothing happened.  The only difference is that if `read`
 * or `startRead` are called, they return null. //TODO Should they block, instead?
 * 
 * @author erhannis
 */
public class AltingAntidoteChannelInput<T> extends AltingChannelInput<T> {
    private final AltingChannelInput<T> in;

    public AltingAntidoteChannelInput(AltingChannelInput<T> in) {
        this.in = in;
    }

    public void poison(int strength) {
        in.poison(strength);
    }

    public T read() {
        try {
            return in.read();
        } catch (PoisonException e) {
            //TODO Log exception?
            return null;
        }
    }

    @Override
    public boolean pending() {
        try {
            return in.pending();
        } catch (PoisonException e) {
            return false;
        }
    }

    @Override
    boolean enable(Alternative alt) {
        try {
            return in.enable(alt);
        } catch (PoisonException e) {
            return false;
        }
    }

    @Override
    boolean disable() {
        try {
            return in.disable();
        } catch (PoisonException e) {
            return false;
        }
    }

    public T startRead() {
        try {
            return in.startRead();
        } catch (PoisonException e) {
            return null;
        }
    }

    public void endRead() {
        try {
            in.endRead();
        } catch (PoisonException e) {
        }
    }
}
