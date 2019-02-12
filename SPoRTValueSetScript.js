dojo.provide("com.ibm.sport.rtc.common.SPoRTValueSetScript");
dojo.require("com.ibm.team.workitem.api.common.WorkItemAttributes");

(function() {
   // Obtain built-in attributes
   var WorkItemAttributes= com.ibm.team.workitem.api.common.WorkItemAttributes;

dojo.declare("com.ibm.sport.rtc.common.SPoRTValueSetScript", null, {

   getValueSet: function(attributeId, workItem, configuration) {
      
      var enumChoices = []; // Return values
      
      // Set enumeration choices based on conditions as defined in the configuration - 
      // Generalized means of deriving choices when the process configuration
      // defines ALL the enumeration references and "logic" to determine
      // the valid condition (STATE, in many cases) / choice set.
      // User could initially define a DEFAULT entry to point to the STATE attribute.
      // This script is more of a parser of those configuration-defined conditions.
      
      // Each Condition logic config entry is named "SPoRTRTCCondition" 
      // and contains the elements:
      // attrID - ID of the attribute to query (must be string-like for testing)
      //    - ["*ACTION*" = special value to use the current action ]
      //      NOTE: Action from configuration object will only be available
      //      for required attribute Condition provider (per RTC doc).
      //    - A builtin state attribute reference will obtain the current STATE
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
      // valueMatch - the value the work item attribute should be checked against
      //    - Multiple unique values can be specified, as an "OR" condition, 
      //      but must be separated by 1 or more blanks to insure non-partial matches.
      //    - A NOTMATCH of multiple values means the attribute does NOT match any of the values.
      //    - "*any*" matches any value
      //    - "*none*" matches when the attribute is empty 
      //    - A special STATE value of "*CREATE_STATE*" is used internally to represent the condition 
      //      when a work item is being created (empty state). Users can use this value for
      //      testing in the configuration element.
      // If ANY of the conditions match, the attribute(s) are deemed required.
      // (Multiple conditions OR'd together.) 
       
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
      //      NOTE: Action from configuration object will only be available
      //      for required attribute Condition provider (per RTC doc).
      //    - A builtin state attribute reference will obtain the current STATE
      //    - *** attribute will be matched to a STRING value ***
      // attrTYPE - attribute type, (optional) 
      //    - defaults to "STRING" (should also use STRING for enums)
      //    - Non-string types are only allowed if they implement a toString method
      //      (The intent is to allow boolean attributes to be tested)
      // type - MATCH, or NOTMATCH
      // valueMatch - the value the work item attribute should be checked against
      //    - Multiple unique values can be specified, as an "OR" condition, 
      //      but must be separated by 1 or more blanks to insure non-partial matches.
      //    - A NOTMATCH of multiple values means the attribute does NOT match any of the values.
      //    - "*any*" matches any value
      //    - "*none*" matches when the attribute is empty 
      //    - A special STATE value of "*CREATE_STATE*" is used internally to represent the condition 
      //      when a work item is being created (empty state). Users can use this value for
      //      testing in the configuration element.

      // Each parent condition entry will contain child elements with corresponding
      // enumeration choices:
      // "enumChoice" child entry, with "value" element
      // _F155269A add attrID= element with splitAt= to gather choices
      // from an attribute value split at the indicated value.
      
      // Configuration definitions - 
      // Parent - 
      var parentConfigEntry = "SPoRTRTCCondition";
      var attrConfigElem = "attrID";
      var attrTypeConfigElem = "attrTYPE"; // STRING or not
      var typeConfigElem = "type";
      var matchConfigElem = "valueMatch";
      // Child of parent entry - 
      var andConfigEntry = "andCondition";
      var orConfigEntry = "orCondition";
      var choicesConfigEntry = "enumChoice";
      var choiceValueConfigElem = "value";
      // _F155269A enumChoice attrID="..." [attrType="..."{String}] [splitAt="..."]
      var choiceAttrSplitConfigElem = "splitAt";
      var newlineString = "\\n"; // This is how "\n" comes in from the project area
      var realNewline = "\n"; // Single backslash for line break
      // Constants - 
      // Attribute-related
      var builtinActionAttr = "*ACTION*"; // Not defined to RTC, use special value
      var builtinActionType = "STRING"; // More like enum, but this works here
      var builtinStateAttr = "com.ibm.team.workitem.attribute.state";
      var builtinStateType = "STRING"; // More like enum, but this works here
      var createState = "*CREATE_STATE*"; // Special defined value
      // Match condition-related
      var defaultConfigType = "DEFAULT";
      var defaultWIAttrID = ""; // Set by DEFAULT config TYPE entry
      var defaultWIAttrTYPE = "STRING"; // (Re)Set by DEFAULT config TYPE entry
      var matchType = "MATCH";
      var notMatchType = "NOTMATCH";
      var matchAll = "*any*";
      var matchNone = "*none*"; // For matching in sub-conditions only
      var notSpecified = "*unspecified*";
      var stringAttrType = "STRING"; // Also use for enums
      
      // Work item attribute derivation vars - 
      var wiAttrStringValue = "";
      var attrsObtained = []; // Key (typically attr ID), Value pair
      // Config element data - 
      var wiAttrId = ""; // attrID
      var wiAttrType = ""; // attrTYPE
      var configTestType = ""; // type
      var configMatchValue = ""; // valueMatch
      
      var includeChoice = false;
      // Parent config containing list of required conditions
      var sportConditions = configuration.getChildren(parentConfigEntry);
      for (var sportCondI=0; sportCondI < sportConditions.length; sportCondI++)
      {
         var sportCondition = sportConditions[sportCondI]; // Current condiiton
         wiAttrId = sportCondition.getStringDefault(attrConfigElem, defaultWIAttrID);
         wiAttrType = sportCondition.getStringDefault(attrTypeConfigElem, defaultWIAttrTYPE);
         configTestType = sportCondition.getStringDefault(typeConfigElem, matchType);
         configMatchValue = sportCondition.getStringDefault(matchConfigElem, matchAll);
         
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
         
         // All other condition types - 
         if (configMatchValue == matchNone)
         {
            // Force to match with unspecified attribute value
            configMatchValue = notSpecified;
         }
         // For non-partial matching purposes, add a blank to 1st/last value
         if (configMatchValue != matchAll)
         {
            configMatchValue = " " + configMatchValue + " ";
         }
         // Obtain the attribute value (or reuse obtained value)
         if (wiAttrId.length > 0)
         {
            if (attrsObtained[wiAttrId] == null || attrsObtained[wiAttrId].length == 0)
            {
               // Set default to not specified
               wiAttrStringValue = notSpecified;
               if (wiAttrId == builtinActionAttr)
               {
                  // Obtain ACTION
                  // TBD - use not specified until available
                  // NOTE: Action from configuration object will only be available
                  // for required attribute Condition provider (per RTC doc).
                  // wiAttrStringValue = configuration.getWorkflowAction();
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
                     wiAttrStringValue = notSpecified;
                  }
               }
               // For non-partial matching purposes, surround attribute value with blanks
               attrsObtained[wiAttrId] = " " + wiAttrStringValue + " ";
            }
            // Now check the attribute vs the config values
            includeChoice = (configMatchValue.indexOf(attrsObtained[wiAttrId]) > -1 || 
                             configMatchValue == matchAll);
            if (configTestType == notMatchType)
            {
               includeChoice = !includeChoice;
            }
         } // End - work item attr ID specified
         else
         {
            // On the main condition, if no attribute specified - 
            // presume user wants results to occur without any test performed
            includeChoice = true;
         }
            
         // Found a match on primary condition ...
         // Check if any AND/OR sub conditions defined based on attributes
         if (includeChoice)
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
                  includeChoice = false;
                  var orCondition = orConditions[orI];
                  wiAttrId = orCondition.getStringDefault(attrConfigElem, defaultWIAttrID);
                  wiAttrType = orCondition.getStringDefault(attrTypeConfigElem, defaultWIAttrTYPE);
                  configTestType = orCondition.getStringDefault(typeConfigElem, matchType);
                  configMatchValue = orCondition.getStringDefault(matchConfigElem, matchAll);
      
                  if (configMatchValue == matchNone)
                  {
                     // Force to match with unspecified attribute value
                     configMatchValue = notSpecified;
                  }
                  // For non-partial matching purposes, add a blank to 1st/last value
                  if (configMatchValue != matchAll)
                  {
                     configMatchValue = " " + configMatchValue + " ";
                  }
                  // Obtain the attribute value (or reuse obtained value)
                  if (wiAttrId.length > 0)
                  {
                     if (attrsObtained[wiAttrId] == null || attrsObtained[wiAttrId].length == 0)
                     {
                        // Set default to not specified
                        wiAttrStringValue = notSpecified;
                        if (wiAttrId == builtinActionAttr)
                        {
                           // Obtain ACTION
                           // TBD - use not specified until available
                           // NOTE: Action from configuration object will only be available
                           // for required attribute Condition provider (per RTC doc).
                           // wiAttrStringValue = configuration.getWorkflowAction();
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
                     // Now check the attribute vs the config values
                     includeChoice = (configMatchValue.indexOf(attrsObtained[wiAttrId]) > -1 || 
                                      configMatchValue == matchAll);
                     if (configTestType == notMatchType)
                     {
                        includeChoice = !includeChoice;
                     }
                      
                  } // END - work item attr ID specified
                  // On inner sub-conditions, no attribute does not presume a match.
                  // A no-op here is same as not determining "true".
                  if (includeChoice) 
                  {
                     break; // 1 true OR condition - short circuit
                  }

               } // End OR conditions
               if (!includeChoice)
               {
                  break; // 1 false AND condition - short circuit
               }
                   
            } // End AND conditions
         } // End Match on primary condition - check sub-conditions
         
         // Now check the attribute vs the config values
         if (includeChoice) 
         {
            // If there is a match, process the child elements
            var getChoices = sportCondition.getChildren(choicesConfigEntry);
            for (var choiceI=0; choiceI < getChoices.length; choiceI++)
            {
            	// <enumChoice ( value="..." | (attrID="..." [attrType="..."{String}] [splitAt="..."]) )/>
               var returnChoice = getChoices[choiceI];
               var enumValue = returnChoice.getStringDefault(choiceValueConfigElem, "");
               var enumAttrID = returnChoice.getStringDefault(attrConfigElem, "");
               var enumAttrType = returnChoice.getStringDefault(attrTypeConfigElem, defaultWIAttrTYPE);
               var enumAttrValue = "";
               var enumSplitAt = returnChoice.getStringDefault(choiceAttrSplitConfigElem, "");
               // enumChoices.push(returnChoice.getString(choiceValueConfigElem));
               if (enumValue.length > 0 || enumAttrID.length == 0)
               {
            	  // Value supplied or attrID NOT specified 
                  enumChoices.push(enumValue);
               }
               // Obtain the attribute value (or reuse obtained value)
               if (enumAttrID.length > 0)
               {
                  if (attrsObtained[enumAttrID] == null || attrsObtained[enumAttrID].length == 0)
                  {
                     if (enumAttrType == builtinActionAttr)
                     {
                        // Obtain ACTION
                        // TBD - use not specified until available
                        // NOTE: Action from configuration object will only be available
                        // for required attribute Condition provider (per RTC doc).
                        // enumAttrValue = configuration.getWorkflowAction();
                        if (enumAttrValue == null || enumAttrValue.length == 0)
                        {
                        	// enumAttrValue = notSpecified;
                        }
                        enumAttrType = builtinActionType; // Enforce string compare
                     }
                     else if (enumAttrID == builtinStateAttr)
                     {
                        // Obtain STATE
                    	enumAttrValue = workItem.getValue(WorkItemAttributes.STATE);
                        if (enumAttrValue == null || enumAttrValue.length == 0)
                        {
                           // On a create, state may be empty. Set a value to avoid (or allow) a match
                        	enumAttrValue = createState; // User can test for this special value
                        }
                        enumAttrType = builtinStateType; // Enforce string compare
                     }
                     else if (workItem.isAttributeSet(enumAttrID))
                     {
                        if (enumAttrType == stringAttrType)
                        {
                           // String - 
                           enumAttrValue = workItem.getValue(enumAttrID); // Assume string attributes
                        }
                        else
                        {
                           // Non-string, assume toString() available
                           var enumAttrNativeValueOR = workItem.getValue(enumAttrID);
                           enumAttrValue = enumAttrNativeValueOR.toString(); 
                        }
                        if (enumAttrValue == null || enumAttrValue.length == 0)
                        {
                           // enumAttrValue = notSpecified;
                        }
                     }
                     // For non-partial matching purposes, surround attribute value with blanks
                     attrsObtained[enumAttrID] = " " + enumAttrValue + " ";
                  }
                  // Use base enumAttrValue derived.
                  if (enumAttrValue.length > 0)
                  {
                     // Only add attribute value if not empty? AttrID is NOT empty.
                	 // Initialize to entire attribute value.
                	 var enumAttrValues = [enumAttrValue];
                	 if (enumSplitAt.length > 0)
                	 {
                		if (enumSplitAt == newlineString)
                		{
                		   enumSplitAt = realNewline;
                		}
                		// Attempt to split attribute value
                		enumAttrValues = enumAttrValue.split(enumSplitAt);
                	 }
                	 // Original value or split values
             		 if (enumAttrValues.length > 0)
            		 {
            		    // Attribute was split into value(s)
            		    for (var enumIx=0; enumIx < enumAttrValues.length; enumIx++)
            		    {
            		       // Add each entry from attribute value.
            		       enumChoices.push(enumAttrValues[enumIx]);
            		    }
            		 }
                  }
               } // END - work item attr ID specified
            } 
         } 
         
      } // End primary conditions
      
      // final set of choices - 
      return enumChoices;
       
   },
   
   __sentinel: null
});

})();