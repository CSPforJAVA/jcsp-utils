package jcsp.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.Consumer;

import jcsp.lang.Alternative;
import jcsp.lang.AltingChannelInput;
import jcsp.lang.AltingChannelInputInt;
import jcsp.lang.Any2AnyChannel;
import jcsp.lang.Any2OneChannel;
import jcsp.lang.CSProcess;
import jcsp.lang.Channel;
import jcsp.lang.ChannelInput;
import jcsp.lang.ChannelOutput;
import jcsp.lang.ChannelOutputInt;
import jcsp.lang.DisableableTimer;
import jcsp.lang.Guard;
import jcsp.lang.ProcessManager;
import jcsp.util.ChannelDataStore;

/**
 * Utility methods for use in JCSP.
 * 
 * @author erhannis
 *
 */
public class JcspUtils
{
   /**
    * Yeah, this is really weirdly nested. I know.
    * Also, it's kindof a singleton, but it's to be used specifically for
    * debugging, so I'm less inclined to be worried.
    * 
    * @author erhannis
    *
    */
   private static class DeadlockLogger implements CSProcess
   {
      private static final ChannelOutput<DeadlockTag> startOut;
      private static final ChannelOutput<DeadlockTag> stopOut;
      static
      {
         Any2OneChannel<DeadlockTag> startChannel = Channel.<DeadlockTag> any2one();
         AltingChannelInput<DeadlockTag> startIn = startChannel.in();
         startOut = startChannel.out();

         Any2OneChannel<DeadlockTag> stopChannel = Channel.<DeadlockTag> any2one();
         AltingChannelInput<DeadlockTag> stopIn = stopChannel.in();
         stopOut = stopChannel.out();

         new ProcessManager(new DeadlockLogger(startIn, stopIn)).start();
      }

      private static class DeadlockTag
      {
         public final Exception exception;

         public DeadlockTag(Exception exception)
         {
            this.exception = exception;
         }
      }

      private static class DeadlockLoggingChannelOutput<T> implements ChannelOutput<T>
      {
         private final ChannelOutput<T> out;

         public DeadlockLoggingChannelOutput(ChannelOutput<T> out)
         {
            this.out = out;
         }

         public void poison( int strength )
         {
            out.poison(strength);
         }

         public void write( T object )
         {
            DeadlockTag tag;
            try
            {
               // TODO This may be kinda heavy.
               throw new RuntimeException("Deadlock detected!");
            }
            catch (Exception e)
            {
               tag = new DeadlockTag(e);
            }
            startOut.write(tag);
            out.write(object);
            stopOut.write(tag);
         }

      }

      private static class DeadlockLoggingChannelOutputInt implements ChannelOutputInt
      {
         private final ChannelOutputInt out;

         public DeadlockLoggingChannelOutputInt(ChannelOutputInt out)
         {
            this.out = out;
         }

         public void poison( int strength )
         {
            out.poison(strength);
         }

         public void write( int value )
         {
            DeadlockTag tag;
            try
            {
               // TODO This may be kinda heavy.
               throw new RuntimeException("Deadlock detected!");
            }
            catch (Exception e)
            {
               tag = new DeadlockTag(e);
            }
            startOut.write(tag);
            out.write(value);
            stopOut.write(tag);
         }

      }

      private static final int DEADLOCK_TIMEOUT = 10000;

      private final AltingChannelInput<DeadlockTag> startIn;
      private final AltingChannelInput<DeadlockTag> stopIn;

      private final HashMap<DeadlockTag, Long> times = new HashMap<DeadlockTag, Long>();

      private DeadlockLogger(AltingChannelInput<DeadlockTag> startIn, AltingChannelInput<DeadlockTag> stopIn)
      {
         this.startIn = startIn;
         this.stopIn = stopIn;
      }

      public void run()
      {
         final DisableableTimer deadlockTimer = new DisableableTimer();

         HashMap<DeadlockTag, Long> loggedDeadlocks = new HashMap<DeadlockTag, Long>();

         Alternative alt = new Alternative(new Guard[] { startIn, stopIn, deadlockTimer });

         while (true)
         {
            switch (alt.priSelect())
            {
               case 0: // Start a timer
               {
                  DeadlockTag tag = startIn.read();
                  times.put(tag, deadlockTimer.read() + DEADLOCK_TIMEOUT);
                  break;
               }
               case 1: // Stop a timer
               {
                  DeadlockTag tag = stopIn.read();
                  if (loggedDeadlocks.containsKey(tag))
                  {
                     // Oops, wasn't a deadlock afterall
                     System.err.println("Nevermind; the following wasn't actually a deadlock.  Stopped for " + (deadlockTimer.read() - loggedDeadlocks.remove(tag)) + "ms");
                     tag.exception.printStackTrace();
                  }
                  times.remove(tag);
                  break;
               }
               case 2: // Deadlock
                  // Deadlock handling actually occurs in timer update
                  break;
            }

            // Update timer
            Iterator<Entry<DeadlockTag, Long>> it = times.entrySet().iterator();
            Long min = null;
            long now = deadlockTimer.read();
            while (it.hasNext())
            {
               Entry<DeadlockTag, Long> e = it.next();
               if (e.getValue() < now)
               {
                  // Deadlock has occurred
                  e.getKey().exception.printStackTrace();
                  loggedDeadlocks.put(e.getKey(), e.getValue() - DEADLOCK_TIMEOUT);
                  it.remove();
               }
               else
               {
                  if (min == null || e.getValue() < min)
                  {
                     min = e.getValue();
                  }
               }
            }
            if (min == null)
            {
               deadlockTimer.turnOff();
            }
            else
            {
               deadlockTimer.setAlarm(min + 50); // Plus anti-earlywakeup
            }
         } // Loop
      }
   }

