package jcsp.lang;

/**
 * A CSTimer you can turn off, rather than just setting it to some distant
 * future time.
 * Call `turnOff()` to turn the timer off. Calling `setAlarm()` or `set()` will
 * turn it back on.
 * 
 * @author erhannis
 *
 */
public class DisableableTimer extends CSTimer
{
   private boolean enabled = false;

   public void turnOff()
   {
      enabled = false;
   }
   
   public boolean isOff()
   {
       return !enabled;
   }

   @Override
   boolean enable( Alternative alt )
   {
      if (!enabled)
      {
         return false;
      }
      return super.enable(alt);
   }

   @Override
   boolean disable()
   {
      if (!enabled)
      {
         return false;
      }
      return super.disable();
   }

   @Override
   public void setAlarm( long msecs )
   {
      super.setAlarm(msecs);
      enabled = true;
   }

   @Override
   public void set( long msecs )
   {
      super.set(msecs);
      enabled = true;
   }
}
