package com.ibm.sport.rtc.common;

public class SbsErrorException
      extends SportUserActionFailException
{
   private static final long serialVersionUID = 1L;
   private String sbsResponseDetails;

   public SbsErrorException( String message )
   {
      super( message );
      sbsResponseDetails = null;
   }

   public SbsErrorException( String message, String sbsResponseDetails )
   {
      super( message );
      this.sbsResponseDetails = sbsResponseDetails;
   }

   public String getSbsResponseDetails()
   {
      return sbsResponseDetails;
   }
}
