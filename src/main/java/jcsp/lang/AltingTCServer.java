package jcsp.lang;

import jcsp.helpers.*;

/**
 * 
 * @author erhannis
 *
 * @param <T>
 * @param <U>
 * 
 * @see jcsp.helpers.FunctionChannel
 */
public abstract class AltingTCServer<T, U> extends Guard implements TCServer<T, U>, Poisonable
{
}
