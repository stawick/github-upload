package com.ibm.sport.rtc.common;

import java.util.ArrayList;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.internal.attributeValueProviders.AttributeValueProviderDescriptor;
import com.ibm.team.workitem.common.internal.attributeValueProviders.AttributeValueProviderRegistry;
import com.ibm.team.workitem.common.internal.attributeValueProviders.IConfiguration;
import com.ibm.team.workitem.common.internal.attributeValueProviders.IValidator;
import com.ibm.team.workitem.common.internal.util.SeparatedStringList;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

/**
 * 
 * RTC attribute validation java extension.
 * 
 * <p>The idea is to provide a generic java extension to validate
 * attributes in cases where RTC-provided regular expression or
 * scripted providers do not abide. This is rather like a utility
 * class that allows for a single plugin.xml extension definition and
 * when the need arises, the process template can utilize this feature,
 * with necessary logic written herein. This is applied by RTC as a
 * precondition test, upon a work item Save. It is executed on EVERY
 * save, regardless of action. In fact, a work flow action cannot be
 * tested for. We might get the context of a modify action by a
 * specific attribute (by work item type), but do not know the context
 * of any work flow action.
 * <br>
 * The intent is to do specific validation by individual methods based
 * on the applied attribute (passed as an argument), and return the
 * result. This can include common methods that can be reused like for
 * length checking, when needed. This can afford things like and / or
 * combination of validation logic.
 * <br>
 * It might be useful to collect ALL plugin validation code here.
 * Specific validation methods may need to be re-used in other plugin
 * code which may need a SportCommonData argument.
 * <br>
 * When an unknown attribute type is used (ex template change made
 * without making necessary logic updates here), no validity checking
 * takes place. This assumes a valid attribute.
 * 
 * <p>See example on jazz.net forum at<br>
 * https://jazz.net/forum/questions/111462/custom-ivalidator-plugin-error-comibmteamworkitemcommon-validator-not-found
 * 
 * @author stawick
 *
 */