   /**
    * Reads and discards all pending data in the channel.
    * 
    * @param in
    */
   public static void clear( AltingChannelInputInt in )
   {
      while (in.pending())
      {
         in.read();
      }
   }

   /**
    * Reads and discards all pending data in the channel.
    * 
    * @param in
    */
   public static void clear( AltingChannelInput in )
   {
      while (in.pending())
      {
         in.read();
      }
   }

   public static <T> ChannelOutput<T> logDeadlock( final ChannelOutput<T> out )
   {
      return new DeadlockLogger.DeadlockLoggingChannelOutput<T>(out);
   }

   public static ChannelOutputInt logDeadlock( final ChannelOutputInt out )
   {
      return new DeadlockLogger.DeadlockLoggingChannelOutputInt(out);
   }

   /**
    * Spawns a task with one channel out.
    *
    * //TODO Should this be in a particular class?
    * 
    * @param task
    * @return
    */
   public static <T> AltingChannelInput<T> spawn( final Consumer<ChannelOutput<T>> task )
   {
      // TODO Could buffer, so the thread can die after writing. I don't think
      // that should be default, though.
      Any2OneChannel<T> resultChannel = Channel.<T> any2one();
      AltingChannelInput<T> resultIn = resultChannel.in();
      final ChannelOutput<T> resultOut = resultChannel.out();
      new ProcessManager(new CSProcess()
      {
         public void run()
         {
            task.accept(resultOut);
         }
      }).start();
      return resultIn;
   }

   private static class VentProcess implements CSProcess
   {
      public static final ChannelOutput<AltingChannelInput> ventOut;
      static
      {
         Any2OneChannel<AltingChannelInput> ventChannel = Channel.<AltingChannelInput> any2one();
         AltingChannelInput<AltingChannelInput> ventIn = ventChannel.in();
         ventOut = ventChannel.out();

         new ProcessManager(new VentProcess(ventIn)).start();
      }

      private final AltingChannelInput<AltingChannelInput> ventIn;

      private VentProcess(AltingChannelInput<AltingChannelInput> ventIn)
      {
         this.ventIn = ventIn;
      }

      public void run()
      {
         ArrayList<AltingChannelInput> vents = new ArrayList<AltingChannelInput>();

         Alternative alt = new Alternative(new Guard[] { ventIn });
         while (true)
         {
            try
            {
               int choice = alt.fairSelect();
               if (choice == 0)
               {
                  // New vent
                  vents.add(ventIn.read());
                  // TODO This could probably be more efficient.
                  Guard[] guards = new Guard[vents.size() + 1];
                  guards[0] = ventIn;
                  for (int i = 0; i < vents.size(); i++)
                  {
                     guards[i + 1] = vents.get(i);
                  }
                  alt = new Alternative(guards);
               }
               else
               {
                  choice--;
                  vents.get(choice).read();
               }
            }
            catch (Exception e)
            {
               System.err.println("Something unexpected went wrong in JcspUtils.VentProcess!");
               e.printStackTrace();
            }
         }
      }
   }

   /**
    * If you have to shut down a process, and no longer care about the data it's
    * receiving, but don't want to block the things sending it, you can vent the
    * channel here. Passing a channel here adds it to a list of processes to be
    * monitored and automatically read, discarding the results.
    * 
    * @param in
    */
   public static void vent( AltingChannelInput in )
   {
      VentProcess.ventOut.write(in);
   }
   
   /**
    * Provides a buffered Output for a given Output.  WARNING: Does this by starting a new process to handle the exchange.  Beware the associated thread clutter.
    * @param co
    * @return
    */
   public static <T> ChannelOutput<T> buffer( final ChannelOutput<T> target, ChannelDataStore<T> buffer ) {
      Any2AnyChannel<T> c = Channel.<T>any2any(buffer);
      final ChannelInput<T> ci = c.in();
      ChannelOutput<T> co = c.out();
      new ProcessManager(new CSProcess()
      {
         public void run()
         {
            while (true)
            {
               //TODO Deadlock log?
               target.write(ci.read());
            }
         }
      }).start();
      return co;
   }
}
