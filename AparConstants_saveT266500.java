package com.ibm.sport.rtc.common;

import java.util.HashMap;

public class AparConstants_saveT266500
{
   public static final String ATTRIBUTE_ID_PREFIX = "com.ibm.sport.rtc.workItem.attribute.apar.";
   public static final String WORK_ITEM_TYPE_ID = "com.ibm.sport.rtc.workItem.type.apar";
   public static final String SYSROUTE_LINK = "com.ibm.sport.rtc.repository.common.linkTypes.sysroutelink";
   public static final String PMRSUBSCRIBE_LINK = "com.ibm.sport.rtc.repository.common.linkTypes.aparsubscribelink";

   public static final String APAR_CLASS = "APAR";

   public static final String ABSTRACT_TEXT_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "abstractText";
   public static final String ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "additionalProblemSummary";
   public static final String APAR_IN_RETAIN_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "aparInRetain";
   public static final String APAR_NUM_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "aparNum";
   public static final String APAR_NAME_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "aparName";

   public static final String APAR_PTF_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "aparPtf";
   public static final String APAR_TYPE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "aparType";
   public static final String APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "applicableComponentLvls1"; // _B74146C
   public static final String AUTO_RECEIPT_SYSROUTE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "autoReceiptSysroute";
   public static final String CIRCUMVENTION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "circumvention";
   public static final String CLOSE_CODE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "closeCode";
   public static final String CLOSE_DATE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "closeDate";
   public static final String COMMENTS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "comments";
   public static final String CONTACT_PHONE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "contactPhone";
   public static final String COMPONENT_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "component";
   public static final String COMMITTED_FIX_RELEASE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "committedFixRelease";
   public static final String CREATE_AS_DRAFT_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "createAsDraft";
   public static final String CREATE_DATE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "createDate";
   public static final String CURRENT_CHANGE_TEAM_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "currentChangeTeam";
   public static final String CURRENT_TARGET_DATE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "currentTargetDate";
   public static final String CUSTOMER_NAME_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "customerName";
   public static final String CUSTOMER_NUMBER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "customerNumber";
   public static final String DATA_LOSS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "dataLoss";

   public static final String DRAFT_ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftAdditionalProblemSummary";
   // _T266500C Changed to stringList type for multiple selection.
   public static final String DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftApplicableComponentLvls2"; // _T266500C
   // _B74146C The selector did NOT need to change - keep name as-is (was).
   // This only selects ONE entry at a time, large string not needed.
   // _T266500C With the advent of multiple selection via a single
   // stringList type, this selector has become deprecated.
   public static final String DRAFT_APPLICABLE_COMPONENT_LVLS_SELECTOR_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftApplicableComponentLvls" + "Selector";
   public static final String DRAFT_CIRCUMVENTION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftCircumvention";
   public static final String DRAFT_CLOSE_CODE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftCloseCode";
   public static final String DRAFT_COMMENTS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftComments";
   public static final String DRAFT_COMMITTED_FIX_RELEASE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftCommittedFixRelease";
   public static final String DRAFT_FAILING_LVL_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftFailingLvl";
   public static final String DRAFT_FAILING_MODULE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftFailingModule";
   public static final String DRAFT_MESSAGE_TO_SUBMITTER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftMessageToSubmitter";
   public static final String DRAFT_MODULES_MACROS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftModulesMacros1";
   public static final String DRAFT_ORIGINAL_IF_DUP_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftOriginalIfDup";
   public static final String DRAFT_PROBLEM_CONCLUSION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftProblemConclusion";
   public static final String DRAFT_PROBLEM_DESCRIPTION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftProblemDescription";
   // _F155269A FIXCAT, Preventative Service Program (PSP)
   // PSP Keywords / APAR Close
   public static final String DRAFT_PSP_KEYWORDS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftPSPKeywords";
   // For the web client, this is an internal editable non-visible attribute.
   public static final String DRAFT_PSP_KEYWORDS_EDITABLE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftPSPKeywordsForWeb";
   public static final String DRAFT_PSP_KEYWORD_SELECTOR_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftPSPKeywordSelector";
   public static final String DRAFT_PSP_KEYWORD_UNSELECTOR_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftPSPKeywordUnSelector";
   public static final String DRAFT_REASON_CODE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftReasonCode";
   public static final String DRAFT_RECOMMENDATION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftRecommendation";
   public static final String DRAFT_RETURN_CODES_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftReturnCodes";
   public static final String DRAFT_SRLS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftSrls1"; // _B74146C
   public static final String DRAFT_SUPPORT_CODE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftSupportCode";
   public static final String DRAFT_TEMPORARY_FIX_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftTemporaryFix";
   public static final String DRAFT_TREL_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftTREL";
   public static final String DRAFT_USERS_AFFECTED_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "draftUsersAffected";
   // _B93726A Close attributes to restore draft attribute values to.
   public static final String STORE_ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeAdditionalProblemSummary";
   public static final String STORE_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeApplicableComponentLvls1";
   // _B74146C The selector did NOT need to change - keep name as-is (was).
   // This only selects ONE entry at a time, large string not needed.
   /*
    * public static final String
    * STORE_APPLICABLE_COMPONENT_LVLS_SELECTOR_ATTRIBUTE_ID =
    * ATTRIBUTE_ID_PREFIX + "storeApplicableComponentLvls" + "Selector";
    */
   public static final String STORE_CIRCUMVENTION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeCircumvention";
   public static final String STORE_CLOSE_CODE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeCloseCode";
   public static final String STORE_COMMENTS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeComments";
   public static final String STORE_COMMITTED_FIX_RELEASE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeCommittedFixRelease";
   public static final String STORE_FAILING_LVL_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeFailingLvl";
   public static final String STORE_FAILING_MODULE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeFailingModule";
   public static final String STORE_MESSAGE_TO_SUBMITTER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeMessageToSubmitter";
   public static final String STORE_MODULES_MACROS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeModulesMacros1";
   public static final String STORE_ORIGINAL_IF_DUP_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeOriginalIfDup";
   public static final String STORE_PROBLEM_CONCLUSION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeProblemConclusion";
   public static final String STORE_PROBLEM_DESCRIPTION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeProblemDescription";
   // _F155269A FIXCAT, Preventative Service Program (PSP)
   // PSP Keywords / APAR Close
   // No need to save / restore UI-only (Un)Selector attributes.
   public static final String STORE_PSP_KEYWORDS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storePSPKeywords";
   // For the web client, this is an internal editable non-visible attribute.
   public static final String STORE_PSP_KEYWORDS_EDITABLE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storePSPKeywordsForWeb";
   public static final String STORE_REASON_CODE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeReasonCode";
   public static final String STORE_RECOMMENDATION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeRecommendation";
   public static final String STORE_RETURN_CODES_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeReturnCodes";
   public static final String STORE_SRLS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeSrls1";
   public static final String STORE_SUPPORT_CODE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeSupportCode";
   public static final String STORE_TEMPORARY_FIX_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeTemporaryFix";
   public static final String STORE_TREL_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeTREL";
   public static final String STORE_USERS_AFFECTED_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "storeUsersAffected";

