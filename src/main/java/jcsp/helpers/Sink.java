package jcsp.helpers;

import java.util.function.Consumer;

import jcsp.lang.AltingChannelInput;
import jcsp.lang.Any2OneChannel;
import jcsp.lang.CSProcess;
import jcsp.lang.Channel;
import jcsp.lang.ChannelOutput;
import jcsp.lang.ProcessManager;
import jcsp.lang.SharedChannelOutput;

/**
 * Reads a value, calls the callback, repeat.
 * 
 * More convenient than manually creating an internal channel and starting a new
 * process and linking the two.
 * 
 * If `extendedRendezvous` (true by default), doesn't create a separate process - 
 * just calls the callback inline on `write`.  If false, DOES create a separate
 * process - beware thread pileup.
 * 
 * @author erhannis
 *
 * @param <T>
 */
public class Sink<T> implements ChannelOutput<T>
{
   public final Consumer<T> callback;

   public final AltingChannelInput<T> internalIn;
   public final SharedChannelOutput<T> internalOut;
   public final boolean extendedRendezvous;

   public Sink(Consumer<T> callback)
   {
      this(true, callback);
   }

   public Sink(boolean extendedRendezvous, Consumer<T> callback)
   {
      this.callback = callback;
      Any2OneChannel<T> internalChannel = Channel.<T> any2one();
      this.internalIn = internalChannel.in();
      this.internalOut = internalChannel.out();
      this.extendedRendezvous = extendedRendezvous;

      if (!extendedRendezvous) {
         new ProcessManager(new SinkProcess()).start();
      }
   }

   /**
    * Poison ignored. //TODO Should?
    */
   public void poison(int strength)
   {
   }

   public void write(T object)
   {
      if (extendedRendezvous) {
         callback.accept(object);
      } else {
         internalOut.write(object);
      }
   }

   public class SinkProcess implements CSProcess
   {
      public void run()
      {
         while (true)
         {
            T t = internalIn.read();
            callback.accept(t);
         }
      }
   }
}
