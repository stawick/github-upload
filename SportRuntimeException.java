package com.ibm.sport.rtc.common;

public class SportRuntimeException
      extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   public SportRuntimeException( String message )
   {
      super( message );
   }

   public SportRuntimeException( String message, Throwable cause )
   {
      super( message, cause );
   }

   public static SportRuntimeException WrapInSportRtcException( Throwable t,
         String message )
   {
      return (t instanceof SportRuntimeException) ? (SportRuntimeException)t
            : new SportRuntimeException( message + ": " + t.getMessage(), t );

   }
}
