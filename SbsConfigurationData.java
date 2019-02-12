package com.ibm.sport.rtc.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

public class SbsConfigurationData
{
   private String sbsUserName = null;
   private String sbsPassword = null;
   private String sbsServerContextRoot = null;
   private String sbsServerHostName = null;
   private Integer sbsServerPortNumber = null;
   private Integer sbsServerSslPortNumber = null;
   private String sbsProviderClass = null;
   private String sbsProviderName = null;
   private String sbsProviderType = null;
   // _T24561A
   private List<String> projectAreaComponents = null;
   private Hashtable<String, String[]> rtcIdsToCompCTs = null;
   private Boolean reopenConfig = new Boolean( Boolean.FALSE );
   private Boolean pullInSubscribedPMRsConfig = new Boolean( Boolean.TRUE );
   // _F155269A FIXCAT, Preventative Service Program (PSP)
   // PSP Keywords / APAR Close
   // { Component : {KW : {DESCRIPTION : Description, etc...} } } map -
   private HashMap<String, HashMap<String, HashMap<String, String>>> pspKeywordComponentMap = null;

   public String getSbsUserName()
   {
      return sbsUserName;
   }

   public String getSbsUserNameNonNull()
   {
      if (sbsUserName == null)
         throw new SportRuntimeException( "SBS User Name is not configured" );
      return sbsUserName;
   }

   public String getSbsPassword()
   {
      return sbsPassword;
   }

   public String getSbsPasswordNonNull()
   {
      if (sbsPassword == null)
         throw new SportRuntimeException( "SBS Password is not configured" );
      return sbsPassword;
   }

   public String getSbsProviderClass()
   {
      return sbsProviderClass;
   }

   public String getSbsProviderClassNonNull()
   {
      if (sbsProviderClass == null)
         throw new SportRuntimeException(
               "SBS Provider Class is not configured" );
      return sbsProviderClass;
   }

   public String getSbsProviderType()
   {
      return sbsProviderType;
   }

   public String getSbsProviderTypeNonNull()
   {
      if (sbsProviderType == null)
         throw new SportRuntimeException(
               "SBS Provider Type is not configured" );
      return sbsProviderType;
   }

   public String getSbsProviderName()
   {
      return sbsProviderName;
   }

   public String getSbsProviderNameNonNull()
   {
      if (sbsProviderName == null)
         throw new SportRuntimeException(
               "SBS Provider Name is not configured" );
      return sbsProviderName;
   }

   public String getSbsServerContextRoot()
   {
      return sbsServerContextRoot;
   }

   public String getSbsServerContextRootNonNull()
   {
      if (sbsServerContextRoot == null)
         throw new SportRuntimeException(
               "SBS Server Context Root is not configured" );
      return sbsServerContextRoot;
   }

   public String getSbsServerHostNameNonNull()
   {
      if (sbsServerHostName == null)
         throw new SportRuntimeException(
               "SBS Server Host Name is not configured" );
      return sbsServerHostName;
   }

   public Integer getSbsServerPortNumber()
   {
      return sbsServerPortNumber;
   }

   public Integer getSbsServerPortNumberNonNull()
   {
      if (sbsServerPortNumber == null)
         throw new SportRuntimeException(
               "SBS Server Port Number is not configured "
                     + "or is improperly configured" );
      return sbsServerPortNumber;
   }

   public Integer getSbsServerSslPortNumber()
   {
      return sbsServerSslPortNumber;
   }

   public Integer getSbsServerSslPortNumberNonNull()
   {
      if (sbsServerSslPortNumber == null)
         throw new SportRuntimeException(
               "SBS Server Port Number is not configured "
                     + "or is improperly configured" );
      return sbsServerSslPortNumber;
   }

   public String getSbsServerHostName()
   {
      return sbsServerHostName;
   }

   // _T24561A
   public List<String> getProjectAreaComponents()
   {
      return this.projectAreaComponents;
   }

   public Hashtable<String, String[]> getRtcIdsToCompCTs()
   {
      return this.rtcIdsToCompCTs;
   }

   public String getComponentsForRtcId( String rtcId )
   {
      String[] line = this.rtcIdsToCompCTs.get( rtcId );
      try
      {
         return line[0];
      }
      catch (ArrayIndexOutOfBoundsException x)
      {
         return "";
      }
   }