   public static final String ERROR_DESCRIPTION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "errorDescription";
   public static final String EXTERNAL_SYMPTOM_CODE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "externalSymptomCode";
   public static final String FAILING_LVL_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "failingLvl";
   public static final String FAILING_MODULE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "failingModule";
   public static final String FIX_REQUIRED_DATE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "fixRequiredDate";
   public static final String FIXTEST_TEMPORARY_FIX_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "fixtestTemporaryFix";
   public static final String FLAG_SET_REASON_CODE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "flagSetReasonCode1";
   public static final String FLAG_SET_SUPPORT_CODE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "flagSetSupportCode";
   public static final String FUNCTION_LOSS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "functionLoss";
   public static final String HIPER_KEYWORD_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "hiperKeyword1";
   public static final String HIPER_RELIEF_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "hiperRelief1";
   public static final String HIPERY_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "hIPERY";
   public static final String INSTALLABILITY_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "installability";
   public static final String ACTION_INITIATOR_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "actionInitiator";
   public static final String LAST_CHANGED_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "lastChanged";
   public static final String LOCAL_FIX_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "localFix";
   public static final String MATERIAL_SUBMITTED_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "materialSubmitted";
   public static final String MESSAGE_TO_SUBMITTER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "messageToSubmitter";
   public static final String MODIFY_ACTION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "modifyAction";
   public static final String MODULES_MACROS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "modulesMacros1";
   public static final String MSYSPLEX_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "mSYSPLEX";
   public static final String NEW_FUNCTION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "newFunction";
   public static final String OPERAND_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "operand1";
   public static final String ORIGINAL_IF_DUP_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "originalIfDup";
   public static final String ORIGINAL_TARGET_DATE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "originalTargetDate";
   public static final String PERFORMANCE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "performance";
   public static final String PEPTF_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pEPTF";
   public static final String PERVASIVE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pervasive";
   public static String PEFLAG_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX + "peFlag";
   public static final String PMR_NAME_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pmrName";
   public static final String PMR_QUEUE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pmrQueue";
   public static final String PMR_CENTER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pmrCenter";
   public static final String PMR_OPEN_DATE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pmrOpenDate";
   public static final String PMR_TEXT_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pmrText";
   public static final String POPULATE_FROM_PMR_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "populateFromPmr";
   public static final String PROBLEM_CONCLUSION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "problemConclusion";
   public static final String PROBLEM_DESCRIPTION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "problemDescription";
   public static final String PRODUCT_SPECIFIC_HIPER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "productSpecificHiper";
   public static final String PRODUCT_SPECIFIC_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "productSpecific1";
   public static final String PROGRAMMER_NAME_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "programmerName";
   public static final String PROJ_CLOSE_CODE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "projCloseCode";
   public static final String PTF_TEST_DATE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pTFTestDate";
   public static final String PTF_REQUESTED_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "ptfRequested";
   public static final String REASON_CODE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "reasonCode";
   public static final String RECEIPT_DATE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "receiptDate";
   public static final String RECOMMENDATION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "recommendation";
   public static final String RELEASE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "release";
   public static final String RELIEF_AVAILABLE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "reliefAvailable";
   public static final String RETAIN_SEVERITY_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "retainSeverity";
   public static final String RETAIN_STATUS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "retainStatus";
   public static final String RETURN_CODES_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "returnCodes";
   public static final String REVIEW_STATUS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "reviewStatus";
   public static final String REVIEW_NOTES_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "reviewNotes";
   public static final String REVIEWERS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "reviewers";
   public static final String SANITIZED_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "sanitized";
   public static final String SEC_INT_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "secInt";
   public static final String SECURITY_INTEGRITY_RATING_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "securityIntegrityRating";
   public static final String SERVICEABILITY_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "serviceability";
   public static final String SPEC_ACT_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "specAct";
   public static final String SPEC_ATTN_PERVASIVE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "specAttnPervasive";
   public static final String SPEC_ATTN_FLAG_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "specAttnFlag";
   public static final String SRLS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "srls1"; // _B74146C
   public static final String STATUS_DETAIL_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "statusDetail";
   public static final String SUBMITTER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "submitter";
   public static final String SUBSCRIBE_QUEUE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "subscribeQueue";
   public static final String SUBSCRIBE_CENTER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "subscribeCenter";
   public static final String SUPPORT_CODE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "supportCode";
   public static final String SYMPTOM_KEYWORD_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "symptomKeyword1"; // _F29561C
   public static final String SYSROUTED_FROM_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "sysroutedFrom";
   public static final String SYSROUTED_TO_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "sysroutedTo";
   public static final String SYSROUTE_TYPE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "sysrouteType";
   public static final String SYSTEM_OUTAGE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "systemOutage";
   public static final String SYSTEM_RELEASE_LEVEL_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "systemReleaseLevel";
   public static final String TEMPORARY_FIX_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "temporaryFix";
   // _T266500C Changed to stringList type for multiple selection.
   public static final String TO_APPLICABLE_COMPONENT_LEVELS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "toApplicableComponentLevels2"; // _T266500C
   // _B74146C The selector did NOT need to change - keep name as-is (was).
   // This only selects ONE entry at a time, large string not needed.
   // _T266500C With the advent of multiple selection via a single
   // stringList type, this selector has become deprecated.
   public static final String TO_APPLICABLE_COMPONENT_LEVELS_SELECTOR_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "toApplicableComponentLevels" + "Selector";
   public static final String TO_CHANGE_TEAM_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "toChangeTeam";
   public static final String TO_COMMITTED_FIX_RELEASE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "toCommittedFixRelease";
   public static final String TO_COMPONENT_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "toComponent";
   public static final String TO_EXTERNAL_SYMPTOM_CODE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "toExternalSymptomCode";
   public static final String TO_FAILING_LVL_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "toFailingLvl";
   // removed public static final String TO_PE_FLAG_ATTRIBUTE_ID =
   // ATTRIBUTE_ID_PREFIX
   // removed + "toPE";
   // removed public static final String TO_PE_PTF_ATTRIBUTE_ID =
   // ATTRIBUTE_ID_PREFIX
   // removed + "toPEPTF";
   public static final String TO_PROGRAMMER_NAME_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "toProgrammerName";
   public static final String TO_RETAIN_SEVERITY_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "toRetainSeverity";
   public static final String TO_RELEASE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "toRelease";
   public static final String TO_SCP_RELEASE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "toScpRelease";
   public static final String TO_AUTO_RECEIPT_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "autoReceiptSysroute";
   /*
    * caw not needed for sysroute public static final String
    * TO_SYSTEM_RELEASE_LEVEL_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX +
    * "toSystemReleaseLevel";
    */
   public static final String TO_ZE_FLAG_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "toZE";
   public static final String TO_APARCANCELFLAG_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "toAparRouteCancelFlag";
   public static final String TREL_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "tREL";
   public static final String TYPE_OF_SOLUTION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "typeOfSolution";
   public static final String UPDATE_SUBMITTER_FROM_PMR_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "updateSubmitterFromPmr";
   public static final String USERS_AFFECTED_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "usersAffected";
   public static final String X_SYSTEM_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "xSystem";
   public static final String X_SYSTEM_DATA_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "xSystemData";
   public static String ZEC_ID = ATTRIBUTE_ID_PREFIX + "zEC";
   public static String ZEZ_ID = ATTRIBUTE_ID_PREFIX + "zEZ";
   public static String IPLIST_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX + "ipList";
   public static String PMRSUBSCRIBERS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pmrSubscribers";
   public static final String TRACKINGLIST_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "trackingList";
   public static final String UNASSIGNED = "<Unassigned>";
   public static final String UNKNOWN = "UNKNOWN";

