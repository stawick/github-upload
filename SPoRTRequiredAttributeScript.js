dojo.provide("com.ibm.sport.rtc.common.SPoRTRequiredAttributeScript");
dojo.require("com.ibm.team.workitem.api.common.WorkItemAttributes");

(function() {
   // Obtain built-in attributes
   var WorkItemAttributes= com.ibm.team.workitem.api.common.WorkItemAttributes;

dojo.declare("com.ibm.sport.rtc.common.SPoRTRequiredAttributeScript", null, {

   matches: function(workItem, configuration) {
      
      var requireAttr = false;
      
      // Determine whether attribute(s) should be required -
      // Generalized script to be used by Conditions that determine attribute requirement.   
      // The process configuration for the Condition will define ALL the attribute
      // references and "logic" to make the determination.
      // This script is more of a parser of those configuration-defined conditions.
      
      // Each Condition logic config entry is named "SPoRTRTCCondition" 
      // and contains the elements:
      // attrID - ID of the attribute to query (must be string-like for testing)
      //    - ["*ACTION*" = special value to use the current action ]
      //    - A builtin state attribute reference will obtain the current STATE
      //    - A builtin type attribute reference will obtain the workitem TYPE
      //    - *** attribute will be matched to a STRING value ***
      // attrTYPE - attribute type, (optional) 
      //    - defaults to "STRING" initially (should also use STRING for enums)
      //      (overridden by DEFAULT TYPE entry)
      //    - Non-string types are only allowed if they implement a toString method
      //      (The intent is to allow boolean attributes to be tested)
      // type - MATCH, or NOTMATCH
      //    - Also DEFAULT = defines the default attribute to test
      //      as identified by the attrID and attrTYPE of the entry.
      //      No matching is performed. Allows attr to be set "globally".
      //    _F26063A
      //    - Also ENABLED_ATTRIBUTE = indicates to use reverse logic for read-only Conditions.
      //      The idea is to use the Dynamic Read-Only Rules, but specify the attributes
      //      that should be modifiable, and then apply reverse logic on return.
      //      So when a true condition is deduced, false gets returned to turn the 
      //      read-only property OFF for the attribute (and vice-versa when false determined). 
      //    _F67072A
      //    - Also CONTAINSKEY, or NOTCONTAINSKEY
      //      Whereas [NOT]MATCH will test for the existence of the attribute 
      //      value within the supplied test valueMatch, this will do the 
      //      reverse. The attribute will be assumed to be a blank-delimited 
      //      set of keywords. The existence of the supplied test valueMatch 
      //      will be tested within the attribute value.
      // valueMatch - the value the work item attribute should be checked against
      //    - Multiple unique values can be specified, as an "OR" condition,
      //      but must be separated by 1 or more blanks to insure non-partial matches.
      //    - A NOTMATCH of multiple values means the attribute does NOT match any of the values.
      //    - "*any*" matches any value
      //    - "*none*" matches when the attribute is empty
      //    - A special STATE value of "*CREATE_STATE*" is used internally to represent the condition
      //      when a work item is being created (empty state). Users can use this value for
      //      testing in the configuration element.
      //    _F212079A
      //    - *** Overrides attrMatchID value if also specified.
      // _F212079A
      // attrMatchID - ID of an optional attribute to test the attrID value against
      // (must be string-like for testing, valueMatch string overrides if also specified.)
      //    - *** Special built-ins like ACTION, State, or workitem Type are IGNORED
      //      and MUST be specified on the attrID parameter!
      //    - Same as with the valueMatch parameter value, multiple unique values can be specified
      //      within the attribute's data, as an "OR" condition, but must be separated by
      //      1 or more blanks to insure non-partial matches. This is to allow matching a single-value
      //      attribute with a blank-delimited multi-value container.
      //    - A NOTMATCH of multiple values in attrMatchID means the attrID attribute does NOT match
      //      any of the attrMatchID values.
      //    - *** Unlike valueMatch, cannot rely on "special values" contained within the attrMatchID
      //      value (ex "*any*" or "*none*"). In this case, valueMatch must be employed.
      //    - *** There is no special corresponding attrTYPE parameter. It is assumed that the type
      //      is consistent with the attrID value!
      // If ANY of the conditions match, the attribute(s) are deemed required.
      // (Multiple conditions OR'd together.) 
      // 
      // The parent SPoRTRTCCondition entries may also optionally contain 
      // sub-condition logic entries, based on string-capable value attributes
      // (ex, strings with 1 contiguous value, enumerations, - attributes that do not
      // fit this MUST implement the toString conversion method and the user MUST specify
      // the attrTYPE. For example, use of booleans must do this.)
      // andCondition entry - child(ren) of SPoRTRTCCondition.
      //    - ALL conditions must result in a match.
      // orCondition entry - child(ren) of andCondition entry.
      //    - At least 1 condition must match
      // orCondition sub-elements include:
      // attrID - ID of the attribute to query (must be string-like for testing)
      //    - ["*ACTION*" = special value to use the current action ]
      //    - A builtin state attribute reference will obtain the current STATE
      //    - A builtin type attribute reference will obtain the workitem TYPE
      //    - *** attribute will be matched to a STRING value ***
      // attrTYPE - attribute type, (optional) 
      //    - defaults to "STRING" (should also use STRING for enums)
      //    - Non-string types are only allowed if they implement a toString method
      //      (The intent is to allow boolean attributes to be tested)
      // type - MATCH, or NOTMATCH
      //    _F67072A
      //    - Also CONTAINSKEY, or NOTCONTAINSKEY
      //      Whereas [NOT]MATCH will test for the existence of the attribute 
      //      value within the supplied test valueMatch, this will do the 
      //      reverse. The attribute will be assumed to be a blank-delimited 
      //      set of keywords. The existence of the supplied test valueMatch 
      //      will be tested within the attribute value.
      // valueMatch - the value the work item attribute should be checked against
      //    - Multiple unique values can be specified, as an "OR" condition, 
      //      but must be separated by 1 or more blanks to insure non-partial matches.
      //    - A NOTMATCH of multiple values means the attribute does NOT match any of the values.
      //    - "*any*" matches any value
      //    - "*none*" matches when the attribute is empty 
      //    - A special STATE value of "*CREATE_STATE*" is used internally to represent the condition 
      //      when a work item is being created (empty state). Users can use this value for
      //      testing in the configuration element.
      //    - *** attribute will be matched to a STRING value ***
      //    _F212079A
      //    - *** Overrides attrMatchID value if also specified.
      // _F212079A
      // attrMatchID - ID of an optional attribute to test the attrID value against
      // (must be string-like for testing, valueMatch string overrides if also specified.)
      //    - *** Special built-ins like ACTION, State, or workitem Type are IGNORED
      //      and MUST be specified on the attrID parameter!
      //    - Same as with the valueMatch parameter value, multiple unique values can be specified
      //      within the attribute's data, as an "OR" condition, but must be separated by
      //      1 or more blanks to insure non-partial matches. This is to allow matching a single-value
      //      attribute with a blank-delimited multi-value container.
      //    - A NOTMATCH of multiple values in attrMatchID means the attrID attribute does NOT match
      //      any of the attrMatchID values.
      //    - *** Unlike valueMatch, cannot rely on "special values" contained within the attrMatchID
      //      value (ex "*any*" or "*none*"). In this case, valueMatch must be employed.
      //    - *** There is no special corresponding attrTYPE parameter. It is assumed that the type
      //      is consistent with the attrID value!
      
      // Configuration definitions - 
      // Parent
      var parentConfigEntry = "SPoRTRTCCondition";
      var attrConfigElem = "attrID";
      var attrTypeConfigElem = "attrTYPE"; // STRING or not
      var typeConfigElem = "type";
      var matchConfigElem = "valueMatch";
      // _F212079A Attribute matching.
      // attrID [NOT]MATCHES attrMatchID (in lieu of valueMatch).
      // Assumes attrTYPE the same between both attributes.
      var attrMatchIDConfigElem = "attrMatchID";
      // Child of parent entry - 
      var andConfigEntry = "andCondition";
      var orConfigEntry = "orCondition";
      // Constants - 
      // Attribute-related
      var builtinActionAttr = "*ACTION*"; // Not defined to RTC, use special value
      var builtinActionType = "STRING"; // More like enum, but this works here
      var builtinStateAttr = "com.ibm.team.workitem.attribute.state";
      var builtinStateType = "STRING"; // More like enum, but this works here
      var builtinTypeAttr = "com.ibm.team.workitem.attribute.workitemtype";
      var builtinTypeType = "STRING"; // More like enum, but this works here
      var createState = "*CREATE_STATE*"; // Special defined value
      // Match condition-related
      var defaultConfigType = "DEFAULT";
      var defaultWIAttrID = ""; // Set by DEFAULT config TYPE entry
      var defaultWIAttrTYPE = "STRING"; // (Re)Set by DEFAULT config TYPE entry
      var enableAttributeLogic = "ENABLED_ATTRIBUTE"; // _F26063A
      var readOnlyReverseProcessing = "false"; // _F26063A
      var matchType = "MATCH";
      var notMatchType = "NOT" + matchType;
      // _F67072A Matching when the attribute has multiple blank-delimited
      // keywords, and want to test for a single keyword
      var containsKeyType = "CONTAINSKEY"; // _F67072A
      var notContainsKeyType = "NOT" + containsKeyType; // _F67072A
      var keyLogic = " " + containsKeyType + 
                     " " + notContainsKeyType + 
                     " "; // _F67072A
      var switchLogic = " " + notMatchType + 
                        " " + notContainsKeyType + 
                        " "; // _F67072A
      var matchAll = "*any*";
      var matchNone = "*none*"; // For matching in sub-conditions only
      var notSpecified = "*unspecified*";
      var stringAttrType = "STRING"; // Also use for enums
      // Known SPoRT work item types
      var sportAPARworkItemType = "com.ibm.sport.rtc.workItem.type.apar";
      var sportPMRworkItemType = "com.ibm.sport.rtc.workItem.type.pmr";
      var sportPTFworkItemType = "com.ibm.sport.rtc.workItem.type.ptf";
      var sportLCworkItemType = "com.ibm.sport.rtc.workItem.type.legalchecklist";
      // _T88014A Call Queue feature
      var sportCallQworkItemType = "com.ibm.sport.rtc.workItem.type.callqueue";
      var knownSPoRTworkItems = " " + sportAPARworkItemType +
                                " " + sportPMRworkItemType +
                                " " + sportPTFworkItemType + 
                                " " + sportLCworkItemType +
                                " " + sportCallQworkItemType + 
                                " ";
      // Match up provider with work item type - match in upper case
      var conditionProviderID = configuration.getStringDefault("id", notSpecified);
      // Based on naming convention
      var builtinProviderPrefix = "com.ibm.sport.rtc.team.workitem.valueproviders.CONDITION.SPoRTBuiltin";
      var aparProviderPrefix = "com.ibm.sport.rtc.team.workitem.valueproviders.CONDITION.SPoRTApar";
      var pmrProviderPrefix = "com.ibm.sport.rtc.team.workitem.valueproviders.CONDITION.SPoRTPmr";
      var ptfProviderPrefix = "com.ibm.sport.rtc.team.workitem.valueproviders.CONDITION.SPoRTPtf";
      var legalchecklistProviderPrefix = "com.ibm.sport.rtc.team.workitem.valueproviders.CONDITION.SPoRTLegalchecklist";
      // _T88014A Call Queue feature
      var callqueueProviderPrefix = "com.ibm.sport.rtc.team.workitem.valueproviders.CONDITION.SPoRTCallQ";

      // Work item attribute derivation vars - 
      var wiAttrStringValue = "";
      var attrsObtained = []; // Key (typically attr ID), Value pair
      // Config element data - 
      var wiAttrId = ""; // attrID
      var wiAttrType = ""; // attrTYPE
      var configTestType = ""; // type
      var configMatchValue = ""; // valueMatch
      // _F212079A Attribute matching.
      var wiAttrMatchId = "";
      var configAttrMatchValue = ""; // attrMatchID value
      
      // _F26063A - Required or Read-only bypass
      // Prior to actual logic processing, see if work item has 
      // attribute(s) set to bypass required or read-only validation
      // Also bypass for non-SPoRT work item type. (for performance in eclipse)
      var bypassValidation = false;
      var sportBypassAttrName = "sportInternalWorkitemSave";
      var sbsBypassAttrName = "sbsSave";
      wiAttrId = builtinTypeAttr; // Need type to get proper attribute
      // Obtain TYPE (builtin)
      wiAttrStringValue = workItem.getValue(WorkItemAttributes.TYPE);
      if (wiAttrStringValue == null || wiAttrStringValue.length == 0)
      {
         // If type is empty (unlikely), set a value to avoid (or allow) a match
         wiAttrStringValue = notSpecified; // User can test for this special value
      }
      wiAttrType = builtinTypeType; // Enforce string compare
      // Save in case referred to in logic - 
      // For non-partial matching purposes, surround attribute value with blanks
      attrsObtained[wiAttrId] = " " + wiAttrStringValue + " ";
      // If non-SPoRT work item, bypass SPoRT required or read-only processing
      // (Conditions are executed by RTC for any work item)
      if (knownSPoRTworkItems.indexOf(attrsObtained[wiAttrId]) < 0)
      {
         return requireAttr; // Already preset as false
      }
      // Else - Known SPoRT work item
      
      // Now match up Condition provider ID with work item type
      // Based on naming convention
      // Builtin applies to all SPoRT types
      // Case-cancelling, in case developer mistypes ID 
      if (conditionProviderID.toUpperCase().indexOf(builtinProviderPrefix.toUpperCase()) != 0)
      {
         // Not a Builtin Condition provider
         if (conditionProviderID.toUpperCase().indexOf(aparProviderPrefix.toUpperCase()) == 0)
         {
            // APAR provider
            if (attrsObtained[wiAttrId] != " " + sportAPARworkItemType + " ")
            {
               // Not an APAR work item - bypass logic test
               return requireAttr; // Already preset as false
            }
         }
         else if (conditionProviderID.toUpperCase().indexOf(pmrProviderPrefix.toUpperCase()) == 0)
         {
            // PMR provider
            if (attrsObtained[wiAttrId] != " " + sportPMRworkItemType + " ")
            {
               // Not a PMR work item - bypass logic test
               return requireAttr; // Already preset as false
            }
         }
         else if (conditionProviderID.toUpperCase().indexOf(ptfProviderPrefix.toUpperCase()) == 0)
         {
            // PTF provider
            if (attrsObtained[wiAttrId] != " " + sportPTFworkItemType + " ")
            {
               // Not a PTF work item - bypass logic test
               return requireAttr; // Already preset as false
            }
         }
         else if (conditionProviderID.toUpperCase().indexOf(legalchecklistProviderPrefix.toUpperCase()) == 0)
         {
            // Legalchecklist provider
            if (attrsObtained[wiAttrId] != " " + sportLCworkItemType + " ")
            {
               // Not a Legalchecklist work item - bypass logic test
               return requireAttr; // Already preset as false
            }
         }
         else if (conditionProviderID.toUpperCase().indexOf(callqueueProviderPrefix.toUpperCase()) == 0)
         {
            // _T88014A Call Queue provider
            if (attrsObtained[wiAttrId] != " " + sportCallQworkItemType + " ")
            {
               // Not a Call Queue work item - bypass logic test
               return requireAttr; // Already preset as false
            }
         }
      }
      
      // Else - Builtin or work item type-appropriate Condition
      
      // Build attribute from type - replace ".workItem.type." with
      // ".workItem.attribute.", then append "." + attr name
      // Check for internal SPoRT save case
      wiAttrId = wiAttrStringValue.replace( ".workItem.type.", ".workItem.attribute.");
      wiAttrId += "." + sportBypassAttrName;
      wiAttrType = "boolean"; // Defined as a boolean
      if (workItem.isAttributeSet(wiAttrId))
      {
         bypassValidation = workItem.getValue(wiAttrId);
         // Internal use only, no need to save value in attrsObtained table
      }
      // When bypassing either Required or Read-only determination,
      // BOTH cases should return FALSE:
      // Required: False === NOT required
      // Read-only: False === NOT read-only, ie ENABLED 
      // if (bypassValidation)
      // Web seems to return value as string, so need to convert - 
      if (bypassValidation.toString() == "true")
      {
         return requireAttr; // Already preset as false
      }
      // Also check for SBS save case, if needed
      wiAttrId = wiAttrStringValue.replace( ".workItem.type.", ".workItem.attribute.");
      wiAttrId += "." + sbsBypassAttrName;
      wiAttrType = "boolean"; // Defined as a boolean
      if (workItem.isAttributeSet(wiAttrId))
      {
         bypassValidation = workItem.getValue(wiAttrId);
         // Internal use only, no need to save value in attrsObtained table
      }
      // When bypassing either Required or Read-only determination,
      // BOTH cases should return FALSE:
      // Required: False === NOT required
      // Read-only: False === NOT read-only, ie ENABLED 
      // if (bypassValidation)
      // Web seems to return value as string, so need to convert - 
      if (bypassValidation.toString() == "true")
      {
         return requireAttr; // Already preset as false
      }
      // Else - proceed on to determine logic
      
      // Parent config containing list of required conditions
      var sportConditions = configuration.getChildren(parentConfigEntry);
      for (var sportCondI=0; sportCondI < sportConditions.length; sportCondI++)
      {
         var sportCondition = sportConditions[sportCondI]; // Current condiiton
         // attrID=... (and attrTYPE)
         wiAttrId = sportCondition.getStringDefault(attrConfigElem, defaultWIAttrID);
         wiAttrType = sportCondition.getStringDefault(attrTypeConfigElem, defaultWIAttrTYPE);
         // _F212079A Attribute matching.
         // Optional attrMatchID=... (assume same attrTYPE!)
         wiAttrMatchId = sportCondition.getStringDefault(attrMatchIDConfigElem, notSpecified);
         // type={[NOT](MATCH | CONTAINSKEY)} 
         configTestType = sportCondition.getStringDefault(typeConfigElem, matchType);
         
         if (configTestType == defaultConfigType)
         {
            // This entry simply defines the default work item ID/type
            if (wiAttrId.length > 0)
            {
               defaultWIAttrID = wiAttrId;
               if (wiAttrType.length > 0)
               {
                  defaultWIAttrTYPE = wiAttrType;
               }
            }
            continue; // Next condition entry 
         }
         
         // _F26063A Read-Only requested?
         if (configTestType == enableAttributeLogic)
         {
            // Indicate to return the reverse condition
            readOnlyReverseProcessing = "true";
            continue; // Next condition entry 
         }
         
         // All other condition types - 
         
         // Obtain the attrID= attribute value (or reuse obtained value)
         if (wiAttrId.length > 0)
         {
            if (attrsObtained[wiAttrId] == null || attrsObtained[wiAttrId].length == 0)
            {
               // Set default to not specified
               wiAttrStringValue = notSpecified;
               if (wiAttrId == builtinActionAttr)
               {
                  // Obtain ACTION
                  // NOTE: Action from configuration object will only be available
                  // for required attribute Condition provider (per RTC doc).
                  wiAttrStringValue = configuration.getWorkflowAction();
                  if (wiAttrStringValue == null || wiAttrStringValue.length == 0)
                  {
                     wiAttrStringValue = notSpecified;
                  }
                  wiAttrType = builtinActionType; // Enforce string compare
               }
               else if (wiAttrId == builtinStateAttr)
               {
                  // Obtain STATE
                  wiAttrStringValue = workItem.getValue(WorkItemAttributes.STATE);
                  if (wiAttrStringValue == null || wiAttrStringValue.length == 0)
                  {
                     // On a create, state may be empty. Set a value to avoid (or allow) a match
                     wiAttrStringValue = createState; // User can test for this special value
                  }
                  wiAttrType = builtinStateType; // Enforce string compare
               }
               else if (wiAttrId == builtinTypeAttr)
               {
                  // Obtain TYPE
                  wiAttrStringValue = workItem.getValue(WorkItemAttributes.TYPE);
                  if (wiAttrStringValue == null || wiAttrStringValue.length == 0)
                  {
                     // If type is empty (unlikely), set a value to avoid (or allow) a match
                     wiAttrStringValue = notSpecified; // User can test for this special value
                  }
                  wiAttrType = builtinTypeType; // Enforce string compare
               }
               else if (workItem.isAttributeSet(wiAttrId))
               {
                  if (wiAttrType == stringAttrType)
                  {
                     // String - 
                     wiAttrStringValue = workItem.getValue(wiAttrId); // Assume string attributes
                  }
                  else
                  {
                     // Non-string, assume toString() available
                     var wiAttrNativeValue = workItem.getValue(wiAttrId);
                     wiAttrStringValue = wiAttrNativeValue.toString(); 
                  }
                  if (wiAttrStringValue == null || wiAttrStringValue.length == 0)
                  {
                     // Somehow can still have a string attribute "set" with empty value
                     wiAttrStringValue = notSpecified;
                  }
               }
               // For non-partial matching purposes, surround attribute value with blanks
               attrsObtained[wiAttrId] = " " + wiAttrStringValue + " ";
            }
            
            // _F212079A Attribute matching - optional attrMatchID=...
            configAttrMatchValue = matchAll; // When nothing used, match to anything.
            if (wiAttrMatchId != notSpecified)
            {
            	// Get attrMatchID attribute value.
                if (attrsObtained[wiAttrMatchId] == null || attrsObtained[wiAttrMatchId].length == 0)
                {
                    // Set default to not specified
                    wiAttrStringValue = notSpecified;
                   // Built-ins are not recognized for the attrMatchID.
                   // Assume Action, State, Type, etc are single occurrence
                   // attributes and will be valueMatch-ed.
                   if (workItem.isAttributeSet(wiAttrMatchId))
                   {
                      if (wiAttrType == stringAttrType)
                      {
                         // String - 
                         wiAttrStringValue = workItem.getValue(wiAttrMatchId); // Assume string attributes
                      }
                      else
                      {
                         // Non-string, assume toString() available
                         var wiAttrNativeValue = workItem.getValue(wiAttrMatchId);
                         wiAttrStringValue = wiAttrNativeValue.toString(); 
                      }
                      if (wiAttrStringValue == null || wiAttrStringValue.length == 0)
                      {
                         // Somehow can still have a string attribute "set" with empty value
                         wiAttrStringValue = notSpecified;
                      }
                   }
                   // For non-partial matching purposes, surround attribute value with blanks
                   attrsObtained[wiAttrMatchId] = " " + wiAttrStringValue + " ";
                }
                // Set to obtained or saved attribute value (with delimiters).
                // Allows for subset / element containment matching between attributes
                // like with valueMatch.
                configAttrMatchValue = attrsObtained[wiAttrMatchId];
            }
            
            // _F212079C valueMatch | optional attrMatchID
            // Use valueMatch to test if specified [override], otherwise
            // test with attrMatchID value (or matchAll, default).
            // This will use a not-specified default if attrMatchID specified,
            // but no such value in work item.
            configMatchValue = sportCondition.getStringDefault(matchConfigElem, configAttrMatchValue);
            // _F212079M Move valueMatch transformation code here
            if (configMatchValue == matchNone)
            {
               // Force to match with unspecified attribute value
               configMatchValue = notSpecified;
            }
            // For non-partial matching purposes, add a blank to 1st/last value
            // if not already done via attrMatchID value.
            if (configMatchValue != matchAll &&
            		configAttrMatchValue == matchAll)
            {
               configMatchValue = " " + configMatchValue + " ";
            }
            
            // Now check the attribute vs the config values
            // _F67072C When keyword test is requested, test that the 
            // attribute value contains the supplied keyword.
            // Else test supplied value contains the attribute value.
            requireAttr = (configMatchValue == matchAll ||
     		               (keyLogic.indexOf(" " + configTestType + " ") < 0
     		            		   && configMatchValue.indexOf(
     		            				   attrsObtained[wiAttrId]) > -1
    		    	       ) ||
            		       (keyLogic.indexOf(" " + configTestType + " ") > -1
            		    		   && attrsObtained[wiAttrId].
            		    				   indexOf(configMatchValue) > -1
            		       )
            		      )
            // _F67072C Negated logic test (ie NOT*)
            if (switchLogic.indexOf(" " + configTestType + " ") > -1)
            {
               requireAttr = !requireAttr;
            }
         } // End - work item attr ID specified
         else
         {
            // On the main condition, if no attribute specified - 
            // presume user wants results to occur without any test performed
            requireAttr = true;
         }
            
         // Found a match on primary condition ...
         // Check if any AND/OR sub conditions defined based on attributes
         if (requireAttr)
         {
            // See if sub-conditions exist
            // If there is a match, process the further attribute-condition elements
            var andConditions = sportCondition.getChildren(andConfigEntry);
            for (var andI=0; andI < andConditions.length; andI++)
            {
               var andCondition = andConditions[andI];
               var orConditions = andCondition.getChildren(orConfigEntry);
               for (var orI=0; orI < orConditions.length; orI++)
               {
                  // We have to find at least 1 true OR condition to match
                  requireAttr = false;
                  var orCondition = orConditions[orI];
                  // attrID=... (and attrTYPE)
                  wiAttrId = orCondition.getStringDefault(attrConfigElem, defaultWIAttrID);
                  wiAttrType = orCondition.getStringDefault(attrTypeConfigElem, defaultWIAttrTYPE);
                  // _F212079A Attribute matching.
                  // Optional attrMatchID=... (assume same attrTYPE!)
                  wiAttrMatchId = orCondition.getStringDefault(attrMatchIDConfigElem, notSpecified);
                  // type={[NOT](MATCH | CONTAINSKEY)} 
                  configTestType = orCondition.getStringDefault(typeConfigElem, matchType);
                  
                  // Obtain the attrID attribute value (or reuse obtained value)
                  if (wiAttrId.length > 0)
                  {
                     if (attrsObtained[wiAttrId] == null || attrsObtained[wiAttrId].length == 0)
                     {
                        // Set default to not specified
                        wiAttrStringValue = notSpecified;
                        if (wiAttrId == builtinActionAttr)
                        {
                           // Obtain ACTION
                           // NOTE: Action from configuration object will only be available
                           // for required attribute Condition provider (per RTC doc).
                           wiAttrStringValue = configuration.getWorkflowAction();
                           if (wiAttrStringValue == null || wiAttrStringValue.length == 0)
                           {
                              wiAttrStringValue = notSpecified;
                           }
                           wiAttrType = builtinActionType; // Enforce string compare
                        }
                        else if (wiAttrId == builtinStateAttr)
                        {
                           // Obtain STATE
                           wiAttrStringValue = workItem.getValue(WorkItemAttributes.STATE);
                           if (wiAttrStringValue == null || wiAttrStringValue.length == 0)
                           {
                              // On a create, state may be empty. Set a value to avoid (or allow) a match
                              wiAttrStringValue = createState; // User can test for this special value
                           }
                           wiAttrType = builtinStateType; // Enforce string compare
                        }
                        else if (wiAttrId == builtinTypeAttr)
                        {
                           // Obtain TYPE
                           wiAttrStringValue = workItem.getValue(WorkItemAttributes.TYPE);
                           if (wiAttrStringValue == null || wiAttrStringValue.length == 0)
                           {
                              // If type is empty (unlikely), set a value to avoid (or allow) a match
                              wiAttrStringValue = notSpecified; // User can test for this special value
                           }
                           wiAttrType = builtinTypeType; // Enforce string compare
                        }
                        else if (workItem.isAttributeSet(wiAttrId))
                        {
                           if (wiAttrType == stringAttrType)
                           {
                              // String - 
                              wiAttrStringValue = workItem.getValue(wiAttrId); // Assume string attributes
                           }
                           else
                           {
                              // Non-string, assume toString() available
                              var wiAttrNativeValueOR = workItem.getValue(wiAttrId);
                              wiAttrStringValue = wiAttrNativeValueOR.toString(); 
                           }
                           if (wiAttrStringValue == null || wiAttrStringValue.length == 0)
                           {
                              wiAttrStringValue = notSpecified;
                           }
                        }
                        // For non-partial matching purposes, surround attribute value with blanks
                        attrsObtained[wiAttrId] = " " + wiAttrStringValue + " ";
                     }
                     
                     // _F212079A Attribute matching - optional attrMatchID=...
                     configAttrMatchValue = matchAll; // When nothing used, match to anything.
                     if (wiAttrMatchId != notSpecified)
                     {
                     	// Get attrMatchID attribute value.
                         if (attrsObtained[wiAttrMatchId] == null || attrsObtained[wiAttrMatchId].length == 0)
                         {
                             // Set default to not specified
                             wiAttrStringValue = notSpecified;
                            // Built-ins are not recognized for the attrMatchID.
                            // Assume Action, State, Type, etc are single occurrence
                            // attributes and will be valueMatch-ed.
                            if (workItem.isAttributeSet(wiAttrMatchId))
                            {
                               if (wiAttrType == stringAttrType)
                               {
                                  // String - 
                                  wiAttrStringValue = workItem.getValue(wiAttrMatchId); // Assume string attributes
                               }
                               else
                               {
                                  // Non-string, assume toString() available
                                  var wiAttrNativeValue = workItem.getValue(wiAttrMatchId);
                                  wiAttrStringValue = wiAttrNativeValue.toString(); 
                               }
                               if (wiAttrStringValue == null || wiAttrStringValue.length == 0)
                               {
                                  // Somehow can still have a string attribute "set" with empty value
                                  wiAttrStringValue = notSpecified;
                               }
                            }
                            // For non-partial matching purposes, surround attribute value with blanks
                            attrsObtained[wiAttrMatchId] = " " + wiAttrStringValue + " ";
                         }
                         // Set to obtained or saved attribute value (with delimiters).
                         // Allows for subset / element containment matching between attributes
                         // like with valueMatch.
                         configAttrMatchValue = attrsObtained[wiAttrMatchId];
                     }
                     
                     // _F212079C valueMatch | optional attrMatchID
                     // Use valueMatch to test if specified [override], otherwise
                     // test with attrMatchID value (or matchAll, default).
                     // This will use a not-specified default if attrMatchID specified,
                     // but no such value in work item.
                     configMatchValue = orCondition.getStringDefault(matchConfigElem, configAttrMatchValue);
                     // _F212079M Move valueMatch transformation code here
                     if (configMatchValue == matchNone)
                     {
                        // Force to match with unspecified attribute value
                        configMatchValue = notSpecified;
                     }
                     // For non-partial matching purposes, add a blank to 1st/last value
                     // if not already done via attrMatchID value.
                     if (configMatchValue != matchAll &&
                     		configAttrMatchValue == matchAll)
                     {
                        configMatchValue = " " + configMatchValue + " ";
                     }
                     
                     // Now check the attribute vs the config values
                     // _F67072C When keyword test is requested, test that the 
                     // attribute value contains the supplied keyword.
                     // Else test supplied value contains the attribute value.
                     requireAttr = (configMatchValue == matchAll ||
              		                (keyLogic.indexOf(
              		                		" " + configTestType + " ") < 0
              		                		&& configMatchValue.
              		                		indexOf(attrsObtained[wiAttrId])
              		                		> -1
             		    	        ) ||
                     		        (keyLogic.indexOf(
                     		        		" " + configTestType + " ") > -1
                     		        		&& attrsObtained[wiAttrId].
                     		        		indexOf(configMatchValue) 
                     		        		> -1
                     		        )
                     		       )
                     // _F67072C Negated logic test (ie NOT*)
                     if (switchLogic.indexOf(" " + configTestType + " ") > -1)
                     {
                        requireAttr = !requireAttr;
                     }
                      
                  } // END - work item attr ID specified
                  // On inner sub-conditions, no attribute does not presume a match.
                  // A no-op here is same as not determining "true".
                  if (requireAttr) 
                  {
                     break; // 1 true OR condition - short circuit
                  }

               } // End OR conditions
               if (!requireAttr)
               {
                  break; // 1 false AND condition - short circuit
               }
                   
            } // End AND conditions
         } // End Match on primary condition - check sub-conditions
         
         if (requireAttr) 
         {
            // Any/All sub-conditions passed
            break; // 1 true OR condition - short circuit
         }
         
      } // End primary conditions
      
      // _F26063A Read-Only reverse logic for enabled attributes
      // All of the above condition processing should take place as normal - 
      // to determine if the attribute should be optional / modifiable.
      // Return the OPPOSITE to set the read-only property correctly
      // (ie true for enabled === OFF for read-only
      // false for enabled === ON for read-only).
      if (readOnlyReverseProcessing == "true")
      {
         requireAttr = !requireAttr;
      }
      return requireAttr;
       
   },
   
   __sentinel: null
});

})();