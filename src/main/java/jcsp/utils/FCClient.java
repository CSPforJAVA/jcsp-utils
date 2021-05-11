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
public interface FCClient<T, U>
{
   public U call(T t);
}
