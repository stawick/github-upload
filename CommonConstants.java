package com.ibm.sport.rtc.common;

public class CommonConstants
{

   /* real version info is added by top-level build script */
   public static final String BUILD_NAME = new String( "Not Specified" );
   public static final String FULL_VERSION = new String( "Not Specified" );
   public static final String VERSION = new String( "Not Specified" );

   public static final String RETAIN_EXCEPTION = new String(
         "RETAIN Exception" );

   public static final String SBS_SAVE_ATTRIBUTE_ID = "com.ibm.sport.rtc.workItem.attribute.${workItemType}."
         + "sbsSave";

   public static final String ACTION_INITIATOR_ATTRIBUTE_ID = "com.ibm.sport.rtc.workItem.attribute.${workItemType}."
         + "actionInitiator";

   // _T26063A - Workaround for Required/Read-Only bypass
   public static final String SPORT_INTERNAL_BYPASS_ATTRIBUTE_ID = "com.ibm.sport.rtc.workItem.attribute.${workItemType}."
         + "sportInternalWorkitemSave";

   // _B93726A Initial work item state additional save parameter name.
   // Lets make the additional save attribute look official, even thought it
   // wont actually be added to any work item.
   public static final String RTCSPORT_ADDITIONAL_SAVE_INITIAL_STATE_ATTRIBUTE_ID = "com.ibm.sport.rtc.workItem.attribute.${workItemType}."
         + "sbsInitialWorkItemState";

   /**
    * _B111803A Define our own SPoRT SKIP MAIL additional save parameter
    * constant so the build is not relying on a specific RTC level. It
    * corresponds to the RTC constant
    * <b>IAdditionalSaveParameters.SKIP_MAIL</b> . <br>
    * We would only need to change this if RTC changed the value, which may
    * cause other issues, so that probability is low. This additional save
    * parameter is used to inform RTC to not send email notifications for
    * attribute changes on our internal work item saves.
    */
   public static final String SPORT_ADDITIONAL_SAVE_SKIPMAIL_ID = "com.ibm.team.workitem.common.internal.skipMail";

   public static final String SPORT_RTC_PLUGIN_PREFIX = "com.ibm.sport.rtc";

   public static final String SPORT_PROCESS_ID = "com.ibm.sport.process.sport";

   // _T155539A Unique PMR by name and RETAIN create date(/time)
   public static final String RTC_TIMESTAMP_FORMAT_PATTERN = new String(
         "yyyy/MM/dd HH:mm" );

   // _T266500A Needed for SeparatedStringList handling.
   public static final String LINEFEED = new String( "\n" );
   public static final String CARRIAGE_RETURN = new String( "\r" );
   // Escape character(s) needed since these are special regex symbols.
   public static final String SQUARE_BRACKET_LEFT = new String( "\\[" );
   public static final String SQUARE_BRACKET_RIGHT = new String( "\\]" );
   /**
    * Regex pattern to split strings that are whitespace or comma separated.
    */
   public static final String COMMA_ANDOR_SPACE_SPLIT_PATTERN = new String(
         "(,|\\s)+" );
   /**
    * Regex pattern to split strings that are comma + whitespace
    * separated.
    */
   public static final String COMMA_AND_SPACE_SPLIT_PATTERN = new String(
         "(,\\s)+" );
   /**
    * Regex pattern to generically match a newline where the \r
    * carriage return is optional - ie "(\r?\n)".
    * Note: Where the pattern matches in a "split", it is removed
    * from the strings returned in the array entries.
    */
   public static final String LINE_SPLIT_PATTERN = "(" + CARRIAGE_RETURN
         + "?" + LINEFEED + ")";

}
