package com.ibm.sport.rtc.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.ArtifactTechnology.common.Artifact;
import com.ibm.ArtifactTechnology.common.ArtifactFieldValue;
import com.ibm.ArtifactTechnology.common.ArtifactRecordField;
import com.ibm.ArtifactTechnology.common.ArtifactRecordPart;
import com.ibm.ArtifactTechnology.common.Constants;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.internal.attributeValueProviders.IConfiguration;
import com.ibm.team.workitem.common.internal.attributeValueProviders.IFilteredValueSetProvider;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

/** 
 * _T21519A RETAIN Component browse to obtain dynamic choices
 * for release, change team, and applicable releases (with some
 * result formatting - <release> + <Y|N>)
 * @author stawick
 *
 */
public class ComponentBrowseValueSetUtils
      implements IFilteredValueSetProvider<Object>
      // implements IValueSetProvider<Object>
{
   protected static String noComponentErrMsg1 = "*** Values to select are derived from the component.";
   protected static String noComponentErrMsg2 = "*** Please specify a component and try again.";
   protected static String notifySPoRTExcpMsg = "*** SPoRT SBS error obtaining values! Check SPoRT SBS logs. Try again later.";
   protected static String notifyOtherExcpMsg = "*** Error encountered obtaining values! Check logs. Try again later.";
   // _F206987A Exclude invalid releases as choices.
   protected final static ArrayList<String> INCLUDE_INVALID_RELEASE_ATTRIBUTE_IDS = new ArrayList<String>();
   /*
   // For future enhancement. Currently all attributes require ONLY
   // VALID releases. Add here any attributes that should include
   // invalid releases (for whatever reasons). 
   static
   {
      INCLUDE_INVALID_RELEASE_ATTRIBUTE_IDS.add( "" );
   }
   */
   // _T257292C Attributes that require release choices returned.
   // Eventually replicate for component, change teams, etc when those
   // are hit.
   protected final static ArrayList<String> RELEASE_CHOICE_ATTRIBUTE_IDS = new ArrayList<String>();
   static
   {
      RELEASE_CHOICE_ATTRIBUTE_IDS.add( AparConstants.RELEASE_ATTRIBUTE_ID );
      RELEASE_CHOICE_ATTRIBUTE_IDS
            .add( AparConstants.TO_RELEASE_ATTRIBUTE_ID );
      // _T266500C Changed to stringList type for multiple selection.
      RELEASE_CHOICE_ATTRIBUTE_IDS.add(
            AparConstants.DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID );
      /*
      // _T266500C With the advent of multiple selection via a single
      // stringList type, this selector has become deprecated.
      RELEASE_CHOICE_ATTRIBUTE_IDS.add(
            AparConstants.DRAFT_APPLICABLE_COMPONENT_LVLS_SELECTOR_ATTRIBUTE_ID );
      */
      // _T266500C Changed to stringList type for multiple selection.
      RELEASE_CHOICE_ATTRIBUTE_IDS
            .add( AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_ATTRIBUTE_ID );
      /*
      // _T266500C With the advent of multiple selection via a single
      // stringList type, this selector has become deprecated.
      RELEASE_CHOICE_ATTRIBUTE_IDS.add(
            AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_SELECTOR_ATTRIBUTE_ID );
      */
      RELEASE_CHOICE_ATTRIBUTE_IDS
            .add( AparConstants.SYSTEM_RELEASE_LEVEL_ATTRIBUTE_ID );
   }
   // _T257292A Enable APAR default system level determination.
   protected final static ArrayList<String> INCLUDE_SREL_RELEASE_ATTRIBUTE_IDS = new ArrayList<String>();
   static
   {
      INCLUDE_SREL_RELEASE_ATTRIBUTE_IDS
            .add( AparConstants.SYSTEM_RELEASE_LEVEL_ATTRIBUTE_ID );
   }
   // _T266500A Multiple selection.
   // Attributes that allow multiple selections.
   protected final static ArrayList<String> MULTI_SELECT_ATTRIBUTE_IDS = new ArrayList<String>();
   static
   {
      // _T266500C Changed to stringList type for multiple selection.
      MULTI_SELECT_ATTRIBUTE_IDS.add(
            AparConstants.DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID );
      /*
      // _T266500C With the advent of multiple selection via a single
      // stringList type, this selector has become deprecated.
      MULTI_SELECT_ATTRIBUTE_IDS.add(
            AparConstants.DRAFT_APPLICABLE_COMPONENT_LVLS_SELECTOR_ATTRIBUTE_ID );
      */
      // _T266500C Changed to stringList type for multiple selection.
      MULTI_SELECT_ATTRIBUTE_IDS
            .add( AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_ATTRIBUTE_ID );
      /*
      // _T266500C With the advent of multiple selection via a single
      // stringList type, this selector has become deprecated.
      MULTI_SELECT_ATTRIBUTE_IDS.add(
            AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_SELECTOR_ATTRIBUTE_ID );
      */
   }
   // _T266500A Attributes to be excluded from filtering.
   protected final static ArrayList<String> NON_FILTERED_ATTRIBUTE_IDS = new ArrayList<String>();
   /*
   // For future enhancement. Currently all attributes can take
   // advantage of the choice filtering feature.
   static
   {
      NON_FILTERED_ATTRIBUTE_IDS.add( "" );
   }
   */

   /** 
    * These next 2 methods are derived from the implemented class.
    * We must provide these.
    * TODO Not sure whether we intend to filter the results? (TBD)
    * 
    * @param attribute
    * @param workItem
    * @param workItemCommon
    * @param configuration
    * @param filter
    * @param monitor
    * @return
    * @throws TeamRepositoryException
    */
   // _T266500C Now uses filtering of choices.
   @SuppressWarnings("unchecked")
   public List getFilteredValueSet( IAttribute attribute, IWorkItem workItem,
         IWorkItemCommon workItemCommon, IConfiguration configuration,
         String filter, IProgressMonitor monitor )
         throws TeamRepositoryException
   {
      // Get initial values without filter.
      List<String> returnValues = getValueSet( attribute, workItem, workItemCommon,
            configuration, monitor );
      // _T266500A If no filter, no choices to filter, or limiting
      // filtering to specific attributes, return full set when not
      // filtering.
      if (filter == null || filter.trim().length() == 0
            || returnValues == null || returnValues.isEmpty()
            || expectedAttributeId( NON_FILTERED_ATTRIBUTE_IDS,
                  attribute.getIdentifier() ))
      {
         // No filter regex or no choices, use all choices
         return returnValues;
      }
      // Filter values to be returned.
      List<String> filteredValues = null;
      // Insure filter in usable regex format
      String processedFilter = CommonUtils.processFilterToRegex( filter );
      try
      {
         for (String returnChoice : (List<String>)returnValues)
         {
            if (returnChoice.matches( processedFilter ))
            {
               if (filteredValues == null)
               {
                  filteredValues = new ArrayList<String>();
               }
               filteredValues.add( returnChoice );
            }
         }
      }
      catch (Exception e)
      {
         // Catch a potential regex exception and just indicate
         // regex error as choice. Have user try again.
         if (filteredValues == null)
         {
            filteredValues = new ArrayList<String>();
         }
         filteredValues.add( "Exception encountered with filter: "
               + e.getMessage() );
      }

      // Prevent null pointer exception in value-set picker presentation
      // when list returned from cache is empty.
      // Return empty list in this case.
      return (filteredValues == null ? new ArrayList<String>()
            : filteredValues);
   }

   @SuppressWarnings("unchecked")
   public List getValueSet( IAttribute attribute, IWorkItem workItem,
         IWorkItemCommon workItemCommon, IConfiguration configuration,
         IProgressMonitor monitor )
         throws TeamRepositoryException
   {
      List<String> returnValues = null;

      // _T257294C Reuse common utility code - 
      SportCommonData sportCommonData = CommonUtils
            .getLimitedUseSPoRTDataObject( workItem, workItemCommon,
                  this.getClass().getPackage().getName(), monitor );

      String populateChoiceAttrId = attribute.getIdentifier();
      String browseComponentValue = getComponentValue( populateChoiceAttrId,
            workItem, sportCommonData );
      
      // When requesting component list, might not have value yet - OK
      if ((browseComponentValue == null || browseComponentValue.length() == 0)
            && !componentsNeeded( populateChoiceAttrId ))
      {
         // No component to use (ex. not specified yet on Create)
         returnValues = new ArrayList<String>();
         returnValues.add( noComponentErrMsg1 );
         returnValues.add( noComponentErrMsg2 );
         return returnValues;
      }

      // Attempt to find in cache 1st
      // Using browseComponentValue as part of the key
      // _T24561C
      returnValues = obtainCachedList( browseComponentValue,
            populateChoiceAttrId, sportCommonData );
      
      // Only call component browse for release or change team attributes
      // _T257294C Prevent redundant component browse and cache when
      // requesting a subset of choices that may be empty.
      if ((returnValues == null || returnValues.isEmpty())
            && !componentsNeeded( populateChoiceAttrId )
            && !subsetEmptyYetComponentCached( browseComponentValue,
                  populateChoiceAttrId ))
      {
         // Do the SBS component browse
         try
         {
            // SBS call - (via AparUtils)
            String componentReplacement = null;
            // Obtaining choices for Sysroute attributes -
            // (Reroute and close both use the base component attr)
            if (sysrouteProcessing( populateChoiceAttrId ))
            {
               // Need to set component to different value (Sysroute action)
               // Get base component field in artifact, 
               // used by component browse
               componentReplacement = browseComponentValue;
            }
            Artifact componentBrowseResults = AparUtils.componentBrowse(
                  workItem, sportCommonData, componentReplacement );
            // Call method to extract desired attribute values after
            // successful SBS call -
            // Cache these results (complete set), only on success
            // Using browseComponentValue as part of the key
            cacheComponentData( componentBrowseResults, browseComponentValue,
                  sportCommonData ); // _T24561C
            // Extract correct data from newly formed cache values
            returnValues = obtainCachedList( browseComponentValue,
                  populateChoiceAttrId,
                  sportCommonData ); // _T24561C
         }
         catch (SportUserActionFailException e)
         {
            // Handle SBS exception - 
            // Return error msg as result value
            // Allows primary function to continue, while indicating error 
            // within this peripheral feature. 
            if (returnValues == null)
               returnValues = new ArrayList<String>();
            returnValues.add( notifySPoRTExcpMsg );
            returnValues.add( e.getMessage() );
            // Attempt to log the exception at least
            Log log = sportCommonData.getLog();
            String errMsg = "SPoRT Component Browse error encountered. "
               + "Problem encountered obtaining RETAIN data for component "
               + browseComponentValue
               + Constants.NEWLINE
               + e.getMessage();
            log.warn( errMsg );
         }
         catch (Exception e)
         {
            // Handle other exception - 
            // Return error msg as result value
            // Allows primary function to continue, while indicating error 
            // within this peripheral feature. 
            if (returnValues == null)
               returnValues = new ArrayList<String>();
            returnValues.add( notifyOtherExcpMsg );
            returnValues.add( e.getMessage() );
            // Attempt to log the exception at least
            Log log = sportCommonData.getLog();
            String errMsg = "SPoRT Component Browse error encountered. "
               + "Problem encountered obtaining RETAIN data for component "
               + browseComponentValue
               + Constants.NEWLINE
               + e.getMessage();
            log.warn( errMsg );
         }
      }

      // Now remove any choices already selected in a "collector" 
      // attribute to prevent duplicate selections.
      returnValues = preventDuplicateChoices( returnValues,
            populateChoiceAttrId, workItem, sportCommonData );
      // Note: RTC requirement open to use a string list type attribute
      // plus multi-select presentation.

      // Prevent null pointer exception in value-set picker presentation
      // when list returned from cache is empty.
      // Return empty list in this case.
      // return returnValues;
      return (returnValues == null ? new ArrayList<String>() : returnValues);
   }
   
   /** 
    * Method to obtain the component used based on attribute processing.
    * Returns the value upper cased in case user input is in lower case.
    * 
    * @param populateChoiceAttrId
    * @param workItem
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    */
   public static String getComponentValue(
         String populateChoiceAttrId, 
         IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String componentValue = "";
      // Obtaining choices for Sysroute attributes -
      // (Reroute and close both use the base component attr)
      if (sysrouteProcessing( populateChoiceAttrId ))
      {
         // Get Sysroute component attr from workItem argument
         componentValue = RtcUtils.getAttributeValueAsString(
               workItem, AparConstants.TO_COMPONENT_ATTRIBUTE_ID,
               sportCommonData );
      }
      else
      {
         // Get base component attr from workItem argument
         componentValue = RtcUtils.getAttributeValueAsString(
               workItem, AparConstants.COMPONENT_ATTRIBUTE_ID,
               sportCommonData );
      }
      // In case value is from user-input, ensure upper case used
      if (componentValue != null && componentValue.length() > 0)
      {
         componentValue = componentValue.toUpperCase();
      }

      return componentValue;
   }

   /** 
    * Method to get the specific attribute-related info.
    * Use attribute to determine data to extract from cache.
    * 
    * @param browseComponentValue
    * @param populateChoiceAttrId
    * @param sportCommonData _T24561A
    * @return
    */
   public static List<String> obtainCachedList( String browseComponentValue,
         String populateChoiceAttrId,
         SportCommonData sportCommonData )
   {
      List<String> cachedValues = null;
      // Attempt to find in cache 1st
      // Using browseComponentValue as part of the key

      // Requesting component releases - 
      if (releasesNeeded( populateChoiceAttrId ))
      {
         cachedValues = ComponentBrowseCache
               .obtainCachedReleaseList( browseComponentValue );
         if (cachedValues != null && !cachedValues.isEmpty())
         {
            // Might data need formatting?
            // Since we may be altering values, we dont want to modify
            // the cached values - so need new list
            List<String> formattedValues = new ArrayList<String>();
            for (String releaseValue : cachedValues)
            {
               // _F206987A Exclude invalid releases as choices.
               // Skip release choice if invalid and should NOT be
               // included.
               if (!validRelease( releaseValue, populateChoiceAttrId )
                     && !includeInvalidReleases( populateChoiceAttrId ))
                  continue;
               // Strip off any potential invalid indicator.
               // Note Strings are immutable, so this should return a
               // new String object with the modifications requested.
               // On the next iteration we start with a handle to the
               // next cached release.
               // _T257292A When "S" SREL type release is valid for
               // selection, strip that indicator off as well.
               releaseValue = cleanRelease( releaseValue );
               if (releaseYNType( populateChoiceAttrId ))
               {
                  // Release + Y, N
                  formattedValues.add( releaseValue + "Y" );
                  formattedValues.add( releaseValue + "N" );
               }
               else
               {
                  formattedValues.add( releaseValue );
               }
            }
            // Return the reformatted release choices
            return formattedValues;
         }

      }
      // Requesting component change teams -
      if (changeTeamsNeeded( populateChoiceAttrId ))
      {
         cachedValues = ComponentBrowseCache
               .obtainCachedChangeTeamList( browseComponentValue );
      }
      
      // Alternate use - just get components cached
      if (componentsNeeded( populateChoiceAttrId ))
      {
         // _T24561C Get components specific to project area
         // Returned as a basic list, but sorted
         cachedValues = ComponentBrowseCache
               .obtainCachedComponentNamesList( sportCommonData ); 
      }

      return cachedValues;
   }

   /** 
    * Method to cache SBS component browse results 
    * from SBS result artifact.
    * 
    * @param componentBrowseResults
    * @param browseComponentValue
    * @param sportCommonData _T24561A
    */
   public static void cacheComponentData( Artifact componentBrowseResults,
         String browseComponentValue,
         SportCommonData sportCommonData )
   {
      // SBS component browse result artifact will contain BOTH 
      // release AND change team data. Cache both sets of data.
      List<String> cachedData = new ArrayList<String>();

      ArtifactRecordPart cbMainRecord = componentBrowseResults
            .findRecord( 
                  AparConstants.SBS_COMPONENT_BROWSE_MAIN_RECORD_NAME );
      // Obtain releases AND change teams from component browse to save 
      // in cache.
      // Obtain Release data from Component Browse results
      for (ArtifactRecordPart componentReleaseRecord : cbMainRecord
            .findRecords( 
                  AparConstants.SBS_COMPONENT_BROWSE_RELEASE_RECORD_NAME ))
      {
         // Process each release value
         String releaseValue = componentReleaseRecord
               .findFieldValue( 
                     AparConstants.SBS_COMPONENT_BROWSE_RELEASE_FIELD_NAME );
         if (releaseValue != null)
         {
            // _F206987A Exclude invalid releases as choices.
            // See if the release is valid, append a <RETAINinvalid>
            // indicator but store the release anyway. Choice
            // functions can optionally pull all values or sort out
            // (and trim) these invalid choices.
            // _T257292A Identify an "S" type SREL default release type
            // and append that release with <RETAINsrel>. If needed,
            // can test for this indicator as a suffix.
            String releaseType = componentReleaseRecord.findFieldValue(
                  AparConstants.SBS_COMPONENT_BROWSE_RELEASETYPE_FIELD_NAME );
            if (releaseType != null)
            {
               // _T257292C Fix invalid release identification, per RETAIN SSF doc.
            	// Set delimiter for content test
               String testReleaseType = " " + releaseType + " ";
               if (!(AparConstants.SBS_COMPONENT_BROWSE_VALID_RELEASETYPE_VALUES
                     .contains( testReleaseType )))
               {
                  // _T257292A Enable APAR default system level determination.
                  // An SREL "S" type release gets into here.
                  if (releaseType.equalsIgnoreCase(
                        AparConstants.SBS_COMPONENT_BROWSE_SREL_RELEASETYPE_VALUE ))
                  {
                     // Mark as SREL, but store in cache.
                     releaseValue += AparConstants.SBS_COMPONENT_BROWSE_SREL_RELEASETYPE_INDICATOR;
                  }
                  else
                  {
                     // Mark as invalid, but store in cache.
                     releaseValue += AparConstants.SBS_COMPONENT_BROWSE_INVALID_RELEASETYPE_INDICATOR;
                  }
               }
            }
            cachedData.add( releaseValue );
         }
      }

      // Cache releases - 
      // Cache these results (complete set), only on success
      // Using browseComponentValue as part of the key
      ComponentBrowseCache.cacheComponentReleaseData(
            browseComponentValue, cachedData );

      // Obtain change team data from Component Browse results
      // Since we are defining new values, we dont want to modify the 
      // cached release values - so need new list
      // cachedData.clear(); // Reset internal list
      cachedData = new ArrayList<String>();
      ArtifactRecordField chgTeamField = cbMainRecord
            .findField( 
                  AparConstants.SBS_COMPONENT_BROWSE_CHANGE_TEAM_FIELD_NAME );
      if (chgTeamField != null)
      {
         for (ArtifactFieldValue chgTeamValue : chgTeamField
               .getFieldValues())
         {
            String chgTeamStr = chgTeamValue.getValue();
            cachedData.add( chgTeamStr );
         }
      }

      // Cache change teams 
      // Cache these results (complete set), only on success
      // Using browseComponentValue as part of the key
      ComponentBrowseCache.cacheComponentChangeTeamData(
            browseComponentValue, cachedData );
      
      // _T24561A
      // Now cache the use of this component for this project area
      // for component list retrieval.
      // In the off chance that a component browse is done before
      // project area components are retrieved, this will retrieve
      // the project area components from the process configuration.
      ComponentBrowseCache.recordComponentUse( sportCommonData,
            browseComponentValue );
   }
   
   /** 
    * Method to remove choices already in some other "collector" attribute.
    * Use attribute to determine data to check for duplicates.
    * Note: While some cases have changed to use a stringList attribute
    * to allow multiple selections, keep this method general purpose
    * enough to allow reuse for other types, just in case.
    * 
    * @param initialComponentBrowseChoices
    * @param populateChoiceAttrId
    * @param workItem
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    */
   public static List<String> preventDuplicateChoices(
         List<String> initialComponentBrowseChoices,
         String populateChoiceAttrId, IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // Strip out choices already in some "collector" attribute
      // Initially all are returned
      List<String> returnValues = initialComponentBrowseChoices;
      String opt1 = "";
      String opt2 = "";
      
      String collectorValues = null;
      // Get collector attribute value (values already selected in UI).
      // Attribute indicates it is multi-value capable.
      // TODO use REAL selector attribute when implemented
      // _T266500C Changed to stringList type for multiple selection.
      // With the advent of multiple selection via a single stringList
      // type, the old selectors have become deprecated in both APAR
      // Close and Sysroute cases.
      if (expectedAttributeId( MULTI_SELECT_ATTRIBUTE_IDS,
            populateChoiceAttrId ))
      {
         // Get collector attribute values already selected.
         // Format may be "[v1, v2, ..., vN]" (comma AND space
         // delimited).
         collectorValues = RtcUtils.getAttributeValueAsString( workItem,
               populateChoiceAttrId, sportCommonData );
      }
      // If we have values already chosen (rrrY and / or rrrN)
      if (collectorValues != null && collectorValues.length() > 0)
      {
         // _T266500A Remove optional brackets for general stringList
         // selector handling.
         collectorValues = collectorValues
               .replaceAll( CommonConstants.SQUARE_BRACKET_LEFT, "" );
         collectorValues = collectorValues
               .replaceAll( CommonConstants.SQUARE_BRACKET_RIGHT, "" );
         // _B233387A Handle alternate nbsp space chars.
         // Scrub and convert to blanks. Allows for correct duplicate
         // recognition in possible manual-entry existing value.
         collectorValues = RtcUtils.scrubChars( collectorValues );
         // New set to be determined
         // Since we are altering values, we dont want to modify the 
         // cached values - so need new list
         returnValues = new ArrayList<String>();
         // Upper case for non-case-sensitive search.
         // _T266500C Format may now be "v1, v2, ..., vN" when
         // stringList used.
         ArrayList<String> alreadySelected = StringUtils
               .arrayListFromArray( collectorValues.toUpperCase().split(
                     CommonConstants.COMMA_ANDOR_SPACE_SPLIT_PATTERN ) );
         // Remove values already chosen from component-defined rrrX
         // values gathered.
         for (String returnValue : initialComponentBrowseChoices)
         {
            // Once a rrrY or rrrN is selected, neither rrr choice 
            // should be present (even though RETAIN SDI allows both
            // rrrY AND rrrN to be set - but it doesnt make sense)
            opt1 = returnValue.substring( 0, returnValue.length() - 1 ) + "Y";
            opt2 = returnValue.substring( 0, returnValue.length() - 1 ) + "N";
            if (alreadySelected.indexOf( opt1 ) < 0
                  && alreadySelected.indexOf( opt2 ) < 0)
            {
               // rrr{Y|N} Not yet selected, add it to new set
               returnValues.add( returnValue );
            }
         }
      }
      
      return returnValues;
   }

   /**
    * Return status based on selector attribute being processed 
    * @return
    */
   public static boolean changeTeamsNeeded( String populateChoiceAttrId )
   {
      // Requesting component change teams - 
      return ( populateChoiceAttrId
            .equals( AparConstants.CURRENT_CHANGE_TEAM_ATTRIBUTE_ID ) 
            || populateChoiceAttrId
            .equals( AparConstants.TO_CHANGE_TEAM_ATTRIBUTE_ID )
            );
   }

   /**
    * Return status based on selector attribute being processed 
    * @return
    */
   public static boolean componentsNeeded( String populateChoiceAttrId )
   {
      // Requesting choices for component attributes -
      return (populateChoiceAttrId
            .equals( AparConstants.COMPONENT_ATTRIBUTE_ID ) 
            || populateChoiceAttrId
            .equals( AparConstants.TO_COMPONENT_ATTRIBUTE_ID )
            );
   }

   /**
    * Return status based on selector attribute being processed 
    * @return
    */
   public static boolean releasesNeeded( String populateChoiceAttrId )
   {
      // _T257292C Requesting component releases -
      return (expectedAttributeId( RELEASE_CHOICE_ATTRIBUTE_IDS,
            populateChoiceAttrId ));
}

   /**
    * Return status based on selector attribute being processed 
    * @return
    */
   public static boolean releaseYNType( String populateChoiceAttrId )
   {
      // Obtaining choices for applicable levels attributes -
      // These will need Y/N appended to the releases.
      return ( populateChoiceAttrId
            .equals( AparConstants.DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID )
            // .equals( AparConstants.DRAFT_APPLICABLE_COMPONENT_LVLS_SELECTOR_ATTRIBUTE_ID )
            || populateChoiceAttrId
            .equals( AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_ATTRIBUTE_ID )
            // .equals( AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_SELECTOR_ATTRIBUTE_ID )
      );
   }

   // _F206987A Exclude invalid releases as choices.
   /**
    * Return status based on selector attribute being processed 
    * @return
    */
   public static boolean includeInvalidReleases( String populateChoiceAttrId )
   {
      // For future enhancement. Currently all attributes require ONLY
      // VALID releases. Add here a test for any attributes that
      // should include invalid releases (for whatever reasons). 
      // Update the static ArrayList with such attribute IDs.
      return (expectedAttributeId( INCLUDE_INVALID_RELEASE_ATTRIBUTE_IDS,
            populateChoiceAttrId ));
   }

   // _F206987A Exclude invalid releases as choices.
   /**
    * Invalid releases have an indicator appended (which needs to be
    * trimmed).
    * <br>This must now test for both invalid indicator, or whether
    * the SREL is indicated and it is not requested, or NOT indicated
    * and it IS requested.
    * 
    * @param releaseValue
    * @param populateChoiceAttrId
    * 
    * @return
    */
   public static boolean validRelease( String releaseValue,
         String populateChoiceAttrId )
   {
      // Return if invalid indicator set.
      if (releaseValue
            .endsWith( AparConstants.SBS_COMPONENT_BROWSE_INVALID_RELEASETYPE_INDICATOR ))
         return false;

      // _T257292A "S" SREL type also not valid for attributes that
      // utilize release selection choices.
      if (srelRelease( releaseValue )
            && !includeSRELReleases( populateChoiceAttrId ))
         return false;
      // When requesting SREL releases specifically, and this
      // is not one, also invalid.
      if (!srelRelease( releaseValue )
            && includeSRELReleases( populateChoiceAttrId ))
         return false;

      // Otherwise a valid release.
      return true;
   }

   // _T257292A Enable APAR default system level determination.
   /**
    * Indicate that only a subset of values were actually empty, but
    * the Component may have already been cached.
    * 
    * <p>
    * When requesting SREL releases and there were none, BUT releases
    * were already cached for the component, bypass redundant cache
    * processing.
    * 
    * <p>
    * Return status based on selector attribute being processed.
    * 
    * @return
    */
   public static boolean subsetEmptyYetComponentCached( String browseComponentValue,
         String populateChoiceAttrId )
   {
      if (includeSRELReleases( populateChoiceAttrId ))
      {
         // SREL-requesting attribute -
         // Determine if *any* releases cached yet
         List<String> cachedReleases = ComponentBrowseCache
               .obtainCachedReleaseList( browseComponentValue );
         return (cachedReleases != null && !cachedReleases.isEmpty());
      }
      return false;
   }

   // _T257292A Enable APAR default system level determination.
   /**
    * Return status based on selector attribute being processed.
    * Currently, MOST attributes do not include the SREL type of
    * release as a possible choice.
    * 
    * @return
    */
   public static boolean includeSRELReleases( String populateChoiceAttrId )
   {
      // Update the static ArrayList with applicable attribute IDs.
      return (expectedAttributeId( INCLUDE_SREL_RELEASE_ATTRIBUTE_IDS,
            populateChoiceAttrId ));
   }

   // _T257292A Enable APAR default system level determination.
   /**
    * SREL releases have an indicator appended (which needs to be
    * trimmed)
    *  
    * @return
    */
   public static boolean srelRelease( String releaseValue )
   {
      // Return if SREL indicator set.
      // _T257292C "S" type also not valid for attributes that utilize
      // release selection choices. System level allows "S" type and
      // must explicitly inspect for it.
      return (releaseValue.endsWith(
            AparConstants.SBS_COMPONENT_BROWSE_SREL_RELEASETYPE_INDICATOR )
      );
   }

   // _T257292A Enable APAR default system level determination.
   /**
    * Invalid or SREL releases have an indicator appended (which needs
    * to be trimmed).
    *  
    * @return
    */
   public static String cleanRelease( String releaseValue )
   {
      String returnValue = releaseValue;
      // Strip off any potential invalid indicator.
      // Note Strings are immutable, so this should return a
      // new String object with the modifications requested.
      // On the next iteration we start with a handle to the
      // next cached release.
      returnValue = returnValue.replaceAll(
            AparConstants.SBS_COMPONENT_BROWSE_INVALID_RELEASETYPE_INDICATOR,
            "" );
      // _T257292A When "S" SREL type release is valid for
      // selection, strip that indicator off as well.
      returnValue = returnValue.replaceAll(
            AparConstants.SBS_COMPONENT_BROWSE_SREL_RELEASETYPE_INDICATOR,
            "" );

      return returnValue;
   }

   /**
    * Return status based on selector attribute being processed 
    * @return
    */
   public static boolean sysrouteProcessing( String populateChoiceAttrId )
   {
      // Obtaining choices for Sysroute attributes -
      // (Reroute and close both use the base component attr)
      return ( populateChoiceAttrId
            .equals( AparConstants.TO_RELEASE_ATTRIBUTE_ID )
            || populateChoiceAttrId
                  .equals( AparConstants.TO_CHANGE_TEAM_ATTRIBUTE_ID ) 
            || populateChoiceAttrId
                  .equals( AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_ATTRIBUTE_ID )
                  // .equals( AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_SELECTOR_ATTRIBUTE_ID )
      );
   }

   /**
    * Return status based on attribute being processed and list of
    * expected attribute IDs to match. Common method to use.
    * 
    * @param expectedAttrIdSet - Array list of expected attribute
    * IDs to which expected function is to be performed.
    * @param populateChoiceAttrId - attribute being processed.
    * 
    * @return
    */
   public static boolean expectedAttributeId(
         ArrayList<String> expectedAttrIdSet, String populateChoiceAttrId )
   {
      return (expectedAttrIdSet != null && !(expectedAttrIdSet.isEmpty())
            && expectedAttrIdSet.contains( populateChoiceAttrId ));
   }

}