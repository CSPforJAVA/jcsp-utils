/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcsp.lang;

/**
 *
 * @author erhannis
 */
public class AntidoteChannelInput<T> implements ChannelInput<T> {

    private final ChannelInput<T> in;

    public AntidoteChannelInput(ChannelInput<T> in) {
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

    public T startRead() {
        try {
            return in.startRead();
        } catch (PoisonException e) {
            //TODO Log exception?
            return null;
        }
    }

    public void endRead() {
        try {
            in.endRead();
        } catch (PoisonException e) {
            //TODO Log exception?
        }
    }
}
