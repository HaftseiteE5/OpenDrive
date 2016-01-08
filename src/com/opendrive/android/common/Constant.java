package com.opendrive.android.common;

import android.os.Environment;

public class Constant {

    public static String APPPath = Environment.getExternalStorageDirectory() + "/OpenDrive/";
    public static String ICON_THUMBNAIL_PATH = Environment.getExternalStorageDirectory() + "/OpenDrive/Thumbnails/";
    public static String FOLDER_PATH = Environment.getExternalStorageDirectory()+"/OpenDrive/Files/";

    public static final String GA_ID = "UA-41973491-1";
    public static final String GA_SETTINGS_VIEW = "Settings view";
    public static final String GA_FILES_VIEW = "Files view";
    public static final String GA_AUTO_UPLOAD_SETTINGS_VIEW = "AutoUpload settings view";
    public static final String GA_UPLOAD_VIEW = "Upload view";
    public static final String GA_PASS_CODE_SETTINGS_VIEW = "Passcode settings view";

    public static final String GA_EVENT_CAT_ROTATING = "Rotating";
    public static final String GA_EVENT_CAT_DETAILS = "Details";
    public static final String GA_EVENT_CAT_DELETING = "Deleting";
    public static final String GA_EVENT_CAT_PREVIEW = "Preview";
    public static final String GA_EVENT_CAT_UPLOAD = "Upload";
    public static final String GA_EVENT_CAT_PLAYER = "Player";
    public static final String GA_EVENT_CAT_MOVE_TO_FOLDER = "MoveToFolder";
    public static final String GA_EVENT_CAT_COPY_LINK = "CopyLink";

    public static final String GA_EVENT_ACTION_ROTATE_PORTRAIT_SETTINGS = "try to rotate to portrait on Settings";
    public static final String GA_EVENT_ACTION_ROTATE_LANDSCAPE_SETTINGS = "try to rotate to landScape on Settings";
    public static final String GA_EVENT_ACTION_ROTATE_PORTRAIT_FILES = "rotate to portrait on Files";
    public static final String GA_EVENT_ACTION_ROTATE_LANDSCAPE_FILES = "rotate to landScape on Files";
    public static final String GA_EVENT_ACTION_USER_VIEWED_DETAILS = "user viewed Details";
    public static final String GA_EVENT_ACTION_WAS_USED_TRASH_BUTTON = "was used \"trash\" button";
    public static final String GA_EVENT_ACTION_IMG_DOC_FILE_WAS_OPENED = "image/word/powerpoint/excel/pdf/richtext/other file was opened";
    public static final String GA_EVENT_ACTION_USER_UPLOADING_FILES = "user uploading files";
    public static final String GA_EVENT_ACTION_USER_USED_MOVE_TO_FOLDER_BTN = "user used \"Move To Folder\" button";
    public static final String GA_EVENT_ACTION_AUDIO_FILE_WAS_OPENED = "audio file was opened";
    public static final String GA_EVENT_ACTION_VIDEO_FILE_WAS_OPENED = "video file was opened";
    public static final String GA_EVENT_ACTION_USER_USED_COPY_LNK_BTN = "user used \"Copy Link\" button";
    public static final String GA_EVENT_ACTION_WAS_USED_RENAME_BTN = "was used \"rename\" button";
    public static final String GA_EVENT_ACTION_DCM_FILE_WAS_OPENED = "DCM file was opened";

    public static final String GA_AUTO_LOGIN_DISABLED = "Auto LogIn disabled";
    public static final String GA_AUTO_LOGIN_ENABLED = "Auto LogIn enabled";
    public static final String GA_AUTO_UPLOAD_DISABLED = "Auto upload disabled";
    public static final String GA_AUTO_UPLOAD_ENABLED = "Auto upload enabled";
    public static final String GA_PASSCODE_DISABLED = "Passcode disabled";
    public static final String GA_PASSCODE_ENABLED = "Passcode enabled";

    public static String AMPString = "&amp;"; // "&"
    public static String LTString = "&lt;"; // "<"
    public static String GTString = "&gt;"; // ">"
    public static String QUOTString = "&quot;";// """
    public static String APOSString = "&apos;";// "'"

    public static String ErrorMessage = "ERROR";

    // public static String ServerURL = "http://199.255.236.53/"
    // public static String ServerURL = "https://www.opendrive.com/"
    public static String ServerURL = "https://ad1.opendrive.com";

