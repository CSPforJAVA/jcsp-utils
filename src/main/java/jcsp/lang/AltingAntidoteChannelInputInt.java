/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcsp.lang;

/**
 * Wraps an AltingChannelInput, and if any method throws a PoisonException, it
 * basically pretends nothing happened.  The only difference is that if `read`
 * or `startRead` are called, they return 0. //TODO Should they block, instead?
 * 
 * @author erhannis
 */
public class AltingAntidoteChannelInputInt extends AltingChannelInputInt {
    private final AltingChannelInputInt in;

    public AltingAntidoteChannelInputInt(AltingChannelInputInt in) {
        this.in = in;
    }

    public void poison(int strength) {
        in.poison(strength);
    }

    public int read() {
        try {
            return in.read();
        } catch (PoisonException e) {
            //TODO Log exception?
            return 0;
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

    public int startRead() {
        try {
            return in.startRead();
        } catch (PoisonException e) {
            return 0;
        }
    }

    public void endRead() {
        try {
            in.endRead();
        } catch (PoisonException e) {
        }
    }
}
