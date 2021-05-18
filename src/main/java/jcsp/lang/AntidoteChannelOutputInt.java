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
public class AntidoteChannelOutputInt implements ChannelOutputInt {

    private final ChannelOutputInt out;

    public AntidoteChannelOutputInt(ChannelOutputInt out) {
        this.out = out;
    }

    public void poison(int strength) {
        out.poison(strength);
    }

    public void write(int value) {
        try {
            out.write(value);
        } catch (PoisonException e) {
            //TODO Log exception?
        }
    }
}
