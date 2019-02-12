package com.ibm.sport.rtc.common;

import java.util.ArrayList;

public class PmrConstants
{
   public static final String ATTRIBUTE_ID_PREFIX = "com.ibm.sport.rtc.workItem.attribute.pmr.";
   public static final String WORK_ITEM_TYPE_ID = "com.ibm.sport.rtc.workItem.type.pmr";
   public static final String PMR_CLASS = "PMR";
   public static final String CALL_CLASS = "Call";
   public static final String PMR_CALL_RECORD_NAME = "PMR_CALL";

   public static final String OPEN_STATE = "OPEN";

   public static final String PMR_NAME_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pmrName";
   public static final String PMR_NO_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pmrNo";
   public static final String OPEN_DATE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pmrOpenDate";
   public static final String COMPONENT_ID_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "componentID";
   public static final String CITY_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "city";
   public static final String QUEUE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "queue";
   public static final String STATE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "State";
   public static final String CONTACT_NAME_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "contactName";
   public static final String CONTACT_PHONE_1_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "contactPhone1";
   public static final String CONTACT_PHONE_2_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "contactPhone2";
   public static final String CRITICAL_SITUATION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "criticalSituation";
   public static final String CUSTOMER_NAME_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "customerName";
   public static final String CUSTOMER_NUMBER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "customerNumber";
   public static final String EMAIL_ADDRESS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "emailAddress";
   public static final String KEYWORD_1_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "keyword1";
   public static final String KEYWORD_2_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "keyword2";
   public static final String KEYWORD_3_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "keyword3";
   public static final String OWNER_EMPLOYER_NUMBER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "ownerEmployerNumber";
   public static final String OWNER_NAME_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "ownerName";
   public static final String ENTITLEMENT_CODE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "entitlementCode";
   public static final String APAR_NUMBER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "aparNumber";
   public static final String RELEASE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "release";
   public static final String PROBLEM_STATUS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "problemStatus";
   public static final String TEXT_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "text";
   public static final String CRITICAL_SITUATION_DESC_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "criticalSituationDesc";
   public static final String COMMENT_LINE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "commentLine";
   public static final String BRANCH_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "branch";
   public static final String COUNTRY_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "country";
   public static final String CENTER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "center";
   public static final String MODIFY_ACTION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "modifyAction";
   public static final String RETAIN_STATUS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "retainStatus";
   public static final String NEXT_CENTER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "nextCenter";
   public static final String NEXT_QUEUE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "nextQueue";
   public static final String PRIMARY_FUP_QUEUE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "primaryFupQueue";
   public static final String PRIMARY_FUP_CENTER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "primaryFupCenter";
   public static final String SERVICE_GIVEN_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "serviceGiven";
   public static final String FOLLOWUP_INFO_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "followupInfo";
   public static final String RETAIN_SEVERITY_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "retainSeverity";
   public static final String RETAIN_PRIORITY_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "retainPriority2";
   public static final String PMR_IN_RETAIN_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pmrInRetain";
   public static final String INPUT_TEXT_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "inputText";
   public static final String PMR_CHECKCALLS_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pmrCheckCalls";
   public static final String PMR_CLOSETEXT_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pmrCloseText";
   public static final String SUB_QUEUE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "subQueue";
   public static final String SUB_CENTER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "subCenter";
   public static final String SUB_APARNUM_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "subAparNum";
   public static final String CALL_COUNT_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "callCount";
   public static final String CALL_LIST_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "callList1"; // _B68102C
   public static final String SELECTED_CALL_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "selectedCall";
   public static final String PPG_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX + "pPG";
   public static final String TARGET_QUEUE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "targetQueue";
   public static final String TARGET_CENTER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "targetCenter";
   public static final String CALL_PRIMARY_SECONDARY_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "callPrimarySecondary";
   public static final String SERIAL_DISPATCHED_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "serialDispatched";
   public static final String CALL_DISPATCHED_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "callDispatched";
   public static final String UNFORMATTED_CALL_LIST_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "unformattedCallList";

   public static final String PMR_CALL_QUEUE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pmrCall.queue";
   public static final String PMR_CALL_CENTER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pmrCall.center";
   public static final String PMR_CALL_PPG_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pmrCall.pPG";
   public static final String SANITIZED_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "sanitized";

