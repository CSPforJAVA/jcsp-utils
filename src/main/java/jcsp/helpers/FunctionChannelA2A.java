package jcsp.helpers;

import jcsp.lang.Any2AnyChannel;
import jcsp.lang.Channel;
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
 * Uses two Any2AnyChannel, internally. I haven't thought through what would
 * happen if many processes used it at once - it might even be fine.
 * 
 * @author erhannis
 *
 * @param <T>
 * @param <U>
 */
public class FunctionChannelA2A<T, U> implements FCServer<T, U>, FCClient<T, U>
{
   private final SharedChannelInput<T> requestChannelIn;
   private final SharedChannelOutput<T> requestChannelOut;
   private final SharedChannelInput<U> responseChannelIn;
   private final SharedChannelOutput<U> responseChannelOut;

   public FunctionChannelA2A()
   {
      Any2AnyChannel<T> requestChannel = Channel.any2any();
      Any2AnyChannel<U> responseChannel = Channel.any2any();
      requestChannelIn = requestChannel.in();
      requestChannelOut = requestChannel.out();
      responseChannelIn = responseChannel.in();
      responseChannelOut = responseChannel.out();
   }

   public FunctionChannelA2A(int immunity)
   {
      Any2AnyChannel<T> requestChannel = Channel.any2any(immunity);
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

   public FCServer<T, U> getServer()
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
}
