

/// LABELING_SECTION_START
// ccImport admin fannel @puutaro
// --
// --
// bellow setting variable main line up
// * terminalFontZoom adjust terminal font size (percentage)
// * terminalFontColor adjust terminal font color
// * terminalColor adjust terminal background color
/// LABELING_SECTION_END


/// SETTING_SECTION_START
terminalDo="OFF"
editExecute="ALWAYS"
overrideItemClickExec="ON"
disableSettingButton="ON"
disableEditButton="ON"
disablePlayButton="ON"
terminalFontZoom="0"
terminalColor=""
terminalFontColor=""
execPlayBtnLongPress=""
execEditBtnLongPress=""
setReplaceVariable="LIST_DIR_PATH=listDir"
setReplaceVariable="LIST_PREFIX=prefix"
setReplaceVariable="LIST_SUFFIX=suffix"
setReplaceVariable="CMDCLICK_ROOT_DIR_PATH=${00}"
setReplaceVariable="CMDCLICK_CONF_DIR_PATH=${CMDCLICK_ROOT_DIR_PATH}/conf"
setReplaceVariable="CMDCLICK_APP_DIR_ADMIN_DIR_PATH=${CMDCLICK_CONF_DIR_PATH}/AppDirAdmin"
setVariableType="appDirAdminList:LI=${LIST_DIR_PATH}=${CMDCLICK_APP_DIR_ADMIN_DIR_PATH}!${LIST_SUFFIX}=.js|menu=add_app_dir!delete!copy_app_dir!rename_app_dir!sync"
scriptFileName="appDirManager.js"
/// SETTING_SECTION_END


/// CMD_VARIABLE_SECTION_START
appDirAdminList=""
/// CMD_VARIABLE_SECTION_END


/// Please write bellow with javascript


let args = jsArgs.get().split("\t");
var FIRST_ARGS = args.at(0);