   public static final String RTC_ENTER_MODIFY_ACTION_ACTION = "Enter Modify Action";
   public static final String RTC_ADD_TEXT_ACTION = "Add Text";
   public static final String RTC_CALL_COMPLETE_ACTION = "Call Complete";
   public static final String RTC_CALL_CREATE_ACTION = "Call Create";
   public static final String RTC_CALL_REQUEUE_ACTION = "Call Requeue";
   // _T64529A PMR Call Dispatch
   public static final String RTC_CALL_DISPATCH_ACTION = "Call Dispatch";
   public static final String RTC_CALL_CONVERT_ACTION = "Call Convert";
   public static final String RTC_EDIT_FIELDS_ACTION = "Edit Fields";
   public static final String RTC_CREATE_ACTION = "Create";
   public static final String RTC_REOPEN_ACTION = "Reopen";
   public static final String RTC_CLOSE_ACTION = "Close";
   // _T155551A ARCHIVED state
   public static final String RTC_ARCHIVE_ACTION = "Archive";
   public static final String RTC_UNARCHIVE_ACTION = "Unarchive";
   public static final String RTC_CREATE_IN_RETAIN_ACTION = "CreateInRETAIN";
   public static final String RTC_REFRESH_FROM_RETAIN_ACTION = "Refresh from RETAIN";
   public static final String RTC_MARK_AS_SANITIZED_ACTION = "Mark as Sanitized";
   public static final String RTC_MARK_AS_NOT_SANITIZED_ACTION = "Mark as Not Sanitized";
   public static final String RTC_CLEAR_PI_DATA_ACTION = "Clear Personal Information Data in RTC";

   public static final String SBS_CREATE_ACTION = "createPMROnly";
   public static final String SBS_CREATE_SECONDARY_ACTION = "createSecondary";

   // _T155546A Actions (state-changing or modify) that require an
   // ACTIVE PMR in RETAIN. ArrayList used for "contains" function.
   // Create is not necessary to test for (even in RETAIN).
   // The archive action already has its own (reverse, not-active)
   // testing (in the operation participant function).
   // _T155546M UnArchive will change to use this pre-check.
   // If there were RTC-only update actions, perhaps they may be
   // omitted from this list.
   /**
    * Actions (state-changing or modify) that require an ACTIVE PMR in RETAIN.
    * ArrayList used for "contains" function.
    * <p>
    * IMPORTANT <br>
    * This should be maintained with any new actions added. <br>
    * /IMPORTANT
    */
   public final static ArrayList<String> ACTIVE_PMR_ACTIONS = new ArrayList<String>();
   static
   {
      ACTIVE_PMR_ACTIONS.add( RTC_ADD_TEXT_ACTION );
      // ARCHIVE uses a "reverse" (not-active) test.
      // ACTIVE_PMR_ACTIONS.add( RTC_ARCHIVE_ACTION );
      ACTIVE_PMR_ACTIONS.add( RTC_CALL_COMPLETE_ACTION );
      ACTIVE_PMR_ACTIONS.add( RTC_CALL_CONVERT_ACTION );
      ACTIVE_PMR_ACTIONS.add( RTC_CALL_CREATE_ACTION );
      ACTIVE_PMR_ACTIONS.add( RTC_CALL_DISPATCH_ACTION );
      ACTIVE_PMR_ACTIONS.add( RTC_CALL_REQUEUE_ACTION );
      ACTIVE_PMR_ACTIONS.add( RTC_CLOSE_ACTION );
      // A create test is not needed. Even in RETAIN, it will throw
      // its own error if it does not exist (archived), or create the
      // currently active PMR.
      // ACTIVE_PMR_ACTIONS.add( RTC_CREATE_ACTION );
      // ACTIVE_PMR_ACTIONS.add( RTC_CREATE_IN_RETAIN_ACTION );
      ACTIVE_PMR_ACTIONS.add( RTC_EDIT_FIELDS_ACTION );
      ACTIVE_PMR_ACTIONS.add( RTC_REFRESH_FROM_RETAIN_ACTION );
      ACTIVE_PMR_ACTIONS.add( RTC_REOPEN_ACTION );
      // _T155546M UnArchive test moved to this common function.
      ACTIVE_PMR_ACTIONS.add( RTC_UNARCHIVE_ACTION );
   }

