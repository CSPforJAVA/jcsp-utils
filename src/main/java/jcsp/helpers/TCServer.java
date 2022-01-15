package jcsp.helpers;

import jcsp.lang.ChannelOutput;

/**
 * 
 * @author erhannis
 *
 * @param <T>
 * @param <U>
 * 
 * @see jcsp.helpers.FunctionChannel
 */
public interface TCServer<T, U>
{
   public static class TaskItem<T, U>
   {
       public final T val;
       public final ChannelOutput<U> responseOut;

       public TaskItem(T val, ChannelOutput<U> responseOut)
       {
          this.val = val;
          this.responseOut = responseOut;
       }
   }
   
   public TaskItem<T, U> read();
}
