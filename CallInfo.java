package com.ibm.sport.rtc.common;

public class CallInfo
{
   private CallType callType;
   private String center;
   private String dispatchedTo;
   private String pmrName;
   private String pPG;
   private String queue;

   public CallType getCallType()
   {
      return callType;
   }
   
   // _B68102A
   /**
    * Convert the CallType enum to a usable string value.
    * 
    * @return - A string version of the call type or empty string.
    */
   public String getCallTypeAsString()
   {
      String callTypeValue = "";
      if (this.getCallType().equals( CallType.BACKUP ))
      {
         callTypeValue = "Backup";
      }
      else if (this.getCallType().equals( CallType.PRIMARY ))
      {
         callTypeValue = "Primary";
      }
      else if (this.getCallType().equals( CallType.SECONDDARY ))
      {
         callTypeValue = "Secondary";
      }
      return (callTypeValue);
   }

   public String getCenter()
   {
      return center;
   }

   public String getDispatchedTo()
   {
      return dispatchedTo;
   }

   public String getPmrName()
   {
      return pmrName;
   }

   public String getPPG()
   {
      return pPG;
   }

   public String getQueue()
   {
      return queue;
   }

   public void setCallType( CallType callType )
   {
      this.callType = callType;
   }

   public void setCenter( String center )
   {
      this.center = center;
   }

   public void setDispatchedTo( String dispatchedTo )
   {
      this.dispatchedTo = dispatchedTo;
   }

   public void setPmrName( String pmrName )
   {
      this.pmrName = pmrName;
   }

   public void setPPG( String pPG )
   {
      this.pPG = pPG;
   }

   public void setQueue( String queue )
   {
      this.queue = queue;
   }
}
