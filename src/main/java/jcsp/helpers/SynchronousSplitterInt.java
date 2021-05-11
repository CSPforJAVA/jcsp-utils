package jcsp.helpers;

import java.util.ArrayList;

import jcsp.lang.AltingChannelInputInt;
import jcsp.lang.CSProcess;
import jcsp.lang.Channel;
import jcsp.lang.ChannelInputInt;
import jcsp.lang.ChannelOutputInt;
import jcsp.lang.One2OneChannelInt;
import jcsp.lang.Parallel;
import jcsp.plugNplay.ints.ProcessWriteInt;

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
 */
public class SynchronousSplitterInt implements CSProcess
{
   private final ChannelInputInt in;
   private final ArrayList<ProcessWriteInt> writers = new ArrayList<ProcessWriteInt>();
   private final Parallel writersProcess = new Parallel();

   public SynchronousSplitterInt(ChannelInputInt in)
   {
      this.in = in;
   }

   public AltingChannelInputInt register()
   {
      One2OneChannelInt newChannel = Channel.one2oneInt();
      AltingChannelInputInt newIn = newChannel.in();
      ChannelOutputInt newOut = newChannel.out();
      synchronized (writers)
      {
         ProcessWriteInt writer = new ProcessWriteInt(newOut);
         writers.add(writer);
         writersProcess.addProcess(writer);
      }
      return newIn;
   }

   public void run()
   {
      while (true)
      {
         int value = in.startRead();
         synchronized (writers)
         {
            for (ProcessWriteInt writer : writers)
            {
               writer.value = value;
            }
            writersProcess.run();
         }
         in.endRead();
      }
   }
}
