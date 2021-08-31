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
public abstract class AltingFCServer<T, U> extends Guard implements FCServer<T, U>, Poisonable
{
}
