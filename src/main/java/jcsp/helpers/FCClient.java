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
public interface FCClient<T, U>
{
   public U call(T t);
}