   public String getChangeTeamsForRtcId( String rtcId )
   {
      String[] line = this.rtcIdsToCompCTs.get( rtcId );
      try
      {
         return line[1];
      }
      catch (ArrayIndexOutOfBoundsException x)
      {
         return "";
      }
   }

   public Boolean getReopenConfig()
   {
      return reopenConfig;
   }

   public Boolean getPullInSubscribedPMRsConfig()
   {
      return pullInSubscribedPMRsConfig;
   }

   // _F155269A FIXCAT, Preventative Service Program (PSP)
   // PSP Keywords / APAR Close -
   public HashMap<String, HashMap<String, HashMap<String, String>>> getProjectAreaPSPKeywordComponentMap()
   {
      return this.pspKeywordComponentMap;
   }

   public void setSbsUserName( String sbsUserName )
   {
      this.sbsUserName = sbsUserName;
   }

   public void setSbsPassword( String sbsPassword )
   {
      this.sbsPassword = sbsPassword;
   }

   public void setSbsProviderClass( String sbsProviderClass )
   {
      this.sbsProviderClass = sbsProviderClass;
   }

   public void setSbsProviderName( String sbsProviderName )
   {
      this.sbsProviderName = sbsProviderName;
   }

   public void setSbsProviderType( String sbsProviderType )
   {
      this.sbsProviderType = sbsProviderType;
   }

   public void setSbsServerContextRoot( String sbsServerContextRoot )
   {
      this.sbsServerContextRoot = sbsServerContextRoot;
   }

   public void setSbsServerHostName( String sbsServerHostName )
   {
      this.sbsServerHostName = sbsServerHostName;
   }

   public void setSbsServerPortNumber( Integer sbsServerPortNumber )
   {
      this.sbsServerPortNumber = sbsServerPortNumber;
   }

   public void setSbsServerSslPortNumber( Integer sbsServerSslPortNumber )
   {
      this.sbsServerSslPortNumber = sbsServerSslPortNumber;
   }

   // public void setSbsAuthRequired( Boolean sbsAuthRequired )
   // {
   // this.sbsAuthRequired = sbsAuthRequired;
   // }

   // _T24561A
   public void setProjectAreaComponents( List<String> componentList )
   {
      this.projectAreaComponents = componentList;
   }

   /**
    * Actually adds unique components to list 1 at a time
    * 
    * @param component
    */
   public void setProjectAreaComponents( String component )
   {
      if (component != null & component.length() > 0)
      {
         if (this.projectAreaComponents == null)
         {
            this.projectAreaComponents = new ArrayList<String>();
         }
         if (!this.projectAreaComponents.contains( component ))
         {
            this.projectAreaComponents.add( component );
         }
      }
   }

   public void setComponentsAndChangeTeamsForRtcId( String rtcId,
         String comps, String changeTeams )
   {
      if (this.rtcIdsToCompCTs == null)
         rtcIdsToCompCTs = new Hashtable<String, String[]>();
      // Parse and format the string such that all components are capitalized,
      // separated by a comma,
      // and one space only
      String[] toAdd = new String[2];
      if ((comps != null) && (!comps.equals( "" )))
      {
         StringTokenizer st = new StringTokenizer( comps, "," );
         StringBuilder sb = new StringBuilder( "" );
         while (st.hasMoreTokens())
         {
            String comp = st.nextToken().toUpperCase().trim();
            sb.append( comp + ", " );
         }
         int index = sb.lastIndexOf( " " );
         if (index >= 0)
            sb.deleteCharAt( index );
         index = sb.lastIndexOf( "," );
         if (index >= 0)
            sb.deleteCharAt( index );
         toAdd[0] = sb.toString();
      }
      if ((changeTeams != null) && (!changeTeams.equals( "" )))
      {
         StringTokenizer st = new StringTokenizer( changeTeams, "," );
         StringBuilder sb = new StringBuilder( "" );
         while (st.hasMoreTokens())
         {
            String ct = st.nextToken().toUpperCase().trim();
            sb.append( ct + ", " );
         }
         int index = sb.lastIndexOf( " " );
         if (index >= 0)
            sb.deleteCharAt( index );
         index = sb.lastIndexOf( "," );
         if (index >= 0)
            sb.deleteCharAt( index );
         toAdd[1] = sb.toString();
      }
      this.rtcIdsToCompCTs.put( rtcId, toAdd );
   }