// The SeparatedStringList is apparently an internal RTC type, and
// access is discouraged. But we need to use it for stringList
// attribute validation. So suppress the warnings for the import and
// reference(s).
@SuppressWarnings("restriction")
// _T266500A RTC attribute validation java extension.
public class SPoRTRTCAttributeValidatorExtension
      implements IValidator
{

   // Define attribute lists here to which specific validation methods
   // are to be called.
   protected final static ArrayList<String> APPLICABLE_RELEASE_ATTRIBUTE_IDS = new ArrayList<String>();
   static
   {
      APPLICABLE_RELEASE_ATTRIBUTE_IDS.add(
            AparConstants.DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID );
      /*
      APPLICABLE_RELEASE_ATTRIBUTE_IDS.add(
            AparConstants.DRAFT_APPLICABLE_COMPONENT_LVLS_SELECTOR_ATTRIBUTE_ID );
      */
      APPLICABLE_RELEASE_ATTRIBUTE_IDS
            .add( AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_ATTRIBUTE_ID );
      /*
      APPLICABLE_RELEASE_ATTRIBUTE_IDS.add(
            AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_SELECTOR_ATTRIBUTE_ID );
      */
   }

   /**
    * Front door precondition method RTC invokes from work item Save
    * in UI for attribute validation.
    * 
    * <p> Arguments passed by RTC via precondition front door.
    * @param attribute - attribute validation applied to.
    * @param workItem
    * @param workItemCommon
    * @param configuration - may be null if from another plugin.
    * @param monitor
    */
   @Override
   public IStatus validate( IAttribute attribute, IWorkItem workItem,
         IWorkItemCommon workItemCommon, IConfiguration configuration,
         IProgressMonitor monitor )
         throws TeamRepositoryException
   {
      // Reuse common utility code -
      // Specific attribute validation methods may need this as an
      // argument, so obtain what we can from the "front door".
      SportCommonData sportCommonData = CommonUtils
            .getLimitedUseSPoRTDataObject( workItem, workItemCommon,
                  this.getClass().getPackage().getName(), monitor );
      // Descriptor needed for plugin ID in Status object. See example
      // from link in class header.
      // If called from a SPoRT plugin, the configuration might not be
      // supplied.
      AttributeValueProviderDescriptor descriptor = null;
      if (configuration != null)
      {
         descriptor = AttributeValueProviderRegistry.getInstance()
               .getProviderDescriptor( configuration );
      }
      // Lets save a few calls.
      String pluginId  = ""; 
      if (descriptor != null)
      {
         pluginId = descriptor.getOriginatingPluginId();
      }
      String validateAttrId = attribute.getIdentifier();
      String validateAttrName = attribute.getDisplayName();
      // This is how status is returned.
      // As a default, unknown attribute, success ...
      IStatus isValid = new Status( IStatus.OK, pluginId,
            validateAttrName + " is OK." );

      // Handle applicable release related attribute(s).
      // The isOK test is in case an attribute gets multiple
      // validations applied, kick it out on the 1st error.
      if (isValid.isOK() &&
            applicableReleasesCondition( attribute, validateAttrId,
                  validateAttrName, workItem, workItemCommon, configuration,
                  monitor, pluginId, sportCommonData )
            )
      {
         isValid = validateApplicableReleases( attribute, validateAttrId,
               validateAttrName, workItem, workItemCommon, configuration,
               monitor, pluginId, sportCommonData );
      }

      return isValid;
   }

   /**
    * Return status based on selector attribute being processed and
    * list of expected attribute IDs to match. Common method to use.
    * 
    * @param expectedValidateAttrSet - Array list of expected attribute
    * IDs to which validation is expected to be performed.
    * @param validateAttrId - attribute validation applied to.
    * 
    * @return
    */
   // Make methods "static" so an instance of this class is not
   // necessary. Attribute-specific methods may then be reused by
   // other plug-ins.
   public static boolean expectedValidateAttr(
         ArrayList<String> expectedValidateAttrSet, String validateAttrId )
   {
      return (expectedValidateAttrSet != null
            && !(expectedValidateAttrSet.isEmpty())
            && expectedValidateAttrSet.contains( validateAttrId ));
   }

   /**
    * Logic if applicable release related attribute(s) should be
    * validated. Applies "filtering" logic to prevent redundant
    * validation since performed on EVERY save.
    * 
    * <br> (*) = Arguments passed by RTC via precondition front door.
    * @param validateAttribute (*) - attribute validation applied to.
    * @param validateAttrId
    * @param validateAttrName
    * @param workItem (*)
    * @param workItemCommon (*)
    * @param configuration (*) - may be null if from another plugin.
    * @param monitor (*)
    * @param pluginId - derived from configuration or supplied by
    *                   internal plugin.
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    */
   public static boolean applicableReleasesCondition(
         IAttribute validateAttribute, String validateAttrId,
         String validateAttrName, IWorkItem workItem,
         IWorkItemCommon workItemCommon, IConfiguration configuration,
         IProgressMonitor monitor, String pluginId,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      if (!expectedValidateAttr( APPLICABLE_RELEASE_ATTRIBUTE_IDS,
            validateAttrId ))
         return false; // do NOT validate
      // Otherwise a known attribute - dig deeper ...

      // Draft applicable releases needs to be valid when - (subset)
      // draftCloseCode==(FIN, OEM, PER, URX).
      // (Also several modify actions and a work flow action that
      // cannot be tested anyway. Try to limit the validation.)
      // Otherwise skip test, since done on EVERY save.
      if (validateAttrId.equals(
            AparConstants.DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID ))
      {
         // APAR Close attribute -
         // Applicable to the following close codes at least.
         String validateCloseCodes = " FIN OEM PER URX ";
         String draftCloseCodeValue = RtcUtils.getAttributeValueAsString(
               workItem, AparConstants.DRAFT_CLOSE_CODE_ATTRIBUTE_ID,
               sportCommonData );
         // Just need to test the code portion (1st 3 chars).
         if (draftCloseCodeValue == null || draftCloseCodeValue.isEmpty()
               || !(validateCloseCodes
                     .contains( " " + draftCloseCodeValue.substring( 0, 3 ) + " " )))
         {
            return false; // do NOT validate
         }
         else
         {
            return true; // validate
         }
      }

      // Sysroute applicable releases needs to be valid when - (subset)
      // sysrouteType=(New CLOSED APAR). (Also the modify action is
      // Sysroute. Try to limit the validation.)
      // Otherwise skip test, since done on EVERY save.
      if (validateAttrId.equals(
            AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_ATTRIBUTE_ID ))
      {
         // Sysroute attribute -
         String validateSysrouteAction = " Sysroute ";
         String modifyActionValue = RtcUtils.getAttributeValueAsString(
               workItem, AparConstants.MODIFY_ACTION_ATTRIBUTE_ID,
               sportCommonData );
         String validateSysrouteType = " New CLOSED APAR ";
         String sysrouteTypeValue = RtcUtils.getAttributeValueAsString(
               workItem, AparConstants.SYSROUTE_TYPE_ATTRIBUTE_ID,
               sportCommonData );
         if (modifyActionValue == null || modifyActionValue.isEmpty()
               || !(validateSysrouteAction
                     .contains( " " + modifyActionValue + " " )))
         {
            // Not Sysroute
            return false; // do NOT validate
         }
         else if (sysrouteTypeValue == null || sysrouteTypeValue.isEmpty()
               || !(validateSysrouteType
                     .contains( " " + sysrouteTypeValue + " " )))
         {
            // Sysroute, but not to Closed APAR.
            return false; // do NOT validate
         }
         else
         {
            // Sysroute to Closed APAR.
            return true; // validate
         }
      }

      // Some other known attribute with no further filtering perhaps.
      return true;
   }

   /**
    * Validate any applicable release attributes and value format.
    * 
    * <ul>
    * <li>Cannot specify BOTH rrrY *AND* rrrN values.
    * <li>In case of some sort of error reporting as a "choice" (ex
    * regex error), must be 4 char selections.
    * </ul>
    * 
    * <br> (*) = Arguments passed by RTC via precondition front door.
    * @param validateAttribute (*) - attribute validation applied to.
    * @param validateAttrId
    * @param validateAttrName
    * @param workItem (*)
    * @param workItemCommon (*)
    * @param configuration (*) - may be null if from another plugin.
    * @param monitor (*)
    * @param pluginId - derived from configuration or supplied by
    *                   internal plugin.
    * @param sportCommonData
    * 
    * @return
    * @throws TeamRepositoryException
    */
   // The SeparatedStringList is apparently an internal RTC type, and
   // access is discouraged. But we need to use it here. So suppress
   // the warnings. Suppression also needed for import, so added to
   // head of class.
   // @SuppressWarnings("restriction")
   // Make methods "static" so an instance of this class is not
   // necessary. Attribute-specific methods may then be reused by
   // other plug-ins.
   public static IStatus validateApplicableReleases( IAttribute validateAttribute,
         String validateAttrId, String validateAttrName, IWorkItem workItem,
         IWorkItemCommon workItemCommon, IConfiguration configuration,
         IProgressMonitor monitor, String pluginId,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // This is how status is returned.
      String reasonMsg = validateAttrName + " is OK.";
      IStatus isValid = new Status( IStatus.OK, pluginId, reasonMsg );

      // We know what type this is supposed to be -
      String vAttrType = validateAttribute.getAttributeType();
      SeparatedStringList applReleases = (SeparatedStringList)validateAttribute
            .getValue( workItemCommon.getAuditableCommon(), workItem,
                  monitor );
      // Validate all choices are 4 chars [rrr(Y|N)].
      ArrayList<String> releasesSelected = new ArrayList<String>();
      String releasePart = "";
      releasePart = vAttrType; // test
      String applReleasePattern = "rrrX"; // rrr(Y|N)
      if (applReleases != null && ! applReleases.isEmpty())
      {
         // Validate each selection / choice.
         // Collect ALL errors to report (on all choices).
         // Collect length-related issues.
         ArrayList<String> lengthErrors = new ArrayList<String>();
         // Collect Y/N releases.
         ArrayList<String> ynErrors = new ArrayList<String>();
         for (String applRelease : applReleases)
         {
            if (lengthErrors.contains( applRelease ))
            {
               // Already reported error for release, nnnnext please.
               continue;
            }
            // Validate selection length.
            isValid = validateLength( validateAttribute, validateAttrId,
                  validateAttrName, workItem, workItemCommon, configuration,
                  monitor, pluginId, applRelease, applReleasePattern.length(),
                  "=", sportCommonData );
            if (!(isValid.isOK()))
            {
               // Length error found, report  / collect that.
               lengthErrors.add( applRelease );
               // Get next release choice.
               continue;
            }
            // Valid length, store rrr portion for Y AND N test.
            // Note: RTC does not allow duplicate choices.
            // SPoRT does not allow manual entry, so no need to handle
            // case sensitivity.
            releasePart = applRelease.substring( 0, 3 );
            if (ynErrors.contains( releasePart ))
            {
               // Already reported error for release, nnnnext please.
               continue;
            }
            if (releasesSelected.contains( releasePart ))
            {
               // Y AND N selected - report  / collect ERROR
               ynErrors.add( releasePart );
               continue;
            }
            releasesSelected.add( releasePart );
         }
         // Report all errors collected.
         // RTC precondition already prefaces with -
         // "Invalid value of attribute '<validateAttrName>' in work item
         // '<id>':".
         // No need to be redundant.
         // String reasonMsg = "Error in " + validateAttrName + ".";
         reasonMsg = "";
         // Even though we test for length-validity 1st, report Y/N
         // errors 1st, as these may be more common.
         if (!ynErrors.isEmpty())
         {
            if (reasonMsg.isEmpty())
               reasonMsg += "Invalid choice(s).";
            // All Y AND N errors -
            reasonMsg += System.lineSeparator(); // Newline
            reasonMsg += "Both Y and N chosen for " + ynErrors.toString()
                  + ". Please remove one and try again.";
         }
         if (!lengthErrors.isEmpty())
         {
            if (reasonMsg.isEmpty())
               reasonMsg += "Invalid choice(s).";
            // All length errors -
            reasonMsg += System.lineSeparator(); // Newline
            reasonMsg += "Length not = 4: " + lengthErrors.toString() + ".";
            // + testCondition + " " + String.valueOf( testLength ) + ".";
            // isValid = new Status( IStatus.ERROR, pluginId, reasonMsg );
         }
         if (!reasonMsg.isEmpty())
            isValid = new Status( IStatus.ERROR, pluginId, reasonMsg );
         /*
         // Not necessary, if there was NO error, isValid will remain
         // as OK.
         else
         {
            // Reset valid.
            reasonMsg = validateAttrName + " is OK.";
            isValid = new Status( IStatus.OK, pluginId, reasonMsg );
         }
         */
      }

      return isValid;
   }

   /**
    * Common method to test the length of a string value.
    * Condition to test passed as argument.
    * 
    * <br> (*) = Arguments passed by RTC via precondition front door.
    * @param validateAttribute (*) - attribute validation applied to.
    * @param validateAttrId
    * @param validateAttrName
    * @param workItem (*)
    * @param workItemCommon (*)
    * @param configuration (*) - may be null if from another plugin.
    * @param monitor (*)
    * @param pluginId - derived from configuration or supplied by
    *                   internal plugin.
    * @param testValue (String only)
    * @param testLength
    * @param testCondition (combo of) "<", ">", "=", "!" (negation).
    * As in "<=" / ">=" means less/greater OR equal to.
    * "!=" means NOT EQUAL.
    * Both "<" and ">" together are ignored (dont do that).
    * @param sportCommonData
    * 
    * @return
    * @throws TeamRepositoryException
    */
   // Make methods "static" so an instance of this class is not
   // necessary. Attribute-specific methods may then be reused by
   // other plug-ins.
   public static IStatus validateLength( IAttribute validateAttribute,
         String validateAttrId, String validateAttrName, IWorkItem workItem,
         IWorkItemCommon workItemCommon, IConfiguration configuration,
         IProgressMonitor monitor, String pluginId, 
         String testValue, int testLength, String testCondition,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // Prime error reason.
      // RTC precondition already prefaces with -
      // "Invalid value of attribute '<validateAttrName>' in work item
      // '<id>':".
      // No need to be redundant.
      // String reasonMsg = "Error in " + validateAttrName + ".";
      String reasonMsg = "Invalid value [" + testValue + "]. Length not "
            + testCondition + " " + String.valueOf( testLength ) + ".";

      // This is how status is returned. Assume error; guilty until
      // proven valid.
      IStatus isValid = new Status( IStatus.ERROR, pluginId, reasonMsg );
      // Null values cannot match any length, and even in this case, 0.
      // Caller may test for null before hand.
      if (testValue != null)
      {
         // Match <, >, <=, >=, =, or != (ie NOT equal)
         // Actually negation ("!") is post-applied to any other
         // condition.
         // Break the logic down -
         // value.len < len
         // as long as <> (both) also NOT specified, but who'd do that?
         // Dont be "that" guy.
         if (!(isValid.isOK()) && 
               (testCondition.contains( "<" )
               && !(testCondition.contains( ">" ))
               && testValue.length() < testLength)
               )
         {
            isValid = new Status( IStatus.OK, pluginId,
                  validateAttrName + " length is OK." );
         }
         // value.len > len
         // as long as <> (both) also NOT specified, but who'd do that?
         // Dont be "that" guy.
         if (!(isValid.isOK()) && 
               (testCondition.contains( ">" )
               && !(testCondition.contains( "<" ))
               && testValue.length() > testLength)
               )
         {
            isValid = new Status( IStatus.OK, pluginId,
                  validateAttrName + " length is OK." );
         }
         // In conjunction with above logic, handles <=, >= or =
         if (!(isValid.isOK()) && 
               (testCondition.contains( "=" )
               && testValue.length() == testLength)
               )
         {
            isValid = new Status( IStatus.OK, pluginId,
                  validateAttrName + " length is OK." );
         }
         // Negation, return to error status if it was deemed OK above.
         if (isValid.isOK() && testCondition.contains( "!" ))
         {
            // Reuse established error msg reason.
            isValid = new Status( IStatus.ERROR, pluginId, reasonMsg );
         }
      }
      /*
      else
      {
         // If null should match != (NOT EQUALS) ? TBD
         if (testCondition.contains( "!" ) && testCondition.contains( "=" ))
         {
            isValid = new Status( IStatus.OK, pluginId,
                  validateAttrName + " length is OK." );
         }
      }
      */

      return isValid;
   }

   /**
    * Common method to test a string value against a regular
    * expression passed as argument.
    * 
    * <br> (*) = Arguments passed by RTC via precondition front door.
    * @param validateAttribute (*) - attribute validation applied to.
    * @param validateAttrId
    * @param validateAttrName
    * @param workItem (*)
    * @param workItemCommon (*)
    * @param configuration (*) - may be null if from another plugin.
    * @param monitor (*)
    * @param pluginId - derived from configuration or supplied by
    *                   internal plugin.
    * @param testValue (String only)
    * @param testExpression - Regular expression to test.
    * @param additionalReason - Additional reason to explain to user
    * details of failing the expression.
    * @param sportCommonData
    * 
    * @return
    * @throws TeamRepositoryException
    */
   // Make methods "static" so an instance of this class is not
   // necessary. Attribute-specific methods may then be reused by
   // other plug-ins.
   public static IStatus validateMatches( IAttribute validateAttribute,
         String validateAttrId, String validateAttrName, IWorkItem workItem,
         IWorkItemCommon workItemCommon, IConfiguration configuration,
         IProgressMonitor monitor, String pluginId, 
         String testValue, String testExpression,
         String additionalReason,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // Prime error reason.
      String reasonMsg = validateAttrName + " is OK.";
      IStatus isValid = new Status( IStatus.OK, pluginId, reasonMsg );
      
      try
      {
         if (!(testValue.matches( testExpression )))
         {
            // RTC precondition already prefaces with -
            // "Invalid value of attribute '<validateAttrName>' in
            // work item '<id>':".
            // No need to be redundant.
            // reasonMsg = "Error in " + validateAttrName + ".";
            reasonMsg = "Invalid value [" + testValue + "]. "
                  + additionalReason;
            isValid = new Status( IStatus.ERROR, pluginId, reasonMsg );
         }
      }
      catch (Exception e)
      {
         // Catch a potential regex exception and just indicate
         // regex error.
         reasonMsg = "Exception encountered with expression: "
               + e.getMessage();
         isValid = new Status( IStatus.ERROR, pluginId, reasonMsg );
      }

      return isValid;
   }
}
