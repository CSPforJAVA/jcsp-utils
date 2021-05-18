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
public class AntidoteChannelOutput<T> implements ChannelOutput<T> {

    private final ChannelOutput<T> out;

    public AntidoteChannelOutput(ChannelOutput<T> out) {
        this.out = out;
    }

    public void poison(int strength) {
        out.poison(strength);
    }

    public void write(T object) {
        try {
            out.write(object);
        } catch (PoisonException e) {
            //TODO Log exception?
        }
    }
}
