package jcsp.helpers;

import jcsp.lang.Poisonable;

/**
 * 
 * @author erhannis
 *
 * @param <T>
 * @param <U>
 * 
 * @see jcsp.helpers.FunctionChannel
 */
public interface FCClient<T, U> extends Poisonable
{
   public U call(T t);
}
