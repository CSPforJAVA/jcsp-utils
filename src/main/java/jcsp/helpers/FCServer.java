package jcsp.helpers;

/**
 * 
 * @author erhannis
 *
 * @param <T>
 * @param <U>
 * 
 * @see jcsp.helpers.FunctionChannel
 */
public interface FCServer<T, U>
{
   public T startRead();

   public void endRead(U u);
}