   public static final String[] SBS_CREATE_ACTION_ATTRIBUTE_IDS = {
         BRANCH_ATTRIBUTE_ID, COUNTRY_ATTRIBUTE_ID,
         RETAIN_SEVERITY_ATTRIBUTE_ID, COMPONENT_ID_ATTRIBUTE_ID,
         RELEASE_ATTRIBUTE_ID, CUSTOMER_NUMBER_ATTRIBUTE_ID,
         CUSTOMER_NAME_ATTRIBUTE_ID, CONTACT_PHONE_1_ATTRIBUTE_ID,
         CONTACT_NAME_ATTRIBUTE_ID, EMAIL_ADDRESS_ATTRIBUTE_ID,
         CONTACT_PHONE_2_ATTRIBUTE_ID, KEYWORD_1_ATTRIBUTE_ID,
         KEYWORD_2_ATTRIBUTE_ID, KEYWORD_3_ATTRIBUTE_ID,
         CRITICAL_SITUATION_DESC_ATTRIBUTE_ID, COMMENT_LINE_ATTRIBUTE_ID,
         INPUT_TEXT_ATTRIBUTE_ID };
   public static final String SBS_BROWSE_ACTION = "browse";
   public static final String SBS_ADD_TEXT_ACTION = "addText";
   public static final String SBS_CALL_BROWSE_ACTION = "browse";
   public static final String SBS_CALL_COMPLETE_ACTION = "callComplete";
   public static final String SBS_CALL_CREATE_ACTION = "callGenerate";
   public static final String SBS_CALL_REQUEUE_ACTION = "callRequeue";
   // _T64529A PMR Call Dispatch, dispatch turns into callUpdate with operand
   public static final String SBS_CALL_DISPATCH_ACTION = "dispatch";
   public static final String SBS_CALL_SEARCH_ACTION = "search";
   public static final String SBS_CALL_CONVERT_ACTION = "callConvert";
   public static final String SBS_UPDATE_PMR_ACTION = "updatePMR";
   public static final String[] SBS_BROWSE_ACTION_ATTRIBUTE_IDS = {
         PMR_NO_ATTRIBUTE_ID, BRANCH_ATTRIBUTE_ID, COUNTRY_ATTRIBUTE_ID };
   public static final String[] SBS_ADD_TEXT_ACTION_ATTRIBUTE_IDS = {
         PMR_NO_ATTRIBUTE_ID, BRANCH_ATTRIBUTE_ID, COUNTRY_ATTRIBUTE_ID,
         INPUT_TEXT_ATTRIBUTE_ID };
   public static final String[] SBS_CALL_BROWSE_ACTION_ATTRIBUTE_IDS = {
         // Uses no PMR attributes
   };
   public static final String[] SBS_CALL_COMPLETE_ACTION_REQ_ATTRIBUTE_IDS = {};
   public static final String[] SBS_CALL_CREATE_ACTION_ATTRIBUTE_IDS = {
         PMR_NO_ATTRIBUTE_ID, BRANCH_ATTRIBUTE_ID, COUNTRY_ATTRIBUTE_ID,
         CUSTOMER_NUMBER_ATTRIBUTE_ID, QUEUE_ATTRIBUTE_ID,
         CENTER_ATTRIBUTE_ID };
   public static final String[] SBS_CALL_REQUEUE_ACTION_REQ_ATTRIBUTE_IDS = {
         TARGET_QUEUE_ATTRIBUTE_ID, TARGET_CENTER_ATTRIBUTE_ID };
   public static final String[] SBS_CALL_REQUEUECOMPLETE_ACTION_PRIMARY_OPT_NOBLANK_ATTRIBUTE_IDS = {
         SERVICE_GIVEN_ATTRIBUTE_ID, FOLLOWUP_INFO_ATTRIBUTE_ID };
   public static final String[] SBS_CALL_REQUEUECOMPLETE_ACTION_PRIMARY_OPT_CANBEBLANK_ATTRIBUTE_IDS = {
         PRIMARY_FUP_QUEUE_ATTRIBUTE_ID, PRIMARY_FUP_CENTER_ATTRIBUTE_ID };
   public static final String[] SBS_CALL_REQUEUECOMPLETE_ACTION_SECONDARY_OPT_ATTRIBUTE_IDS = {
         SERVICE_GIVEN_ATTRIBUTE_ID };
   // _T64529A PMR Call Dispatch, dispatch turns into callUpdate with operand
   public static final String[] SBS_CALL_DISPATCH_ACTION_ATTRIBUTE_IDS = {
         /*
          * Uses no PMR attributes, needs Queue, Center, pPG in callId record
          * PMR_NO_ATTRIBUTE_ID, BRANCH_ATTRIBUTE_ID, COUNTRY_ATTRIBUTE_ID,
          * QUEUE_ATTRIBUTE_ID, CENTER_ATTRIBUTE_ID, PPG
          */
   };
   public static final String[] SBS_CALL_CONVERT_ACTION_ATTRIBUTE_IDS = {};
   public static final String[] SBS_EDIT_FIELDS_ACTION_ATTRIBUTE_IDS = {
         COMPONENT_ID_ATTRIBUTE_ID, RELEASE_ATTRIBUTE_ID,
         RETAIN_SEVERITY_ATTRIBUTE_ID, COMMENT_LINE_ATTRIBUTE_ID,
         APAR_NUMBER_ATTRIBUTE_ID, KEYWORD_1_ATTRIBUTE_ID,
         KEYWORD_2_ATTRIBUTE_ID, KEYWORD_3_ATTRIBUTE_ID,
         CUSTOMER_NAME_ATTRIBUTE_ID, CONTACT_NAME_ATTRIBUTE_ID,
         CONTACT_PHONE_1_ATTRIBUTE_ID, CONTACT_PHONE_2_ATTRIBUTE_ID,
         EMAIL_ADDRESS_ATTRIBUTE_ID, CRITICAL_SITUATION_DESC_ATTRIBUTE_ID,
         NEXT_QUEUE_ATTRIBUTE_ID, NEXT_CENTER_ATTRIBUTE_ID };
   public static final String[] PMR_REOPEN_ATTRIBUTE_IDS = {
         CENTER_ATTRIBUTE_ID, QUEUE_ATTRIBUTE_ID, PMR_NO_ATTRIBUTE_ID,
         BRANCH_ATTRIBUTE_ID, COUNTRY_ATTRIBUTE_ID,
         CUSTOMER_NUMBER_ATTRIBUTE_ID };
   public static final String SBS_CLOSE_ACTION = "close";
   public static final String[] SBS_CLOSE_ATTRIBUTE_IDS = {
         PMR_NO_ATTRIBUTE_ID, BRANCH_ATTRIBUTE_ID, COUNTRY_ATTRIBUTE_ID,
         PMR_CHECKCALLS_ATTRIBUTE_ID, PROBLEM_STATUS_ATTRIBUTE_ID,
         CUSTOMER_NUMBER_ATTRIBUTE_ID };
   public static final String[] SBS_ADDCLOSETEXT_ACTION_ATTRIBUTE_IDS = {
         PMR_NO_ATTRIBUTE_ID, BRANCH_ATTRIBUTE_ID, COUNTRY_ATTRIBUTE_ID,
         PMR_CLOSETEXT_ATTRIBUTE_ID };
   public static final String SBS_SUBSCRIBE_ACTION = "subscribe";
   public static final String[] SBS_SUBSCRIBE_ACTION_ATTRIBUTE_IDS = {
         PMR_NO_ATTRIBUTE_ID, BRANCH_ATTRIBUTE_ID, COUNTRY_ATTRIBUTE_ID,
         SUB_QUEUE_ATTRIBUTE_ID, SUB_CENTER_ATTRIBUTE_ID,
         SUB_APARNUM_ATTRIBUTE_ID };
   public static final String SBS_UNSUBSCRIBE_ACTION = "unSubscribe";
   public static final String[] SBS_UNSUBSCRIBE_ACTION_ATTRIBUTE_IDS = {
         PMR_NO_ATTRIBUTE_ID, BRANCH_ATTRIBUTE_ID, COUNTRY_ATTRIBUTE_ID,
         SUB_APARNUM_ATTRIBUTE_ID };
}