    public static String OperationsURL = "api/app/1_3/1_3_14_3/od_operations.php";

    public static String APIName_OperationPath = "/od_operations.php";

    public static String APIPath = "/api/app/1_3/1_3_14_3";

    public static String SessionID = "";
    public static boolean accessUser = false;

    public static String PREFS_NAME = "SettingPrefsFile";
    public static String IS_FIRST_LOUNCH = "is_first_lounch";

    public static String UserName_Key = "UserName";
    public static String Password_Key = "Password";

    public static String UserName = "";
    public static String Password = "";

    public static String APIName_GetPath = "/od_get_path.php";
    public static String Documents_Folder = "Documents";
    public static String Tmp_Folder = "tmp";
    public static String Files_Folder = "files";
    public static String Thumbs_Folder = "thumbs";

    public static String Notification_PickedImage = "Notification_PickedImage";
    public static String Notification_MoviePlayerStart = "Notification_MoviePlayerStart";

    public static String ApiVersion = "1.3.14.3";
    public static String AppVersion = "2.1.2";
    public static String AppProVersion = "";

    public static String ServerResult_True = "True";
    public static String ServerResult_False = "False";

    public static final String MANAGER_PREFS_NAME = "ManagerPrefsFile"; // user preference file name
    public static final String PREFS_HIDDEN = "hidden";
    public static final String PREFS_COLOR = "color";
    public static final String PREFS_THUMBNAIL = "thumbnail";
    public static final String PREFS_SORT = "sort";
    public static final String PREFS_STORAGE = "sdcard space";

    public static int FileType_None = -1;
    public static int FileType_Image = 0;
    public static int FileType_Music = 1;
    public static int FileType_Movie = 2;
    public static int FileType_Word = 3;
    public static int FileType_PowerPoint = 4;
    public static int FileType_Excel = 5;
    public static int FileType_PDF = 6;
    public static int FileType_Web = 7;
    public static int FileType_RichText = 8;
    public static int FileType_Other = 9;

    public static String PostData_Login = "action=%s&user=%s&pass=%s&version=%s&pro_version=%s&pcid=%s";
    public static String PostData_Signup = "action=%s&user_name=%s&passwd=%s&verify_passwd=%s&email=%s&first_name=%s&last_name=%s&sharewithuserid=%s&folder_id=%s";
    public static String PostData_HasShareFolders = "action=%s&session_id=%s";
    public static String PostData_ListDir = "action=%s&session_id=%s&share_user_id=%s&share_id=%s&access_dir_id=%s&dir_id=%s";
    // public static String PostData_ListShareWithUsers s"list_share_whith_users"
    public static String PostData_ListSharedDirectoris = "action=%s&session_id=%s&share_user_id=%s";
    public static String PostData_ListSharedUsers = "action=%s&session_id=%s";
    public static String PostData_GetThumbnail = "action=%s&session_id=%s&share_user_id=%s&file_id=%s";
    public static String PostData_GetStorageInfo = "action=%s&session_id=%s";
    public static String PostData_OpenFileDownload = "action=%s&session_id=%s&is_pro=%s&share_user_id=%s&share_id=%s&access_dir_id=%s&file_id=%s&file_offset=%s";
    public static String PostData_CloseFileDownload = "action=%s&session_id=%s&is_pro=%s&share_user_id=%s&share_id=%s&access_dir_id=%s&file_id=%s";
    public static String PostData_OpenFileUpload = "action=%s&session_id=%s&is_pro=%s&share_user_id=%s&share_id=%s&access_dir_id=%s&file_id=%s&size=%s";
    public static String PostData_UploadFileChunk = "action=%s&session_id=%s&is_pro=%s&share_user_id=%s&share_id=%s&access_dir_id=%s&file_id=%s&temp_location=%s&chunk_offset=%s&chunk_size=%s";
    public static String PostData_CloseFileUpload = "action=%s&session_id=%s&is_pro=%s&share_user_id=%s&share_id=%s&access_dir_id=%s&file_id=%s&temp_location=%s&file_time=%s&file_size=%s";
    public static String PostData_CreateAndOpenFile = "action=%s&session_id=%s&is_pro=%s&share_user_id=%s&share_id=%s&access_dir_id=%s&dir_id=%s&name=%s&size=%s";
    public static String PostData_MoveFileToTrash = "action=%s&session_id=%s&share_user_id=%s&share_id=%s&access_dir_id=%s&file_id=%s";
    public static String PostData_Logout = "action=%s&session_id=%s";

