package jcsp.utils;

import java.util.ArrayList;

import jcsp.lang.AltingChannelInput;
import jcsp.lang.CSProcess;
import jcsp.lang.Channel;
import jcsp.lang.ChannelInput;
import jcsp.lang.ChannelOutput;
import jcsp.lang.One2OneChannel;
import jcsp.lang.Parallel;
import jcsp.plugNplay.ProcessWrite;

/**
 * Writes from `in` to all registered out channels. Outputs are written in
 * parallel. `Synchronous` in that the `in` read does not complete until all
 * outs have completed writing, giving an illusion that `in` is a non-buffered
 * channel writing to all of a fixed set of outputs.
 * 
 * Should be safe to register a new channel while running, though possibly bad
 * practice. While currently writing, registration will block, and vice versa.
 * 
 * @author erhannis
 *
 * @param <T>
 */
public class SynchronousSplitter<T> implements CSProcess
{
   private final ChannelInput<T> in;
   private final ArrayList<ProcessWrite> writers = new ArrayList<ProcessWrite>();
   private final Parallel writersProcess = new Parallel();

   public SynchronousSplitter(ChannelInput<T> in)
   {
      this.in = in;
   }

   public AltingChannelInput<T> register()
   {
      One2OneChannel<T> newChannel = Channel.<T> one2one();
      AltingChannelInput<T> newIn = newChannel.in();
      ChannelOutput<T> newOut = newChannel.out();
      synchronized (writers)
      {
         ProcessWrite writer = new ProcessWrite(newOut);
         writers.add(writer);
         writersProcess.addProcess(writer);
      }
      return newIn;
   }

   public void run()
   {
      while (true)
      {
         T value = in.startRead();
         synchronized (writers)
         {
            for (ProcessWrite writer : writers)
            {
               writer.value = value;
            }
            writersProcess.run();
         }
         in.endRead();
      }
   }
}
