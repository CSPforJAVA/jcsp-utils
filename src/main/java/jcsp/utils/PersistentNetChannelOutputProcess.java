package jcsp.utils;

import java.util.LinkedList;

import jcsp.lang.AltingChannelInput;
import jcsp.lang.CSProcess;
import jcsp.lang.CSTimer;
import jcsp.lang.ChannelOutput;
import jcsp.net2.JCSPNetworkException;
import jcsp.net2.NetChannel;
import jcsp.net2.NodeAddress;

// TODO Could make this a ChannelOutput subclass, if I reaaaallly wanted to
public class PersistentNetChannelOutputProcess implements CSProcess
{
   private final NodeAddress address;
   private final int vcn;
   private final AltingChannelInput outputIn;

   public PersistentNetChannelOutputProcess(NodeAddress address, int vcn, AltingChannelInput outputIn)
   {
      this.address = address;
      this.vcn = vcn;
      this.outputIn = outputIn;
   }

   public void run()
   {
      try
      {
         CSTimer timer = new CSTimer();
         ChannelOutput outputOut = null;
         LinkedList<Object> queue = new LinkedList<Object>();
         while (true)
         {
            // Create channel
            while (outputOut == null)
            {
               System.out.println("PNCOP Creating output channel");
               try
               {
                  outputOut = NetChannel.one2net(address, vcn);
               }
               catch (JCSPNetworkException e)
               {
                  // TODO Suppress?
                  e.printStackTrace();
                  timer.sleep(1000);
               }
            }

            // Write stuff
            while (true)
            {
               try
               {
                  while (!queue.isEmpty())
                  {
                     System.out.println("PNCOP Resending queued object");
                     Object o = queue.getFirst();
                     outputOut.write(o);
                     queue.removeFirst();
                  }
                  queue.add(outputIn.read());
                  outputOut.write(queue.getFirst());
                  queue.removeFirst();
               }
               catch (JCSPNetworkException e)
               {
                  e.printStackTrace();
                  outputOut = null;
                  break;
               }
            }
         }
      }
      finally
      {
         System.err.println("PersistentNetChannelOutputProcess crashing; fix it");
      }
   }
}