    public static String ActionType_Login = "login";
    public static String ActionType_Signup = "new_profile";
    public static String ActionType_HasShareFolders = "has_share_folders";
    public static String ActionType_ListDir = "list_dir";
    public static String ActionType_ListShareWithUsers = "list_share_whith_users";
    public static String ActionType_ListSharedDirectoris = "list_shared_directories";
    public static String ActionType_ListSharedUsers = "list_shared_users";
    public static String ActionType_GetThumbnail = "get_thumbnail";
    public static String ActionType_GetStorageInfo = "get_storage_info";
    public static String ActionType_OpenFileDownload = "open_file_download";
    public static String ActionType_CloseFileDownload = "close_file_download";
    public static String ActionType_OpenFileUpload = "open_file_upload";
    public static String ActionType_UploadFileChunk = "upload_file_chunk";
    public static String ActionType_CloseFileUpload = "close_file_upload";
    public static String ActionType_CreateAndOpenFile = "create_and_open_file";
    public static String ActionType_MoveFileToTrash = "move_file_to_trash";
    public static String ActionType_Logout = "logout";

    public static String kActionKey = "action";
    public static String kSessionIdKey = "sessionId";
    public static String kUsernameKey = "user";
    public static String kPasswordKey = "passwd";
    public static String kVerifyPasswordKey = "verify_passwd";
    public static String kEmailKey = "email";
    public static String kFirstNameKey = "first_name";
    public static String kLastNameKey = "last_name";

    public static String kShareWithUserIdKey = "sharewithuserid";
    public static String kFolderIdKey = "folder_id";

    public static String kApiPathKey = "api_path";
    public static String kVersionKey = "version";
    public static String kProVersionKey = "pro_version";
    public static String kPCIDKey = "pcid";

    public static String kIsProKey = "is_pro";
    public static String kShareUserIdKey = "share_user_id";
    public static String kShareIdKey = "share_id";
    public static String kAccessDirIdKey = "access_dir_id";
    public static String kDirIdKey = "dir_id";
    public static String kFileIdKey = "file_id";
    public static String kFileOffsetKey = "file_offset";
    public static String kFileTimeKey = "file_time";
    public static String kSizeKey = "size";
    public static String kTempLocationKey = "temp_location";
    public static String kChunkOffsetKey = "chunk_offset";
    public static String kChunkSizeKey = "chunk_size";
    public static String kNameKey = "name";
    public static String kFNameKey = "file_name";
    public static String kFileFullPathKey = "file_full_path";

    public static int MainTimerState_Idel = 0;
    public static int MainTimerState_GetApiPath = 1;
    public static int MainTimerState_Login = 2;
    public static int MainTimerState_Signup = 3;
    public static int MainTimerState_HasShareFolders = 4;
    public static int MainTimerState_ListDir = 5;
    public static int MainTimerState_GetListShareWithUsers = 6;
    public static int MainTimerState_GetListSharedDirectories = 7;
    public static int MainTimerState_GetListSharedUsers = 8;
    public static int MainTimerState_GetThumbnail = 9;
    public static int MainTimerState_GetStorageInfo = 10;
    public static int MainTimerState_OpenFileDownload = 11;
    public static int MainTimerState_CloseFileDownload = 12;
    public static int MainTimerState_OpenFileUpload = 13;
    public static int MainTimerState_UploadFileChunk = 14;
    public static int MainTimerState_CloseFileUpload = 15;
    public static int MainTimerState_CreateAndOpenFile = 16;
    public static int MainTimerState_MoveFileToTrash = 17;
    public static int MainTimerState_Logout = 18;

    // Error macros
    public static int LoginError_EmptyUsername = 0;
    public static int LoginError_EmptyPassword = 1;
    public static int LoginError_Fail = 2;

    public static int SignupError_Fail = 0;
    public static int Signup_Success = 1;

    // DB macros
    public static String DB_NAME = "opendrive.db";
    public static String TableName_OpenDrive = "opendrive";

    public static String SQL_SelectAll = "SELECT * FROM %s WHERE parent_id='%s' ORDER BY %s DESC, upper(%s);";
    public static String SQL_DeleteTable = "DELETE FROM %s;";

