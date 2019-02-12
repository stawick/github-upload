package com.ibm.sport.rtc.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;

import com.ibm.ArtifactTechnology.common.Constants;
import com.ibm.team.process.common.IProcessConfigurationData;
import com.ibm.team.process.common.IProcessConfigurationElement;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.process.common.IProjectAreaHandle;

/** 
 * _T24561A Singleton class 
 * Used to cache RETAIN Component browse results for releases and change teams
 * 
 * @author stawick
 *
 */
public class ComponentBrowseCache
{
   // Components (hash of key to list of Strings)
   // ex - SPoRT.ComponentBrowse.release.<component> - [release list]
   private static HashMap<String, List<String>> componentCache = new HashMap<String, List<String>>();
   // Separate mapping of project area to components used in project area
   // ex - (No prefix) <projectAreaName> - [component list]
   private static HashMap<String, List<String>> projAreaComponentMap = new HashMap<String, List<String>>();
   
   // Constants
   private static String cacheKeyPrefix = "SPoRT.ComponentBrowse";
   
   /** 
    * Singleton -
    * No instances should ever be defined.
    */
   public ComponentBrowseCache() {}
   
   /** 
    * Method to cache SBS component browse releases.
    * 
    * @param browseComponentValue
    * @param cachedData
    */
   public static void cacheComponentReleaseData( String browseComponentValue,
         List<String> cachedData )
   {
      String cacheKey = constructReleaseCacheKey( browseComponentValue );
      // Replace with new values
      synchronized (ComponentBrowseCache.class)
      {
         componentCache.put( cacheKey, cachedData );
      }
   }
   
   /** 
    * Method to cache SBS component browse change teams.
    * 
    * @param browseComponentValue
    * @param cachedData
    */
   public static void cacheComponentChangeTeamData( String browseComponentValue,
         List<String> cachedData )
   {
      String cacheKey = constructChangeTeamCacheKey( browseComponentValue );
      // Replace with new values
      synchronized (ComponentBrowseCache.class)
      {
         componentCache.put( cacheKey, cachedData );
      }
   }
   
   /** 
    * Method to cache project area's component use or reference.
    * Warning, will replace any previous set of cached components
    * with the argument passed in.
    * Note: When used in a grow-as-it-goes environment, typically
    * 1 component at a time gets added, using the recordComponentUse
    * method, which insures a unique component reference.
    * Can also be called on first component reference to cache 
    * project area component list map.
    * 
    * @param sportCommonData
    * @param projAreaComponents
    */
   public static void cacheProjectAreaComponentData( SportCommonData sportCommonData,
         List<String> projAreaComponents )
   {
      String projectAreaName = constructProjectAreaCacheKey( sportCommonData );
      if (projectAreaName != null && projectAreaName.length() > 0)
      {
         synchronized (ComponentBrowseCache.class)
         {
            projAreaComponentMap.put( projectAreaName, projAreaComponents );
         }
      }
   }
   
   /** 
    * Method to remove cache of SBS component browse releases
    * and change teams. Determines if a key was passed in,
    * and assumes the last qualifier is the component to use.
    * To be used to be able to refresh cache with RETAIN data
    * on next Component Browse use. (SPoRT Admin feature. TBD) 
    * 
    * @param browseComponentValue (or input key)
    */
   public static void clearComponentData( String inputKey )
   {
      String browseComponentValue = inputKey;
      // Set up regular expression to split at dot qualifiers (ie ".")
      String[] keyComponents = browseComponentValue.split( "\\." );
      if (keyComponents.length > 1)
      {
         // Get the last (dot) qualifier === the component
         browseComponentValue = keyComponents[keyComponents.length-1];
      }
      clearComponentReleaseData( browseComponentValue );
      clearComponentChangeTeamData( browseComponentValue );
   }
   
   /** 
    * Method to remove cache of SBS component browse releases.
    * 
    * @param browseComponentValue
    */
   private static void clearComponentReleaseData( String browseComponentValue )
   {
      // Remove values
      // Keep the key for component collection derivation, remove the values
      cacheComponentReleaseData(browseComponentValue, null);
   }
   
   /** 
    * Method to remove cache of SBS component browse change teams.
    * 
    * @param browseComponentValue
    */
   private static void clearComponentChangeTeamData( String browseComponentValue )
   {
      // Remove values
      // Keep the key for component collection derivation, remove the values
      cacheComponentChangeTeamData(browseComponentValue, null);
   }

