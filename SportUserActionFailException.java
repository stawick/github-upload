package com.ibm.sport.rtc.common;

public class SportUserActionFailException
      extends Exception
{
   private static final long serialVersionUID = 1L;

   public SportUserActionFailException( String message )
   {
      super( message );
   }
}