    public static String SQL_Update_AllFiles_ToSaved = "UPDATE %s SET saved_for_offline=0;";
    public static String SQL_Update_File_ToSaved = "UPDATE %s SET saved_for_offline=%d WHERE id='%s' AND parent_id='%s';";
    public static String SQL_Delete_Directory = "DELETE FROM %s WHERE parent_id='%s'";
    public static String SQL_Delete_File = "DELETE FROM %s WHERE id='%s' AND parent_id='%s' AND saved_for_offline=0;";

    // For DataBase
    public static final String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/OpenDrive/opendrive.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "files";

    // Settings
    public static final String KEEP_LOGGED_IN = "keep_logged_in";
    public static final String PASS_CODE_TURNED = "pass_code_turned";
    public static final String ERASE_DATA = "erase_data";
    public static final String PASS_CODE = "pass_code";
    public static final String ENTER_PASS_CODE_MODE = "pass_code_mode";
    public static final int MODE_SET_NEW_PASSCODE = 0;
    public static final int MODE_TURN_PASSCODE_OFF = 1;
    public static final int MODE_CHANGE_PASSCODE = 2;
    public static final int MODE_ENTER_PASSCODE = 3;
    public static final int SPACE_USED = 0;
    public static final int SPACE_FREE = 1;

    public static final int WI_FI_ONLY = 1;
    public static final int WI_FI_CELL = 2;
    
    //Access
    public static final String ACCESS_PUBLIC = "1";
    public static final String ACCESS_HIDDEN = "2";
    public static final String ACCESS_PRIVATE = "0";
    
    
    public static final int PASSCODE_CHECK_REQUEST_CODE = 1234;

    //Auto Upload
    public static final String AUTO_UPLOAD_OPTION = "auto_upload_option";
    public static final String AUTO_UPLOAD_ENABLED = "auto_upload_enabled";
    public static final String EXTRA_SELECTED_FOLDER_NAME = "selected_folder_name";
    public static final int REQUEST_CODE_SELECT_FOLDER = 1;
    public static final String SELECTED_FOLDER_ID = "selected_folder_id";
    public static final String LAST_UPLOADED_PHOTO_ID = "last_uploaded_photo_id";
    public static final String LAST_UPLOADED_VIDEO_ID = "last_uploaded_video_id";

    public static final String DIR_NOT_EXISTS_ERROR_TYPE = "3";
    public static final String DIR_NOT_EXISTS_ERROR_NAME = "Directory request failed";
    public static final String DIR_NOT_EXISTS_ERROR_DESC = "Directory doesn't exist";



    public static final int REQUEST_CODE_MOVE = 3;
    public static final String ACTION_REFRESH = "com.opendrive.android.action_refresh";
    
    //Background Process
    public static final int BACKGROUND_PROCESS_OK = 1;
    public static final int BACKGROUND_PROCESS_ERROR = 2;
    public static final int BACKGROUND_UPLOAD_IMAGE_FILE = 3;
    public static final int BACKGROUND_SEND_SOME_SEAT = 4;
    public static final int BACKGROUND_CHECK_FILES_FOR_AUTO_UPLOAD = 5;
    public static final int BACKGROUND_DOWNLOAD_FILE = 6;
    public static final int BACKGROUND_DELETE_FILE = 7;
    public static final int BACKGROUND_CREATE_CHECK_NETWORK = 8;
    public static final int BACKGROUND_CHECK_SHARE_FOLDER = 9;
    public static final int BACKGROUND_GET_FILE_LIST = 10;
    public static final int BACKGROUND_GET_BACK_FILE_LIST = 11;
    public static final int BACKGROUND_DOWNLOAD_FILE_WITH_OPEN = 12;
    public static final int BACKGROUND_GET_SHARED_USERS_LIST = 13; 
    public static final int BACKGROUND_GET_SHARED_FOLDERS_LIST = 14;
    public static final int BACKGROUND_GET_SHARED_FILE_LIST = 15;
    public static final int BACKGROUND_GET_FILE_LIST_FROM_DB = 16;
    public static final int BACKGROUND_LOGIN = 17;
    public static final int BACKGROUND_SIGNUP = 18;
    public static final int BACKGROUND_NEW_FILE = 19;
    public static final int BACKGROUND_GET_STORAGE_INFO = 20;
    public static final int BACKGROUND_SPLASH_CREATE = 21;
}