   public static final String RTC_ASSIGN_ACTION = "Assign";
   public static final String RTC_CLEAR_PI_DATA_ACTION = "Clear Personal Information Data in RTC";
   public static final String RTC_CLOSE_ACTION = "Close";
   public static final String RTC_CREATE_ACTION = "Create";
   public static final String RTC_CREATE_IN_RETAIN_ACTION = "Create in RETAIN";
   public static final String RTC_ENTER_MODIFY_ACTION_ACTION = "Enter Modify Action";
   public static final String RTC_MODIFY_CLOSING_TEXT_IN_RTC_ONLY_ACTION_ACTION = "Modify Closing Text In RTC Only";
   public static final String RTC_RECEIPT_ACTION = "Receipt";
   public static final String RTC_RECEIPT_ASSIGN_ACTION = "Receipt+Assign";
   public static final String RTC_FIXTEST_ACTION = "Fixtest";
   public static final String RTC_FLAGSET_ACTION = "Flagset";
   public static final String RTC_REACTIVATE_ACTION = "Reactivate";
   public static final String RTC_REFRESH_FROM_RETAIN_ACTION = "Refresh from RETAIN";
   public static final String RTC_REOPEN_ACTION = "Reopen";
   public static final String RTC_REROUTE_ACTION = "Reroute";
   // public static final String RTC_SECINT_ACTION =
   // "Update Security/Integrity";
   public static final String RTC_S2UPDATE_ACTION = "S2Update";
   public static final String RTC_UPDATEAPARINFO_ACTION = "Update APAR Information";
   public static final String RTC_UPDATECLOSINGTEXT_ACTION = "Update Closing Text";
   public static final String RTC_UPDATESUBSCRIBEDPMRS_ACTION = "Update Subscribed PMRs";
   public static final String RTC_SYSROUTE_ACTION = "Sysroute";
   public static final String RTC_REVIEW_ACTION = "Review";
   public static final String RTC_FETCH_PMR_TEXT_FROM_RETAIN_ACTION = "Fetch PMR Text from RETAIN";
   public static final String RTC_MODIFY_DRAFT_APAR_ACTION = "Modify Draft APAR";
   public static final String RTC_RESET_DRAFT_CLOSING_INFORMATION_ACTION = "Reset Draft Closing Information";
   // _F234119A
   public static final String RTC_POPULATE_DRAFT_CLOSING_INFORMATION_ACTION = "Populate Draft Closing Information from RETAIN";
   public static final String RTC_MARK_AS_SANITIZED_ACTION = "Mark as Sanitized";
   public static final String RTC_MARK_AS_NOT_SANITIZED_ACTION = "Mark as Not Sanitized";

   // START "Get Closing Text From Parent" changes
   public static final String RTC_GET_CLOSING_TEXT_FROM_PARENT_ACTION = "Get Closing Text from Parent";

   // This array contains the field IDs on the DraftClosingInformation
   // tab that are used in the "Get Closing Text From Parent" function
   // which is used for sysrouted APARs.
   // These are the attributes being set in the child APAR.
   // The attributes are derived from the parent DRAFT attributes when
   // the parent is NOT CLOSED.
   // *** These are set from the parent RETAIN attributes when the
   // parent APAR is CLOSED, so the 2 arrays need to be kept in sync!
   public static final String[] RTC_GET_CLOSING_TEXT_FROM_PARENT_DRAFT_FIELDS = {
         DRAFT_CLOSE_CODE_ATTRIBUTE_ID, DRAFT_REASON_CODE_ATTRIBUTE_ID,
         DRAFT_RETURN_CODES_ATTRIBUTE_ID,
         DRAFT_COMMITTED_FIX_RELEASE_ATTRIBUTE_ID,
         DRAFT_FAILING_MODULE_ATTRIBUTE_ID,
         DRAFT_FAILING_LVL_ATTRIBUTE_ID,
         DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID,
         DRAFT_TREL_ATTRIBUTE_ID,
         DRAFT_SUPPORT_CODE_ATTRIBUTE_ID,
         DRAFT_ORIGINAL_IF_DUP_ATTRIBUTE_ID,
         DRAFT_SRLS_ATTRIBUTE_ID,
         DRAFT_MODULES_MACROS_ATTRIBUTE_ID,
         DRAFT_USERS_AFFECTED_ATTRIBUTE_ID,
         DRAFT_PROBLEM_DESCRIPTION_ATTRIBUTE_ID,
         DRAFT_RECOMMENDATION_ATTRIBUTE_ID,
         DRAFT_ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID,
         DRAFT_PROBLEM_CONCLUSION_ATTRIBUTE_ID,
         DRAFT_COMMENTS_ATTRIBUTE_ID,
         DRAFT_CIRCUMVENTION_ATTRIBUTE_ID,
         DRAFT_TEMPORARY_FIX_ATTRIBUTE_ID,
         // _F155269A FIXCAT, Preventative Service Program (PSP)
         // PSP Keywords / APAR Close
         // This appears to copy Draft <--- Draft attributes when
         // not CLOSED and Draft <--- RETAIN attributes when CLOSED.
         // (RETAIN N/A for PSP Keywords.)
         // No need to copy UI-only (Un)Selector attributes.
         DRAFT_PSP_KEYWORDS_ATTRIBUTE_ID,
         DRAFT_PSP_KEYWORDS_EDITABLE_ATTRIBUTE_ID };

   // This array contains the field IDs on the RETAINClosingInformation
   // tab that are used in the "Get Closing Text From Parent" function
   // when the parent APAR is in CLOSED state, to copy to DRAFT
   // attributes, so keep arrays in sync!
   // (This probably should've used a mapping to remove the sync
   // requirement.)
   public static final String[] RTC_GET_CLOSING_TEXT_FROM_PARENT_RETAIN_FIELDS = {
         CLOSE_CODE_ATTRIBUTE_ID, REASON_CODE_ATTRIBUTE_ID,
         RETURN_CODES_ATTRIBUTE_ID, COMMITTED_FIX_RELEASE_ATTRIBUTE_ID,
         FAILING_MODULE_ATTRIBUTE_ID, FAILING_LVL_ATTRIBUTE_ID,
         APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID, TREL_ATTRIBUTE_ID,
         SUPPORT_CODE_ATTRIBUTE_ID, ORIGINAL_IF_DUP_ATTRIBUTE_ID,
         SRLS_ATTRIBUTE_ID, MODULES_MACROS_ATTRIBUTE_ID,
         USERS_AFFECTED_ATTRIBUTE_ID, PROBLEM_DESCRIPTION_ATTRIBUTE_ID,
         RECOMMENDATION_ATTRIBUTE_ID,
         ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID,
         PROBLEM_CONCLUSION_ATTRIBUTE_ID, COMMENTS_ATTRIBUTE_ID,
         CIRCUMVENTION_ATTRIBUTE_ID, TEMPORARY_FIX_ATTRIBUTE_ID,
         // _F155269A FIXCAT, Preventative Service Program (PSP)
         // PSP Keywords / APAR Close
         // This appears to copy Draft <--- Draft attributes when
         // not CLOSED and Draft <--- RETAIN attributes when CLOSED.
         // (RETAIN N/A for PSP Keywords.)
         // *** Also used for REOPEN function.
         // No need to copy UI-only (Un)Selector attributes.
         // Use nulls as an array sync placeholder (no-op). Special
         // function is needed for PSP Keywords - parse from other
         // RETAIN attributes (based on close code).
         // PSP Keywords / APAR Close - PER
         // (SBS mapping appends to Problem Conclusion.)
         // PSP Keywords / APAR Close - URX
         // (SBS mapping appends to Comments.)
         // The related DRAFT attrs get derived from other RETAIN
         // attrs in a 1 to >1 attribute mapping in the code.
         // DRAFT_PSP_KEYWORDS_ATTRIBUTE_ID,
         // DRAFT_PSP_KEYWORDS_EDITABLE_ATTRIBUTE_ID
         null, null };
   // END getClosingTextFromParent changes

   public static final String[] RTC_RETAIN_CLOSING_FIELDS = RTC_GET_CLOSING_TEXT_FROM_PARENT_RETAIN_FIELDS;

