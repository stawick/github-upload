package com.ibm.sport.rtc.common;

public class SysrouteAparInfo
{
   private String targetAparName;
   private String sourceAparName;
   private String newAparName;        //apar to be created
   private String origAparName;       //the apar that the new apar is related to
   public enum relationship {SYSROUTED_TO, SYSROUTED_FROM };
 
   
   public SysrouteAparInfo(String targetApar, String sourceApar, relationship relation)
   throws SportRuntimeException
   {
      switch (relation)
      {
         case SYSROUTED_FROM :
            targetAparName = sourceApar;       //switch so the link is correct
            sourceAparName = targetApar;
            newAparName = targetApar;
            origAparName = sourceApar;
            break;
         case SYSROUTED_TO:
            targetAparName = targetApar;   
            sourceAparName = sourceApar;   
            newAparName = targetApar;
            origAparName = sourceApar;
            break;
         default:
            String message = "Invalid relationship type " +
            relation + 
            " encountered, only sysroutedFrom or sysroutedTo supported";
            throw new SportRuntimeException( message );   
      }

      
   }
   
   public String getTargetAparName()
   {
      return targetAparName;
   }
   public void setTargetAparName( String targetAparName )
   {
      this.targetAparName = targetAparName;
   }
   public String getSourceAparName()
   {
      return sourceAparName;
   }
   public String getNewAparName( )
   {
      return newAparName;
   }
   public String getOriginalAparName( )
   {
      return origAparName;
   }

   
   
}