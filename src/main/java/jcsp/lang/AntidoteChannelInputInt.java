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
public class AntidoteChannelInputInt implements ChannelInputInt {

    private final ChannelInputInt in;

    public AntidoteChannelInputInt(ChannelInputInt in) {
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

    public int startRead() {
        try {
            return in.startRead();
        } catch (PoisonException e) {
            //TODO Log exception?
            return 0;
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
