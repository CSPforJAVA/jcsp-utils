Utilities for JCSP (https://github.com/CSPforJAVA/jcsp).  Buncha miscellaneous helpers - the most useful is probably `logDeadlock`.

Most of these should have further details in the Javadocs.

jcsp.lang - I think I needed to put these in this package for class permissions reasons.
  1. BooleanGuard - A Guard you can turn on and off.  I'm not sure why I wrote this class, honestly - given the existence of `priSelect(boolean[] preCondition)`, this seems redundant.  I think I wrote this before discovering the `preCondition` argument.  Let's call this class deprecated.
  2. DisableableTimer - A convenience, to turn a timer off rather than just setting its time to some future value.
jcsp.helpers
  1. FunctionChannel, FCClient, FCServer - channel carrying a function call.  Kinda like call channels, but (IMO) simpler to use.  See FunctionChannel javadocs.
  2. JcspUtils - a number of static helper methods.
    a. clear(AltingChannelInput<T>) - discard pending data until none remains.
    b. ChannelOutput<T> logDeadlock(ChannelOutput<T>) - This wraps the channel in a layer that monitors for deadlock.
      I. If a call to `.write(T)` on the underlying channel blocks for more than 10 seconds, a watchdog process (a static singleton, shared across all calls to `logDeadlock`) prints "Deadlock detected!" and the stack trace of the call that blocked.
      II. If a call presumed deadlocked subsequently completes, the following is logged: "Nevermind; the following wasn't actually a deadlock.  Stopped for {time}ms".
      III.  Note: wrapping a channel like this is somewhat expensive.  Every message sent causes an exception and stack trace to be preemptively created, and two extra channel calls made to the single coordinating process.  However - in practice it's useful enough, and fast enough, that I've left it all over production code with no apparent problem.
  3. AltingChannelInput<T> spawn(Consumer<ChannelOutput<T>>) - for quickly making a producer of stuff.
  4. vent(AltingChannelInput) - sends the channel to a process that forever pulls and discards any items arriving on any of its channels.  The idea is if you want to kill a receiver without blocking processes that send to it, you vent the receiver's channels.  There may be more idiomatic ways of dealing with this situation, but...here this is.
  5. ChannelOutput<T> buffer(ChannelOutput<T> target, ChannelDataStore<T> buffer) - wraps a target channel with a buffered channel.  Warning: starts a process to do so, and currently there's no provided way to kill it.  Beware thread clutter.
  6. <T> AltingChannelInput<T> antidote(AltingChannelInput<T>) and its kin - wraps a channel in another that swallows PoisonExceptions.

Install to local Maven with `mvn clean install`.

Add dependency as
```
<dependency>
    <groupId>cspforjava</groupId>
    <artifactId>jcsp-utils</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Released under the Apache License, Version 2.0.

-Erhannis