   public static final String SBS_ASSIGN_ACTION = "assignApar";
   public static final String[] SBS_ASSIGN_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, PROGRAMMER_NAME_ATTRIBUTE_ID };

   public static final String SBS_BROWSE_ACTION = "browse";
   public static final String[] SBS_BROWSE_ACTION_ATTRIBUTE_IDS = { APAR_NUM_ATTRIBUTE_ID };

   public static final String SBS_CLOSE_CAN_ACTION = "closeCANApar";
   public static final String[] SBS_CLOSE_CAN_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, DRAFT_CLOSE_CODE_ATTRIBUTE_ID,
         DRAFT_COMMENTS_ATTRIBUTE_ID };

   public static final String SBS_CLOSE_DOC_ACTION = "closeDOCApar";
   public static final String[] SBS_CLOSE_DOC_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID,
         DRAFT_ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID,
         DRAFT_CLOSE_CODE_ATTRIBUTE_ID, DRAFT_FAILING_LVL_ATTRIBUTE_ID,
         DRAFT_PROBLEM_CONCLUSION_ATTRIBUTE_ID,
         DRAFT_PROBLEM_DESCRIPTION_ATTRIBUTE_ID,
         DRAFT_RECOMMENDATION_ATTRIBUTE_ID, DRAFT_REASON_CODE_ATTRIBUTE_ID,
         DRAFT_SRLS_ATTRIBUTE_ID, DRAFT_USERS_AFFECTED_ATTRIBUTE_ID };

   public static final String SBS_CLOSE_DUP_ACTION = "closeDUPApar";
   public static final String[] SBS_CLOSE_DUP_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, DRAFT_ORIGINAL_IF_DUP_ATTRIBUTE_ID,
         DRAFT_COMMENTS_ATTRIBUTE_ID };

   public static final String SBS_CLOSE_FIN_ACTION = "closeFINApar";
   public static final String[] SBS_CLOSE_FIN_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID,
         DRAFT_ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID,
         DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID,
         DRAFT_COMMENTS_ATTRIBUTE_ID, DRAFT_CIRCUMVENTION_ATTRIBUTE_ID,
         DRAFT_FAILING_LVL_ATTRIBUTE_ID, DRAFT_FAILING_MODULE_ATTRIBUTE_ID,
         DRAFT_MODULES_MACROS_ATTRIBUTE_ID,
         DRAFT_PROBLEM_DESCRIPTION_ATTRIBUTE_ID,
         DRAFT_RECOMMENDATION_ATTRIBUTE_ID, DRAFT_REASON_CODE_ATTRIBUTE_ID,
         DRAFT_SUPPORT_CODE_ATTRIBUTE_ID, DRAFT_TEMPORARY_FIX_ATTRIBUTE_ID,
         DRAFT_TREL_ATTRIBUTE_ID, DRAFT_USERS_AFFECTED_ATTRIBUTE_ID };

   public static final String SBS_CLOSE_ISV_ACTION = "closeISVApar";
   public static final String[] SBS_CLOSE_ISV_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, DRAFT_COMMENTS_ATTRIBUTE_ID };

   public static final String SBS_CLOSE_MCH_ACTION = "closeMCHApar";
   public static final String[] SBS_CLOSE_MCH_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, DRAFT_COMMENTS_ATTRIBUTE_ID,
         DRAFT_REASON_CODE_ATTRIBUTE_ID };

   public static final String SBS_CLOSE_OEM_ACTION = "closeOEMApar";
   public static final String[] SBS_CLOSE_OEM_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID,
         DRAFT_ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID,
         DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID,
         DRAFT_CIRCUMVENTION_ATTRIBUTE_ID,
         DRAFT_COMMITTED_FIX_RELEASE_ATTRIBUTE_ID,
         DRAFT_FAILING_LVL_ATTRIBUTE_ID, DRAFT_FAILING_MODULE_ATTRIBUTE_ID,
         DRAFT_MODULES_MACROS_ATTRIBUTE_ID,
         DRAFT_PROBLEM_CONCLUSION_ATTRIBUTE_ID,
         DRAFT_PROBLEM_DESCRIPTION_ATTRIBUTE_ID,
         DRAFT_REASON_CODE_ATTRIBUTE_ID, DRAFT_RECOMMENDATION_ATTRIBUTE_ID,
         DRAFT_SRLS_ATTRIBUTE_ID, DRAFT_SUPPORT_CODE_ATTRIBUTE_ID,
         DRAFT_TEMPORARY_FIX_ATTRIBUTE_ID, DRAFT_USERS_AFFECTED_ATTRIBUTE_ID };

   public static final String SBS_CLOSE_PER_ACTION = "closePERApar";
   public static final String[] SBS_CLOSE_PER_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID,
         DRAFT_ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID,
         DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID,
         DRAFT_CIRCUMVENTION_ATTRIBUTE_ID,
         DRAFT_COMMITTED_FIX_RELEASE_ATTRIBUTE_ID,
         DRAFT_FAILING_LVL_ATTRIBUTE_ID,
         DRAFT_FAILING_MODULE_ATTRIBUTE_ID,
         DRAFT_MODULES_MACROS_ATTRIBUTE_ID,
         DRAFT_PROBLEM_CONCLUSION_ATTRIBUTE_ID,
         DRAFT_PROBLEM_DESCRIPTION_ATTRIBUTE_ID,
         // _F155269A FIXCAT, Preventative Service Program (PSP)
         // PSP Keywords / APAR Close - PER
         // Use the "master" collector attribute here.
         // (SBS mapping appends to Problem Conclusion.)
         DRAFT_PSP_KEYWORDS_EDITABLE_ATTRIBUTE_ID,
         DRAFT_REASON_CODE_ATTRIBUTE_ID, DRAFT_RECOMMENDATION_ATTRIBUTE_ID,
         DRAFT_SRLS_ATTRIBUTE_ID, DRAFT_SUPPORT_CODE_ATTRIBUTE_ID,
         DRAFT_TEMPORARY_FIX_ATTRIBUTE_ID, DRAFT_USERS_AFFECTED_ATTRIBUTE_ID };

   public static final String SBS_CLOSE_PRS_ACTION = "closePRSApar";
   public static final String[] SBS_CLOSE_PRS_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID,
         DRAFT_ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID,
         DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID,
         DRAFT_CIRCUMVENTION_ATTRIBUTE_ID, DRAFT_FAILING_LVL_ATTRIBUTE_ID,
         DRAFT_FAILING_MODULE_ATTRIBUTE_ID,
         DRAFT_MODULES_MACROS_ATTRIBUTE_ID,
         DRAFT_PROBLEM_CONCLUSION_ATTRIBUTE_ID,
         DRAFT_PROBLEM_DESCRIPTION_ATTRIBUTE_ID,
         DRAFT_REASON_CODE_ATTRIBUTE_ID, DRAFT_RECOMMENDATION_ATTRIBUTE_ID,
         DRAFT_SRLS_ATTRIBUTE_ID, DRAFT_SUPPORT_CODE_ATTRIBUTE_ID,
         DRAFT_TEMPORARY_FIX_ATTRIBUTE_ID, DRAFT_USERS_AFFECTED_ATTRIBUTE_ID };

   public static final String SBS_CLOSE_REJ_ACTION = "closeREJApar";
   public static final String[] SBS_CLOSE_REJ_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, DRAFT_COMMENTS_ATTRIBUTE_ID,
         DRAFT_REASON_CODE_ATTRIBUTE_ID };

   public static final String SBS_CLOSE_REQ_ACTION = "closeREQApar";
   public static final String[] SBS_CLOSE_REQ_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, DRAFT_COMMENTS_ATTRIBUTE_ID,
         DRAFT_REASON_CODE_ATTRIBUTE_ID };

   public static final String SBS_CLOSE_RET_ACTION = "closeRETApar";
   public static final String[] SBS_CLOSE_RET_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, DRAFT_COMMENTS_ATTRIBUTE_ID,
         DRAFT_RETURN_CODES_ATTRIBUTE_ID };

   public static final String SBS_CLOSE_STD_ACTION = "closeSTDApar";
   public static final String[] SBS_CLOSE_STD_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, DRAFT_COMMENTS_ATTRIBUTE_ID };

   public static final String SBS_CLOSE_SUG_ACTION = "closeSUGApar";
   public static final String[] SBS_CLOSE_SUG_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, DRAFT_COMMENTS_ATTRIBUTE_ID,
         DRAFT_REASON_CODE_ATTRIBUTE_ID };

   public static final String SBS_CLOSE_URX_ACTION = "closeURXApar";
   // _F80509A Action may actually be CLOSE UR1 or UR2.
   // Note: Test release = 999 implies UR1 or UR2 in RETAIN.
   // This is for SPoRT use, it is NOT a RETAIN action.
   public static final String SBS_CLOSE_UR12_ACTION = "closeUR12Apar";
   public static final String SBS_CLOSE_UR12_TEST_RELEASE = "999";
   public static final String[] SBS_CLOSE_URX_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID,
         DRAFT_ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID,
         DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID,
         DRAFT_CIRCUMVENTION_ATTRIBUTE_ID,
         DRAFT_COMMENTS_ATTRIBUTE_ID,
         DRAFT_COMMITTED_FIX_RELEASE_ATTRIBUTE_ID,
         DRAFT_FAILING_LVL_ATTRIBUTE_ID,
         DRAFT_FAILING_MODULE_ATTRIBUTE_ID,
         DRAFT_MODULES_MACROS_ATTRIBUTE_ID,
         DRAFT_PROBLEM_CONCLUSION_ATTRIBUTE_ID,
         DRAFT_PROBLEM_DESCRIPTION_ATTRIBUTE_ID,
         // _F155269A FIXCAT, Preventative Service Program (PSP)
         // PSP Keywords / APAR Close - URX
         // Use the "master" collector attribute here.
         // (SBS mapping appends to Comments.)
         DRAFT_PSP_KEYWORDS_EDITABLE_ATTRIBUTE_ID,
         DRAFT_REASON_CODE_ATTRIBUTE_ID, DRAFT_RECOMMENDATION_ATTRIBUTE_ID,
         DRAFT_SRLS_ATTRIBUTE_ID, DRAFT_SUPPORT_CODE_ATTRIBUTE_ID,
         DRAFT_TEMPORARY_FIX_ATTRIBUTE_ID, DRAFT_TREL_ATTRIBUTE_ID,
         DRAFT_USERS_AFFECTED_ATTRIBUTE_ID };

   public static final String SBS_CLOSE_USE_ACTION = "closeUSEApar";
   public static final String[] SBS_CLOSE_USE_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, DRAFT_COMMENTS_ATTRIBUTE_ID,
         DRAFT_SRLS_ATTRIBUTE_ID };

   // _T21519A Component Browse
   // This is not meant to represent an RTC work item at all, so the record
   // and/or attr names in the artifact do not NEED to be RTC-qualified
   public static final String SBS_COMPONENT_BROWSE_MAIN_RECORD_NAME = "COMPONENT";
   public static final String SBS_COMPONENT_BROWSE_RELEASE_RECORD_NAME = "RELEASE";
   public static final String SBS_COMPONENT_BROWSE_RELEASE_FIELD_NAME = "release";
   // _F206987A Exclude invalid releases as choices.
   public static final String SBS_COMPONENT_BROWSE_RELEASETYPE_FIELD_NAME = "releaseType";
   public static final String SBS_COMPONENT_BROWSE_INVALID_RELEASETYPE_VALUE = "S";
   public static final String SBS_COMPONENT_BROWSE_INVALID_RELEASETYPE_INDICATOR = "<RETAINinvalid>";
   // _T257292A APAR system level default.
   // Also fix invalid release function.
   public static final String SBS_COMPONENT_BROWSE_VALID_RELEASETYPE_VALUES = " B C ";
   public static final String SBS_COMPONENT_BROWSE_SREL_RELEASETYPE_VALUE = "S";
   public static final String SBS_COMPONENT_BROWSE_SREL_RELEASETYPE_INDICATOR = "<RETAINsrel>";
   public static final String SBS_COMPONENT_BROWSE_CHANGE_TEAM_FIELD_NAME = "changeTeams";
   public static final String SBS_COMPONENT_BROWSE_ACTION = "browseGroup";
   public static final String[] SBS_COMPONENT_BROWSE_ACTION_ATTRIBUTE_IDS = { COMPONENT_ATTRIBUTE_ID };

   public static final String SBS_CREATE_ACTION = "create";
   public static final String[] SBS_CREATE_ACTION_ATTRIBUTE_IDS = {
         ABSTRACT_TEXT_ATTRIBUTE_ID, CONTACT_PHONE_ATTRIBUTE_ID,
         COMPONENT_ATTRIBUTE_ID, CUSTOMER_NUMBER_ATTRIBUTE_ID,
         ERROR_DESCRIPTION_ATTRIBUTE_ID, EXTERNAL_SYMPTOM_CODE_ATTRIBUTE_ID,
         LOCAL_FIX_ATTRIBUTE_ID, PMR_NAME_ATTRIBUTE_ID, RELEASE_ATTRIBUTE_ID,
         SEC_INT_ATTRIBUTE_ID, RETAIN_SEVERITY_ATTRIBUTE_ID,
         SUBMITTER_ATTRIBUTE_ID, SYMPTOM_KEYWORD_ATTRIBUTE_ID,
         SYSTEM_RELEASE_LEVEL_ATTRIBUTE_ID, ACTION_INITIATOR_ATTRIBUTE_ID };
   // DATA_LOSS_ATTRIBUTE_ID, FUNCTION_LOSS_ATTRIBUTE_ID,
   // PERFORMANCE_ATTRIBUTE_ID, MSYSPLEX_ATTRIBUTE_ID,
   // PERVASIVE_ATTRIBUTE_ID, SYSTEM_OUTAGE_ATTRIBUTE_ID,
   // X_SYSTEM_ATTRIBUTE_ID };

   public static final String SBS_FIXTEST_ACTION = "fixTestApar";
   public static final String[] SBS_FIXTEST_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, TEMPORARY_FIX_ATTRIBUTE_ID,
         COMMENTS_ATTRIBUTE_ID };

   // Manjusha-for Apars closed 'CAN' and other Apars
   public static final String SBS_FLAGSET_PE_ACTION = "flagset";
   public static final String[] SBS_FLAGSET_PEY_FOR_OTHER_APARS_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, OPERAND_ATTRIBUTE_ID, PEPTF_ATTRIBUTE_ID };
   // Manjusha-for Apars not closed 'CAN'
   public static final String[] SBS_FLAGSET_PEY_FOR_MOSTCLOSEDAPARS_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, OPERAND_ATTRIBUTE_ID, PEPTF_ATTRIBUTE_ID,
         FLAG_SET_REASON_CODE_ATTRIBUTE_ID,
         FLAG_SET_SUPPORT_CODE_ATTRIBUTE_ID,
         // Manjusha sending specAct in after PMR 86072
         // is resolved due to fix in toolkit on July 2011
         SPEC_ACT_ATTRIBUTE_ID

   };
   // Flagset operand B operation
   public static final String[] SBS_FLAGSET_PEN_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, OPERAND_ATTRIBUTE_ID };

   public static final String SBS_FLAGSET_HIPER_ACTION = "flagsetForHiper";
   public static final String[] SBS_FLAGSET_HIPERY_NONCLOSEDAPARS_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, OPERAND_ATTRIBUTE_ID, RELEASE_ATTRIBUTE_ID,
         HIPER_RELIEF_ATTRIBUTE_ID, HIPER_KEYWORD_ATTRIBUTE_ID,
         PRODUCT_SPECIFIC_HIPER_ATTRIBUTE_ID };
   // For closed Apars we do not need release and hiperRelief
   public static final String[] SBS_FLAGSET_HIPERY_CLOSEDAPARS_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, OPERAND_ATTRIBUTE_ID,
         HIPER_KEYWORD_ATTRIBUTE_ID, PRODUCT_SPECIFIC_HIPER_ATTRIBUTE_ID };

   public static final String[] SBS_FLAGSET_HIPER_SYMPTOM_CODES_ATTRIBUTE_IDS = {
         DATA_LOSS_ATTRIBUTE_ID, FUNCTION_LOSS_ATTRIBUTE_ID,
         PERFORMANCE_ATTRIBUTE_ID, MSYSPLEX_ATTRIBUTE_ID,
         PERVASIVE_ATTRIBUTE_ID, SYSTEM_OUTAGE_ATTRIBUTE_ID,
         X_SYSTEM_ATTRIBUTE_ID };

   // Flagset operand I operation
   public static final String[] SBS_FLAGSET_HIPERN_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, OPERAND_ATTRIBUTE_ID };

   // Flagset operand J,K operations
   public static final String SBS_FLAGSET_SPECIALATTENTION_ACTION = "flagsetForSpecialAttention";
   public static final String[] SBS_FLAGSET_SPECIALATTENTION_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, OPERAND_ATTRIBUTE_ID,
         PRODUCT_SPECIFIC_HIPER_ATTRIBUTE_ID, PRODUCT_SPECIFIC_ATTRIBUTE_ID };

   // Flagset Special Attention Symptom Flags
   public static final String[] SBS_FLAGSET_SPECIALATTENTION_SYMPTOM_FLAGS_ATTRIBUTE_IDS = {
         INSTALLABILITY_ATTRIBUTE_ID, NEW_FUNCTION_ATTRIBUTE_ID,
         SPEC_ATTN_PERVASIVE_ATTRIBUTE_ID, X_SYSTEM_DATA_ATTRIBUTE_ID,
         SERVICEABILITY_ATTRIBUTE_ID };

   // Flagset operand C,D,E,F,G operations
   public static final String SBS_FLAGSET_ZAP_ACTION = "flagsetForZAP";
   public static final String[] SBS_FLAGSET_ZAP_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, OPERAND_ATTRIBUTE_ID

   };
   public static final String SBS_REACTIVATE_ACTION = "reActivateApar";
   public static final String[] SBS_REACTIVATE_ACTION_ATTRIBUTE_IDS = { APAR_NUM_ATTRIBUTE_ID };

   public static final String SBS_RECEIPT_ACTION = "receiptApar";
   public static final String[] SBS_RECEIPT_ACTION_ATTRIBUTE_IDS = { APAR_NUM_ATTRIBUTE_ID };

   public static final String SBS_REOPEN_ACTION = "reopenApar";
   public static final String[] SBS_REOPEN_ACTION_ATTRIBUTE_IDS = { APAR_NUM_ATTRIBUTE_ID };

   public static final String SBS_REROUTE_ACTION = "reroute";
   // removed the 'new' attributes from RTC
   public static final String[] SBS_REROUTE_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, COMPONENT_ATTRIBUTE_ID, RELEASE_ATTRIBUTE_ID,
         CURRENT_CHANGE_TEAM_ATTRIBUTE_ID };
   // _B48060A Reroute clearing draft attrs
   public static final String[] SBS_REROUTE_ACTION_RESET_DRAFT_ATTRIBUTE_IDS = {
         DRAFT_ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID,
         DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID,
         DRAFT_CIRCUMVENTION_ATTRIBUTE_ID,
         DRAFT_CLOSE_CODE_ATTRIBUTE_ID,
         DRAFT_COMMENTS_ATTRIBUTE_ID,
         DRAFT_COMMITTED_FIX_RELEASE_ATTRIBUTE_ID,
         DRAFT_FAILING_LVL_ATTRIBUTE_ID,
         DRAFT_FAILING_MODULE_ATTRIBUTE_ID,
         DRAFT_MESSAGE_TO_SUBMITTER_ATTRIBUTE_ID,
         DRAFT_MODULES_MACROS_ATTRIBUTE_ID,
         DRAFT_ORIGINAL_IF_DUP_ATTRIBUTE_ID,
         DRAFT_PROBLEM_CONCLUSION_ATTRIBUTE_ID,
         DRAFT_PROBLEM_DESCRIPTION_ATTRIBUTE_ID,
         // _F155269A FIXCAT, Preventative Service Program (PSP)
         // PSP Keywords / APAR Close
         DRAFT_PSP_KEYWORDS_ATTRIBUTE_ID,
         DRAFT_PSP_KEYWORDS_EDITABLE_ATTRIBUTE_ID,
         DRAFT_REASON_CODE_ATTRIBUTE_ID, DRAFT_RECOMMENDATION_ATTRIBUTE_ID,
         DRAFT_RETURN_CODES_ATTRIBUTE_ID, DRAFT_SRLS_ATTRIBUTE_ID,
         DRAFT_SUPPORT_CODE_ATTRIBUTE_ID, DRAFT_TEMPORARY_FIX_ATTRIBUTE_ID,
         DRAFT_TREL_ATTRIBUTE_ID, DRAFT_USERS_AFFECTED_ATTRIBUTE_ID };
   // _B93726A When an APAR switches to INTRAN state, certain close codes,
   // and any related attributes may not have valid values or choices.
   // In this case store the draft attributes internally, and clear the
   // draft attributes. Later restore the DRAFT attributes when the APAR
   // comes out of INTRAN state.
   // The direction of saving or restoring can be done in the code.
   public final static HashMap<String, String> SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS = new HashMap<String, String>();
   static
   {
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID,
            STORE_ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID,
            STORE_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_CIRCUMVENTION_ATTRIBUTE_ID,
            STORE_CIRCUMVENTION_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_CLOSE_CODE_ATTRIBUTE_ID, STORE_CLOSE_CODE_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_COMMENTS_ATTRIBUTE_ID, STORE_COMMENTS_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_COMMITTED_FIX_RELEASE_ATTRIBUTE_ID,
            STORE_COMMITTED_FIX_RELEASE_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_FAILING_LVL_ATTRIBUTE_ID, STORE_FAILING_LVL_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_FAILING_MODULE_ATTRIBUTE_ID,
            STORE_FAILING_MODULE_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_MESSAGE_TO_SUBMITTER_ATTRIBUTE_ID,
            STORE_MESSAGE_TO_SUBMITTER_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_MODULES_MACROS_ATTRIBUTE_ID,
            STORE_MODULES_MACROS_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_ORIGINAL_IF_DUP_ATTRIBUTE_ID,
            STORE_ORIGINAL_IF_DUP_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_PROBLEM_CONCLUSION_ATTRIBUTE_ID,
            STORE_PROBLEM_CONCLUSION_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_PROBLEM_DESCRIPTION_ATTRIBUTE_ID,
            STORE_PROBLEM_DESCRIPTION_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_REASON_CODE_ATTRIBUTE_ID, STORE_REASON_CODE_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_RECOMMENDATION_ATTRIBUTE_ID,
            STORE_RECOMMENDATION_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_RETURN_CODES_ATTRIBUTE_ID, STORE_RETURN_CODES_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_SRLS_ATTRIBUTE_ID, STORE_SRLS_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_SUPPORT_CODE_ATTRIBUTE_ID, STORE_SUPPORT_CODE_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_TEMPORARY_FIX_ATTRIBUTE_ID,
            STORE_TEMPORARY_FIX_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_TREL_ATTRIBUTE_ID, STORE_TREL_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_USERS_AFFECTED_ATTRIBUTE_ID,
            STORE_USERS_AFFECTED_ATTRIBUTE_ID );
      // _F155269A FIXCAT, Preventative Service Program (PSP)
      // PSP Keywords / APAR Close
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_PSP_KEYWORDS_ATTRIBUTE_ID, STORE_PSP_KEYWORDS_ATTRIBUTE_ID );
      SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS.put(
            DRAFT_PSP_KEYWORDS_EDITABLE_ATTRIBUTE_ID,
            STORE_PSP_KEYWORDS_EDITABLE_ATTRIBUTE_ID );
   }

   public static final String SBS_ROUTEA_ACTION = "routeA";
   public static final String[] SBS_ROUTEA_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, TO_PROGRAMMER_NAME_ATTRIBUTE_ID, // required,
         // null
         // allowed
         TO_EXTERNAL_SYMPTOM_CODE_ATTRIBUTE_ID, // required, null allowed
         TO_COMPONENT_ATTRIBUTE_ID, // required
         TO_SCP_RELEASE_ATTRIBUTE_ID, // required
         TO_RETAIN_SEVERITY_ATTRIBUTE_ID, // required
         TO_RELEASE_ATTRIBUTE_ID, // required
         // removed TO_PE_FLAG_ATTRIBUTE_ID, //optional
         TO_ZE_FLAG_ATTRIBUTE_ID // optional
   // removed TO_PE_PTF_ATTRIBUTE_ID, //required if PE=Y, null allowed
   };

   // optional for all sysroute A and C requests
   public static final String[] SBS_ROUTE_ACTION_OPTATTRIBUTE_IDS = { TO_CHANGE_TEAM_ATTRIBUTE_ID // optional,
   // null
   // not
   // allowed
   };

   public static final String SBS_ROUTEB_ACTION = "routeB";
   public static final String[] SBS_ROUTEB_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, TO_PROGRAMMER_NAME_ATTRIBUTE_ID, // required
         TO_EXTERNAL_SYMPTOM_CODE_ATTRIBUTE_ID, // required
         TO_COMPONENT_ATTRIBUTE_ID, // required
         TO_SCP_RELEASE_ATTRIBUTE_ID, // required
         TO_RELEASE_ATTRIBUTE_ID, // required
         TO_RETAIN_SEVERITY_ATTRIBUTE_ID, // required
         // TO_APPLICABLE_COMPONENT_LEVELS_ATTRIBUTE_ID, //conditionally
         // required
         TO_COMMITTED_FIX_RELEASE_ATTRIBUTE_ID, // required
         TO_FAILING_LVL_ATTRIBUTE_ID // required
   };
   // optional for sysrouteB requests
   public static final String[] SBS_ROUTEB_ACTION_OPTATTRIBUTE_IDS = { TO_CHANGE_TEAM_ATTRIBUTE_ID // optional,
   // null
   // not
   // allowed
   };

   public static final String SBS_ROUTEC_ACTION = "routeC";
   public static final String[] SBS_ROUTEC_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, TO_PROGRAMMER_NAME_ATTRIBUTE_ID, // required
         TO_EXTERNAL_SYMPTOM_CODE_ATTRIBUTE_ID, // required
         TO_COMPONENT_ATTRIBUTE_ID, // required
         TO_SCP_RELEASE_ATTRIBUTE_ID, // required
         TO_RETAIN_SEVERITY_ATTRIBUTE_ID, // required
         TO_RELEASE_ATTRIBUTE_ID, // required
         // removed TO_PE_FLAG_ATTRIBUTE_ID, //optional
         TO_ZE_FLAG_ATTRIBUTE_ID, // optional
         TO_APARCANCELFLAG_ATTRIBUTE_ID, // required
         // removed TO_PE_PTF_ATTRIBUTE_ID, //required if PE=Y
         TO_CHANGE_TEAM_ATTRIBUTE_ID // optional
   };

   // following is all encompassing attributes on sysroute tab that should
   // be *reset* after successful sysroute action
   public static final String[] SBS_ROUTE_ACTION_ATTRIBUTE_IDS = {
         SYSROUTE_TYPE_ATTRIBUTE_ID, TO_PROGRAMMER_NAME_ATTRIBUTE_ID,
         TO_EXTERNAL_SYMPTOM_CODE_ATTRIBUTE_ID,
         TO_COMPONENT_ATTRIBUTE_ID,
         TO_SCP_RELEASE_ATTRIBUTE_ID,
         TO_RETAIN_SEVERITY_ATTRIBUTE_ID,
         TO_RELEASE_ATTRIBUTE_ID,
         // removed TO_PE_FLAG_ATTRIBUTE_ID,
         TO_ZE_FLAG_ATTRIBUTE_ID,
         // removed TO_PE_PTF_ATTRIBUTE_ID,
         TO_CHANGE_TEAM_ATTRIBUTE_ID,
         TO_APPLICABLE_COMPONENT_LEVELS_ATTRIBUTE_ID,
         TO_COMMITTED_FIX_RELEASE_ATTRIBUTE_ID, TO_FAILING_LVL_ATTRIBUTE_ID };

   // securityIntegrityRating is not RETAIN field, thus not included
   // public static final String SBS_SECINT_ACTION = "editFixedFields";
   // public static final String[] SBS_SECINT_ACTION_ATTRIBUTE_IDS = {
   // APAR_NUM_ATTRIBUTE_ID, COMPONENT_ATTRIBUTE_ID, SEC_INT_ATTRIBUTE_ID };
   // For closed APARs
   public static final String SBS_UPDATECLOSINGTEXT_FOLLOWUP_ACTION = "followup";
   public static final String[] SBS_UPDATECLOSINGTEXT_FOLLOWUP_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, DRAFT_MESSAGE_TO_SUBMITTER_ATTRIBUTE_ID,
         DRAFT_ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID,
         DRAFT_CIRCUMVENTION_ATTRIBUTE_ID, DRAFT_COMMENTS_ATTRIBUTE_ID,
         DRAFT_COMMITTED_FIX_RELEASE_ATTRIBUTE_ID,
         DRAFT_MODULES_MACROS_ATTRIBUTE_ID, DRAFT_SRLS_ATTRIBUTE_ID,
         DRAFT_PROBLEM_CONCLUSION_ATTRIBUTE_ID,
         DRAFT_PROBLEM_DESCRIPTION_ATTRIBUTE_ID,
         DRAFT_RECOMMENDATION_ATTRIBUTE_ID, DRAFT_TEMPORARY_FIX_ATTRIBUTE_ID,
         DRAFT_USERS_AFFECTED_ATTRIBUTE_ID };

   // For open APARs
   public static final String SBS_UPDATECLOSINGTEXT_EDITRESPONDERPAGE_ACTION = "editResponderText";
   public static final String[] SBS_UPDATECLOSINGTEXT_EDITRESPONDERPAGE_ACTION_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, DRAFT_MESSAGE_TO_SUBMITTER_ATTRIBUTE_ID,
         DRAFT_ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID,
         DRAFT_CIRCUMVENTION_ATTRIBUTE_ID, DRAFT_COMMENTS_ATTRIBUTE_ID,
         DRAFT_PROBLEM_CONCLUSION_ATTRIBUTE_ID,
         DRAFT_PROBLEM_DESCRIPTION_ATTRIBUTE_ID,
         DRAFT_RECOMMENDATION_ATTRIBUTE_ID, DRAFT_RETURN_CODES_ATTRIBUTE_ID,
         DRAFT_TEMPORARY_FIX_ATTRIBUTE_ID, DRAFT_USERS_AFFECTED_ATTRIBUTE_ID

   };
   public static final String SBS_S2UPDATE_ACTION = "s2update";
   public static final String[] SBS_S2UPDATE_ACTION_REQ_ATTRIBUTE_IDS = {
         APAR_NUM_ATTRIBUTE_ID, COMPONENT_ATTRIBUTE_ID };
   public static final String[] SBS_S2UPDATE_ACTION_OPT_ATTRIBUTE_IDS = {
         RELIEF_AVAILABLE_ATTRIBUTE_ID, STATUS_DETAIL_ATTRIBUTE_ID,
         TYPE_OF_SOLUTION_ATTRIBUTE_ID, PROJ_CLOSE_CODE_ATTRIBUTE_ID,
         CURRENT_TARGET_DATE_ATTRIBUTE_ID, FIX_REQUIRED_DATE_ATTRIBUTE_ID,
         PTF_TEST_DATE_ATTRIBUTE_ID };

   public static final String SBS_UPDATEAPARINFO_UPDATESEVERITY_ACTION = "severityChange";
   public static final String SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION = "editFixedFields";
   public static final String SBS_UPDATEAPARINFO_UPDATESUBMITTERTEXT_ACTION = "editSubmitterText";

   // HashMap for APAR attribute ids and corresponding sbs actions
   public final static HashMap<String, String> SBS_UPDATEAPARINFO_ACTIONS = new HashMap<String, String>();
   static
   {

      SBS_UPDATEAPARINFO_ACTIONS.put( ABSTRACT_TEXT_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( ERROR_DESCRIPTION_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATESUBMITTERTEXT_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( LOCAL_FIX_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATESUBMITTERTEXT_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( EXTERNAL_SYMPTOM_CODE_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( SYMPTOM_KEYWORD_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( SUBMITTER_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( CONTACT_PHONE_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( CUSTOMER_NAME_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( CUSTOMER_NUMBER_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( PMR_NAME_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( RETAIN_SEVERITY_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATESEVERITY_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( APAR_TYPE_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( SPEC_ACT_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( SEC_INT_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( DATA_LOSS_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( FUNCTION_LOSS_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( PERFORMANCE_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( MSYSPLEX_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( PERVASIVE_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( SYSTEM_OUTAGE_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
      SBS_UPDATEAPARINFO_ACTIONS.put( X_SYSTEM_ATTRIBUTE_ID,
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION );
   }

   // This HashMap is to avoid calling an action multiple times in case
   // multiple attributes are updated at a time
   public final static HashMap<String, String> SBS_UPDATEAPARINFO_ATTRIBUTE_IDS = new HashMap<String, String>();

   static
   {
      SBS_UPDATEAPARINFO_ATTRIBUTE_IDS.put(
            SBS_UPDATEAPARINFO_UPDATESEVERITY_ACTION, APAR_NUM_ATTRIBUTE_ID
                  + "," + RETAIN_SEVERITY_ATTRIBUTE_ID );
      SBS_UPDATEAPARINFO_ATTRIBUTE_IDS.put(
            SBS_UPDATEAPARINFO_UPDATESUBMITTERTEXT_ACTION,
            APAR_NUM_ATTRIBUTE_ID + "," + ERROR_DESCRIPTION_ATTRIBUTE_ID
                  + "," + LOCAL_FIX_ATTRIBUTE_ID );
      SBS_UPDATEAPARINFO_ATTRIBUTE_IDS.put(
            SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION,
            APAR_NUM_ATTRIBUTE_ID + "," + COMPONENT_ATTRIBUTE_ID + ","
                  + ABSTRACT_TEXT_ATTRIBUTE_ID + ","
                  + EXTERNAL_SYMPTOM_CODE_ATTRIBUTE_ID + ","
                  + SYMPTOM_KEYWORD_ATTRIBUTE_ID + ","
                  + SUBMITTER_ATTRIBUTE_ID + "," + CONTACT_PHONE_ATTRIBUTE_ID
                  + "," + CUSTOMER_NAME_ATTRIBUTE_ID + ","
                  + CUSTOMER_NUMBER_ATTRIBUTE_ID + ","
                  + PMR_NAME_ATTRIBUTE_ID + "," + APAR_TYPE_ATTRIBUTE_ID
                  + "," + SPEC_ACT_ATTRIBUTE_ID + ","
                  + DATA_LOSS_ATTRIBUTE_ID + "," + FUNCTION_LOSS_ATTRIBUTE_ID
                  + "," + PERFORMANCE_ATTRIBUTE_ID + ","
                  + MSYSPLEX_ATTRIBUTE_ID + "," + PERVASIVE_ATTRIBUTE_ID
                  + "," + SYSTEM_OUTAGE_ATTRIBUTE_ID + ","
                  + X_SYSTEM_ATTRIBUTE_ID );

   }

   // Map of Draft closing information field and corresponding RETAIN closing
   // information field
   public final static HashMap<String, String> SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS = new HashMap<String, String>();
   static
   {
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID,
            ADDITIONAL_PROBLEM_SUMMARY_ATTRIBUTE_ID );
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID,
            APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID );
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_CIRCUMVENTION_ATTRIBUTE_ID, CIRCUMVENTION_ATTRIBUTE_ID );
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_CLOSE_CODE_ATTRIBUTE_ID, CLOSE_CODE_ATTRIBUTE_ID );
      // _F155269A FIXCAT, Preventative Service Program (PSP)
      // PSP Keywords / APAR Close
      // Extract / remove PSP Keywords from Comments.
      // (Handled within extraction function.)
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put( DRAFT_COMMENTS_ATTRIBUTE_ID,
            COMMENTS_ATTRIBUTE_ID );
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_COMMITTED_FIX_RELEASE_ATTRIBUTE_ID,
            COMMITTED_FIX_RELEASE_ATTRIBUTE_ID );
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_FAILING_LVL_ATTRIBUTE_ID, FAILING_LVL_ATTRIBUTE_ID );
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_FAILING_MODULE_ATTRIBUTE_ID, FAILING_MODULE_ATTRIBUTE_ID );
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_MESSAGE_TO_SUBMITTER_ATTRIBUTE_ID,
            MESSAGE_TO_SUBMITTER_ATTRIBUTE_ID );
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_MODULES_MACROS_ATTRIBUTE_ID, MODULES_MACROS_ATTRIBUTE_ID );
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_ORIGINAL_IF_DUP_ATTRIBUTE_ID, ORIGINAL_IF_DUP_ATTRIBUTE_ID );
      // _F155269A FIXCAT, Preventative Service Program (PSP)
      // PSP Keywords / APAR Close
      // Extract / remove PSP Keywords from Problem Conclusion.
      // (Handled within extraction function.)
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_PROBLEM_CONCLUSION_ATTRIBUTE_ID,
            PROBLEM_CONCLUSION_ATTRIBUTE_ID );
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_PROBLEM_DESCRIPTION_ATTRIBUTE_ID,
            PROBLEM_DESCRIPTION_ATTRIBUTE_ID );
      // _F155269A FIXCAT, Preventative Service Program (PSP)
      // PSP Keywords / APAR Close
      // (PSP keywords may come from 2 RETAIN attributes.)
      // (Handled within extraction function.)
      /*
       * SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
       * DRAFT_PSP_KEYWORDS_ATTRIBUTE_ID,
       * DRAFT_PSP_KEYWORDS_EDITABLE_ATTRIBUTE_ID );
       * SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
       * DRAFT_PSP_KEYWORDS_EDITABLE_ATTRIBUTE_ID,
       * DRAFT_PSP_KEYWORDS_ATTRIBUTE_ID );
       */
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_REASON_CODE_ATTRIBUTE_ID, REASON_CODE_ATTRIBUTE_ID );
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_RECOMMENDATION_ATTRIBUTE_ID, RECOMMENDATION_ATTRIBUTE_ID );
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_RETURN_CODES_ATTRIBUTE_ID, RETURN_CODES_ATTRIBUTE_ID );
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put( DRAFT_SRLS_ATTRIBUTE_ID,
            SRLS_ATTRIBUTE_ID );
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_SUPPORT_CODE_ATTRIBUTE_ID, SUPPORT_CODE_ATTRIBUTE_ID );
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_TEMPORARY_FIX_ATTRIBUTE_ID, TEMPORARY_FIX_ATTRIBUTE_ID );
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put( DRAFT_TREL_ATTRIBUTE_ID,
            TREL_ATTRIBUTE_ID );
      SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS.put(
            DRAFT_USERS_AFFECTED_ATTRIBUTE_ID, USERS_AFFECTED_ATTRIBUTE_ID );
   }
}
