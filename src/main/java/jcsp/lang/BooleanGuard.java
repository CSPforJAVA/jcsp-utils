package jcsp.lang;

/**
 * Guard you can turn on and off.
 * 
 * @author erhannis
 *
 */
public class BooleanGuard extends Guard
{
   private boolean ready = false;

   /**
    * Sets the guard readiness. Do not call while an alternative is selecting on
    * this guard.
    * 
    * @param ready
    * @return whether the guard was previously ready
    */
   public boolean set( boolean ready )
   {
      boolean wasReady = this.ready;
      this.ready = ready;
      return wasReady;
   }

   @Override
   boolean enable( Alternative alt )
   {
      return ready;
   }

   @Override
   boolean disable()
   {
      return ready;
   }
}
