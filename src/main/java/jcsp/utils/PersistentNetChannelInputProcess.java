package jcsp.utils;

import jcsp.lang.AltingChannelInput;
import jcsp.lang.CSProcess;
import jcsp.lang.CSTimer;
import jcsp.lang.ChannelOutput;
import jcsp.net2.NetChannel;

public class PersistentNetChannelInputProcess implements CSProcess
{
   private final int vcn;
   private final ChannelOutput inputOut;

   public PersistentNetChannelInputProcess(int vcn, ChannelOutput inputOut)
   {
      this.vcn = vcn;
      this.inputOut = inputOut;
   }

   public void run()
   {
      try
      {
         AltingChannelInput inputIn = NetChannel.numberedNet2One(vcn);
         CSTimer timer = new CSTimer();
         while (true)
         {
            inputOut.write(inputIn.read());
         }
      }
      finally
      {
         System.err.println("PersistentNetChannelInputProcess crashing; fix it");
      }
   }
}
