package jcsp.utils;

/**
 * 
 * @author erhannis
 *
 * @param <T>
 * @param <U>
 * 
 * @see jcsp.utils.FunctionChannel
 */
public interface FCServer<T, U>
{
   public T startRead();

   public void endRead(U u);
}
