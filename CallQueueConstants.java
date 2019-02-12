package com.ibm.sport.rtc.common;

public class CallQueueConstants
{
   public static final String ATTRIBUTE_ID_PREFIX = "com.ibm.sport.rtc.workItem.attribute.callqueue.";
   public static final String WORK_ITEM_TYPE_ID = "com.ibm.sport.rtc.workItem.type.callqueue";

   public static final String BRANCH_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "branch";
   public static final String CALL_LIST_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "callList1"; // _B68102C
   public static final String CALLQUEUE_NAME_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "callqueueName";
   public static final String CENTER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "center";
   public static final String COUNTRY_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "country";
   public static final String CUSTOMER_NUMBER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "customerNumber";
   public static final String MODIFY_ACTION_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "modifyAction";
   public static final String PMR_NAME_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "pmrName";
   public static final String QUEUE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "queue";
   public static final String SELECTED_CALL_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "selectedCall";
   public static final String TARGET_QUEUE_CENTER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "targetQueueCenter";
   public static final String UNFORMATTED_CALL_LIST_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "unformattedCallList";
   public static final String SERVICE_GIVEN_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "serviceGiven";
   public static final String FOLLOWUP_INFO_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "followupInfo";
   public static final String PRIMARY_FUP_QUEUE_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "primaryFupQueue";
   public static final String PRIMARY_FUP_CENTER_ATTRIBUTE_ID = ATTRIBUTE_ID_PREFIX
         + "primaryFupCenter";

   public static final String RTC_CALL_CREATE_ACTION_FOR_EXISTING_PMR = "Call Create for Existing PMR";
   public static final String RTC_CALL_CREATE_ACTION_WITH_NEW_PMR = "Call Create with New PMR";
   public static final String RTC_CALL_COMPLETE_ACTION = "Call Complete";
   public static final String RTC_CALL_CONVERT_ACTION = "Call Convert";
   public static final String RTC_CALL_DISPATCH_ACTION = "Call Dispatch";
   public static final String RTC_CALL_REQUEUE_ACTION = "Call Requeue";
   public static final String RTC_CREATE_ACTION = "Create";
   public static final String RTC_ENTER_MODIFY_ACTION_ACTION = "Enter Modify Action";
   public static final String RTC_REFRESH_FROM_RETAIN_ACTION = "Refresh from RETAIN";

   public static final String SBS_CALL_CREATE_ACTION = "create";
}