   /** 
    * Method to obtain the key for the singleton cache
    * (Used when specific data to be referenced is known)
    * 
    * @param browseComponentValue
    * @param cacheKeyType
    * @return
    */
   private static String constructCacheKey( String browseComponentValue,
         String cacheKeyType )
   {
      String returnKey = cacheKeyPrefix;
      // Use upper case on component to insure no duplicate components
      returnKey += "." + cacheKeyType + "."
            + browseComponentValue.toUpperCase();
      return returnKey;
   }

   /** 
    * Method to obtain the key for the singleton cache
    * (Used when specific data to be referenced is known)
    * 
    * @param browseComponentValue
    * @return
    */
   private static String constructReleaseCacheKey( 
         String browseComponentValue )
   {
      return (constructCacheKey( browseComponentValue,
            AparConstants.SBS_COMPONENT_BROWSE_RELEASE_FIELD_NAME ));
   }

   /** 
    * Method to obtain the key for the singleton cache
    * (Used when specific data to be referenced is known)
    * 
    * @param browseComponentValue
    * @return
    */
   private static String constructChangeTeamCacheKey(
         String browseComponentValue )
   {
      return (constructCacheKey( browseComponentValue,
            AparConstants.SBS_COMPONENT_BROWSE_CHANGE_TEAM_FIELD_NAME ));
   }

   /** 
    * Method to obtain the key for the singleton cache
    * (Used when specific data to be referenced is known)
    * Sets the key to the project area name which should be
    * unique within a repository.
    * 
    * @param sportCommonData
    * @return
    */
   private static String constructProjectAreaCacheKey(
         SportCommonData sportCommonData )
   {
      String projectAreaKey = null;
      IProjectAreaHandle projectArea = sportCommonData.getProjectArea();
      IProjectArea project = null;
      try
      {
         project = RtcUtils.getProjectArea( projectArea, sportCommonData );
      }
      catch (Exception e)
      {
         // Any exception at all, dont panic, return null key
         // Attempt to log the exception at least
         Log log = sportCommonData.getLog();
         String errMsg = "SPoRT Component Browse Cache error encountered. "
            + "Problem encountered obtaining project area "
            + "for component map key. "
            + Constants.NEWLINE
            + e.getMessage();
         log.warn( errMsg );
         return (null);
      }
      if (project != null)
      {
         projectAreaKey = project.getName(); 
      }

      return (projectAreaKey);
   }

   /** 
    * Method to get the releases cached.
    * Returns a new List object separate from the cached data.
    * 
    * @param browseComponentValue
    * @return
    */
   public static List<String> obtainCachedReleaseList(
         String browseComponentValue )
   {
      // Attempt to find in cache 
      // Using component name as part of the key
      String cacheKey = constructReleaseCacheKey( browseComponentValue );
      // return (componentCache.get( cacheKey ));
      List<String> result = null;
      List<String> cachedReleases = null;
      synchronized (ComponentBrowseCache.class)
      {
         cachedReleases = componentCache.get( cacheKey );
         if (cachedReleases != null && !cachedReleases.isEmpty())
         {
            // Return a separate copy of the values for synchronization
            // purposes
            result = new ArrayList<String>();
            result.addAll( cachedReleases );
         }
      }
      return (result);
   }

   /** 
    * Method to get the change teams cached.
    * Returns a new List object separate from the cached data.
    * 
    * @param browseComponentValue
    * @return
    */
   public static List<String> obtainCachedChangeTeamList(
         String browseComponentValue )
   {
      // Attempt to find in cache 
      // Using component name as part of the key
      String cacheKey = constructChangeTeamCacheKey( browseComponentValue );
      // return (componentCache.get( cacheKey ));
      List<String> result = null;
      List<String> cachedCTs = null;
      synchronized (ComponentBrowseCache.class)
      {
         cachedCTs = componentCache.get( cacheKey );
         if (cachedCTs != null && !cachedCTs.isEmpty())
         {
            // Return a separate copy of the values for synchronization
            // purposes
            result = new ArrayList<String>();
            result.addAll( cachedCTs );
         }
      }
      return (result);
   }