   public void setReopenConfig( String value )
   {
      if (value != null)
      {
         if (value.trim().toLowerCase().equals( "true" ))
         {
            if (reopenConfig != null)
               reopenConfig = Boolean.TRUE;
            else
               reopenConfig = new Boolean( true );
         }
         else
         {
            if (reopenConfig != null)
               reopenConfig = Boolean.FALSE;
            else
               reopenConfig = new Boolean( false );
         }
      }
      else
      {
         if (reopenConfig != null)
            reopenConfig = Boolean.FALSE;
         else
            reopenConfig = new Boolean( false );
      }
   }

   public void setPullInSubscribedPMRsConfig( String value )
   {
      if (value != null)
      {
         if (value.trim().toLowerCase().equals( "true" ))
         {
            if (pullInSubscribedPMRsConfig != null)
               pullInSubscribedPMRsConfig = Boolean.TRUE;
            else
               pullInSubscribedPMRsConfig = new Boolean( true );
         }
         else
         {
            if (pullInSubscribedPMRsConfig != null)
               pullInSubscribedPMRsConfig = Boolean.FALSE;
            else
               pullInSubscribedPMRsConfig = new Boolean( false );
         }
      }
      else
      {
         if (pullInSubscribedPMRsConfig != null)
            pullInSubscribedPMRsConfig = Boolean.TRUE;
         else
            pullInSubscribedPMRsConfig = new Boolean( true );
      }
   }

   public boolean doesIdExist( String rtcId )
   {
      if (this.rtcIdsToCompCTs != null)
      {
         if (this.rtcIdsToCompCTs.containsKey( rtcId ))
            return true;
         else
            return false;
      }
      else
         return false;
   }

   public void removeRtcId( String rtcId )
   {
      if (this.rtcIdsToCompCTs != null)
         rtcIdsToCompCTs.remove( rtcId );
   }

   // _F155269A FIXCAT, Preventative Service Program (PSP)
   // PSP Keywords / APAR Close
   // { Component : {KW : {DESCRIPTION : Description, etc...} } } map -
   // private HashMap<String, HashMap<String, HashMap<String, String>>>
   /**
    * Set from file import
    * 
    * @param thePSPkwComponentMap
    */
   public void setProjectAreaPSPKeywordComponentData(
         HashMap<String, HashMap<String, HashMap<String, String>>> thePSPkwComponentMap )
   {
      this.pspKeywordComponentMap = thePSPkwComponentMap;
   }

   // _F155269A FIXCAT, Preventative Service Program (PSP)
   // PSP Keywords / APAR Close
   // { Component : {KW : {DESCRIPTION : Description, etc...} } } map -
   // HashMap<String, HashMap<String, HashMap<String, String>>>
   /* @formatter:off */
   /**
    * Gather project area configuration of the form - 
    * {@code
    *        <sportPSPKeywordComponentMapping> [0,1]
    *             <sportPSPKeywordComponentData componentName="..."> [0,*]
    *                  <sportPSPKeyword name="..." description="...", etc .../> [0,*]
    *                  (more attributes may get added later, after description)
    *             </sportPSPKeywordComponentData>
    *        </sportPSPKeywordComponentMapping>
    * }
    * <br>
    * Into the following map
    * { Component : {KW : {DESCRIPTION : Description, etc...} } }
    * 
    * @param componentName
    * @param keyword
    * @param description
    */
   /* @formatter:on */
   public void setProjectAreaPSPKeywordComponentData( String componentName,
         String keyword,
         // String description,
         // Add any other keyword-related attributes here ...
         HashMap<String, String> pspKWDetails )
   {
      if (pspKeywordComponentMap == null)
      {
         // 1st map reference - get a new one
         pspKeywordComponentMap = new HashMap<String, HashMap<String, HashMap<String, String>>>();
      }
      HashMap<String, HashMap<String, String>> pspKWComponentEntry = pspKeywordComponentMap
            .get( componentName );
      if (pspKWComponentEntry == null)
      {
         // 1st Component reference - get a new entry
         pspKWComponentEntry = new HashMap<String, HashMap<String, String>>();
      }
      // Set or replace keyword entry and Keyword Component map entry
      pspKWComponentEntry.put( keyword, pspKWDetails );
      pspKeywordComponentMap.put( componentName, pspKWComponentEntry );
   }

}
