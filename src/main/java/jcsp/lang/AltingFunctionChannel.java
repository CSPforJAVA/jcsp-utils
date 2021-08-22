package jcsp.lang;

import jcsp.helpers.FCClient;
import jcsp.helpers.FCServer;
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
 * to use. Like so:<br/>
 * <br/>
 * FunctionChannel<String, String> fc = new FunctionChannel<>();<br/>
 * Process A:<br/>
 * String result = fc.call();<br/>
 * <br/>
 * Process B:<br/>
 * String request = fc.startRead();<br/>
 * String result = complicatedProcessing(request);<br/>
 * fc.endRead(result);<br/>
 * <br/>
 * Uses two Any2OneChannel, internally.
 * 
 * @author erhannis
 *
 * @param <T>
 * @param <U>
 */
public class AltingFunctionChannel<T, U> extends AltingFCServer<T, U> implements FCClient<T, U>
{
   private final AltingChannelInput<T> requestChannelIn;
   private final SharedChannelOutput<T> requestChannelOut;
   private final SharedChannelInput<U> responseChannelIn;
   private final SharedChannelOutput<U> responseChannelOut;

   public AltingFunctionChannel()
   {
      Any2OneChannel<T> requestChannel = Channel.any2one();
      Any2AnyChannel<U> responseChannel = Channel.any2any();
      requestChannelIn = requestChannel.in();
      requestChannelOut = requestChannel.out();
      responseChannelIn = responseChannel.in();
      responseChannelOut = responseChannel.out();
   }

   public AltingFunctionChannel(int immunity)
   {
      Any2OneChannel<T> requestChannel = Channel.any2one(immunity);
      Any2AnyChannel<U> responseChannel = Channel.any2any(immunity);
      requestChannelIn = requestChannel.in();
      requestChannelOut = requestChannel.out();
      responseChannelIn = responseChannel.in();
      responseChannelOut = responseChannel.out();
   }

   public U call(T t)
   {
      requestChannelOut.write(t);
      return responseChannelIn.read();
   }

   public T startRead()
   {
      return requestChannelIn.read();
   }

   public void endRead(U u)
   {
      responseChannelOut.write(u);
   }

   public AltingFCServer<T, U> getServer()
   {
      return this;
   }

   public FCClient<T, U> getClient()
   {
      return this;
   }
   
   public void poison(int strength)
   {
      requestChannelIn.poison(strength);
      requestChannelOut.poison(strength);
      responseChannelIn.poison(strength);
      responseChannelOut.poison(strength);
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