   /** 
    * Method to get the name of ALL components cached.
    * For SPoRT Admin feature. (TBD)
    * Utilizes an intermediate sorted (tree) set to provide
    * the components in a sorted order. Then returns a basic List that is
    * sorted.
    * 
    * @return
    */
   public List<String> obtainCachedComponentNamesList()
   {
      // List of release and change team keys for all components cached
      // Set<String> componentKeys = componentCache.keySet();
      Set<String> componentKeys = null;
      synchronized (ComponentBrowseCache.class)
      {
         componentKeys = componentCache.keySet();
      }
      List<String> componentValues = null;
      TreeSet<String> sortedComponents = null;
      String[] keyComponents = null;
      String componentValue = "";
      // Test if cache is empty first, prevent exception
      if (componentKeys != null)
      {
         for (String compKey : componentKeys)
         {
            componentValue = compKey;
            // Set up regular expression to split at dot qualifiers (ie ".")
            keyComponents = componentValue.split( "\\." );
            if (keyComponents.length > 1)
            {
               // Get the last (dot) qualifier === the component
               componentValue = keyComponents[keyComponents.length - 1];
            }
            if (sortedComponents == null)
            {
               // Add 1st entry
               sortedComponents = new TreeSet<String>();
               sortedComponents.add( componentValue );
            }
            else
            {
               // Only add specific component once
               if (!sortedComponents.contains( componentValue ))
               {
                  sortedComponents.add( componentValue );
               }
            }
         }
         if (sortedComponents != null && !sortedComponents.isEmpty())
         {
            // Sort the results, and return a basic List that is sorted
            componentValues = new ArrayList<String>();
            componentValues.addAll( sortedComponents );
         }

      }
      return (componentValues);
   }

   /** 
    * Method to get the name of components cached for a given project area.
    * Utilizes an intermediate sorted (tree) set as a result to provide
    * the components in a sorted order. Then returns a basic List that is
    * sorted.
    * On the first reference for a project area, obtains a pre-defined list
    * if defined in the project area's configuration by an aspect editor.
    * 
    * @param sportCommonData
    * @return
    */
   public static List<String> obtainCachedComponentNamesList(
         SportCommonData sportCommonData )
   {
      List<String> componentValues = null;
      String projectAreaName = constructProjectAreaCacheKey( sportCommonData );
      List<String> projectAreaComponents = null;
      if (projectAreaName != null && projectAreaName.length() > 0)
      {
         // projectAreaComponents = projAreaComponentMap.get( projectAreaName );
         synchronized (ComponentBrowseCache.class)
         {
            // A local List object is NOT needed here since no further
            // List modifications are being done.
            projectAreaComponents = projAreaComponentMap
                  .get( projectAreaName );
         }
      }
      
      if (projectAreaComponents == null || projectAreaComponents.isEmpty())
      {
         // 1st component reference - gather from project area configuration
         projectAreaComponents = obtainProjectAreaComponents( sportCommonData );
         // Set in cache
         cacheProjectAreaComponentData( sportCommonData,
               projectAreaComponents );
      }
      // Test if cache is empty first, prevent exception
      if (projectAreaComponents != null && !projectAreaComponents.isEmpty())
      {
         // Sort the results, and return a basic List that is sorted
         TreeSet<String> sortedComponents = new TreeSet<String>();
         sortedComponents.addAll( projectAreaComponents );
         componentValues = new ArrayList<String>();
         componentValues.addAll( sortedComponents );
      }
      return (componentValues);
   }

   /** 
    * Method to get the name of components for a given project area
    * from its SBS configuration data defined by an aspect editor.
    * Utilizes a sorted (tree) set as a result to provide
    * the components in a sorted order.
    * 
    * @param sportCommonData
    * @return
    */
   protected static List<String> obtainProjectAreaComponents(
         SportCommonData sportCommonData )
   {
      List<String> componentValues = null;
      try
      {
         // 1st component reference - gather from project area
         IProcessConfigurationData processConfigData = RtcUtils
               .getProcessConfigurationData(
                     "com.ibm.sport.rtc.configuration.sbs", sportCommonData );
         componentValues = obtainProjectAreaComponents( processConfigData
               .getElements() );
      }
      catch (Exception e)
      {
         // Any exception at all, dont panic, return null
         // Attempt to log the exception at least
         Log log = sportCommonData.getLog();
         String errMsg = "SPoRT Component Browse Cache error encountered. "
            + "Problem encountered obtaining SBS process configuration data "
            + "for initial project area components. "
            + "No components returned."
            + Constants.NEWLINE
            + e.getMessage();
         log.warn( errMsg );
         return (null);
      }
      return (componentValues);
   }
   
   /** 
    * Method to get the name of components for a given project area
    * from its SBS configuration data defined by an aspect editor.
    * Results returned as a List in unsorted order.
    * 
    * The SBS process configuration structure -
    *
    *  <configuration-data id="com.ibm.sport.rtc.configuration.sbs">
    *        <sbsUser password="..." userName="..."/>
    *        <sbsProvider class="..." name="..." type="..."/>
    *        <sbsServer contextRoot="..." hostName="..." portNumber="..." sslPortNumber="..."/>
    *  and what we need here -
    *        <projectAreaComponentList> {0,1}
    *             <projectAreaComponent name="..."/> {0,*}
    *        </projectAreaComponentList>
    *  </configuration-data>
    * 
    * @param configData[]
    * @return
    */
   protected static List<String> obtainProjectAreaComponents(
         IProcessConfigurationElement configData[] )
   {
      List<String> componentValues = null;
      String projAreaComponent = null;
      if (configData != null)
      {
         for (IProcessConfigurationElement configElement : configData)
         {
            // SBS configuration parent element, or component list element
            if (configElement.getName().equals( "configuration-data" ) ||
                  configElement.getName().equals( "projectAreaComponentList" ))
            {
               // Process children
               return (obtainProjectAreaComponents( configElement
                     .getChildren() ));
            }
            // Process child elements -
            // Skip irrelevant elements, use only component elements - 
            if (configElement.getName().equals( "projectAreaComponent" ))
            {
               projAreaComponent = configElement.getAttribute( "name" );
               if (projAreaComponent != null
                     && projAreaComponent.length() > 0)
               {
                  if (componentValues == null)
                  {
                     // Adding 1st entry
                     componentValues = new ArrayList<String>();
                     componentValues.add( projAreaComponent.toUpperCase() );
                  }
                  else
                  {
                     // Only add specific component once
                     if (!componentValues.contains( projAreaComponent
                           .toUpperCase() ))
                     {
                        componentValues.add( projAreaComponent.toUpperCase() );
                     }
                  }
                  // Neither release nor change teams defined yet,
                  // initialize in map to null
                  boolean firstReference = false;
                  synchronized (ComponentBrowseCache.class)
                  {
                     firstReference = (!(componentCache
                           .containsKey( constructReleaseCacheKey( projAreaComponent ) )) 
                           && !(componentCache
                           .containsKey( constructChangeTeamCacheKey( projAreaComponent ) )));
                  }
                  if (firstReference)
                  {
                     clearComponentData( projAreaComponent );
                  }
               }
            }
         }
      }
      return (componentValues);
   }
   
   /**
    * For a given project area - 
    * as components are cached, ensure the component is mapped for the
    * project area.
    * Component set is additive. Grow as it goes.
    * First reference will attempt to obtain initial set from project area 
    * configuration.
    * 
    * Currently invoked when saving releases and change teams to cache
    * for component. May also be invoked on SPoRT work item create to
    * record component reference for project area. (Handles things like
    * polled work items.) 
    * 
    * @param sportCommonData
    * @param browseComponentValue
    */
   public static void recordComponentUse( 
         SportCommonData sportCommonData,
         String browseComponentValue )
   {
      // Make sure value is in project area map
      String projectAreaName = constructProjectAreaCacheKey( sportCommonData );
      List<String> projectAreaComponents = null;
      List<String> cacheMappedComponents = null;
      if (projectAreaName != null && projectAreaName.length() > 0)
      {
         // projectAreaComponents = projAreaComponentMap.get( projectAreaName );
         synchronized (ComponentBrowseCache.class)
         {
            cacheMappedComponents = projAreaComponentMap
                  .get( projectAreaName );
            if (cacheMappedComponents != null
                  && !cacheMappedComponents.isEmpty())
            {
               // The hash map returns a reference to the List of elements.
               // Need to use a local copy of the hash map list
               // for synchronization purposes.
               projectAreaComponents = new ArrayList<String>();
               projectAreaComponents.addAll( cacheMappedComponents );
            }
         }
      }

      if (projectAreaComponents == null || projectAreaComponents.isEmpty())
      {
         // 1st component reference - gather from project area configuration
         projectAreaComponents = obtainProjectAreaComponents( sportCommonData );
      }
      if (projectAreaComponents == null)
      {
         // Either project area or component set not defined yet
         projectAreaComponents = new ArrayList<String>();
      }
      if (!(projectAreaComponents.contains( browseComponentValue )))
      {
         // Component not used yet - add to list, replace in map
         // Adds to a local List object, and caches resulting set
         projectAreaComponents.add( browseComponentValue );
         cacheProjectAreaComponentData( sportCommonData,
               projectAreaComponents );
      }
      // Neither release nor change teams defined yet, 
      // initialize in map to null
      boolean firstReference = false;
      synchronized (ComponentBrowseCache.class)
      {
         firstReference = (!(componentCache
               .containsKey( constructReleaseCacheKey( browseComponentValue ) )) 
               && !(componentCache
               .containsKey( constructChangeTeamCacheKey( browseComponentValue ) )));
      }
      if (firstReference)
      {
         clearComponentData( browseComponentValue );
      }
   }
}