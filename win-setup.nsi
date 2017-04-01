;Forked from NSIS Modern User Interface, Basic Example Script by Joost Verburg
; http://nsis.sourceforge.net/Examples/Modern%20UI/Basic.nsi
;--------------------------------
!include "MUI2.nsh"
!include "FileFunc.nsh"
!include "LogicLib.nsh"

;Name and file
Name "<% print guiName %>"
OutFile "<% print outDir %>/<% print appName %>-setup.exe"
;Default installation folder
InstallDir "\$PROGRAMFILES\\<% print appName %>"
;Get installation folder from registry if available
InstallDirRegKey HKLM "Software\\<% print appName %>" ""
RequestExecutionLevel admin

;--------------------------------
;Interface Settings
!define MUI_ABORTWARNING

;--------------------------------
;Pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "<% print licenseFile %>"
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

;--------------------------------
;Languages
!insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

Section "<% print guiName %>" SecMain
	;Installation of core files can't be disabled
	SectionIn RO
	;Install application's files
	SetOutPath "\$INSTDIR"
	File /r "<% print appName %>\\*.*"
	;Store installation folder
	WriteRegStr HKLM "Software\\<% print appName %>" "" \$INSTDIR
	; Show in add/remove programs
	WriteRegStr HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>" "DisplayName" "<% print guiName %>"
	WriteRegStr HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>" "DisplayVersion" "<% print appVersion %>"
	WriteRegStr HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>" "UninstallString" "\$\"\$INSTDIR\\Uninstall.exe\$\""
	WriteRegStr HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>" "QuietUninstallString" "\$\"\$INSTDIR\\Uninstall.exe\$\" /S"
	\${GetSize} "\$INSTDIR" "/S=0K" \$0 \$1 \$2 ; Compute EstimatedSize
	IntFmt \$0 "0x%08X" \$0
	WriteRegDWORD HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>" "EstimatedSize" "\$0"
	;Create uninstaller
	WriteUninstaller "\$INSTDIR\\Uninstall.exe"
SectionEnd


Section "Start menu shortcut" SecStartShortcut
	SetShellVarContext all
	SetOutPath "\$INSTDIR"
	CreateDirectory "\$SMPROGRAMS\\<% print guiName %>"
	CreateShortCut "\$SMPROGRAMS\\<% print guiName %>\\<% print guiName %>.lnk" "\$INSTDIR\\<% print appName %>.exe"
	CreateShortCut "\$SMPROGRAMS\\<% print guiName %>\\Uninstall.lnk" "\$INSTDIR\\Uninstall.exe"
SectionEnd

Section "Desktop shortcut" SecDesktopShortcut
	SetShellVarContext all
	SetOutPath "\$INSTDIR"
	CreateShortCut "\$DESKTOP\\<% print guiName %>.lnk" "\$INSTDIR\\<% print appName %>.exe"
SectionEnd

;Language strings
LangString DESC_SecMain \${LANG_ENGLISH} "<% print guiName %> core files."
LangString DESC_SecStartShortcut \${LANG_ENGLISH} "Folder in the start menu with links to launch <% print guiName %> and to uninstall it."
LangString DESC_SecDesktopShortcut \${LANG_ENGLISH} "Shortcut to launch <% print guiName %> from your desktop."

;Assign language strings to sections
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
!insertmacro MUI_DESCRIPTION_TEXT \${SecMain} \$(DESC_SecMain)
!insertmacro MUI_DESCRIPTION_TEXT \${SecStartShortcut} \$(DESC_SecStartShortcut)
!insertmacro MUI_DESCRIPTION_TEXT \${SecDesktopShortcut} \$(DESC_SecDesktopShortcut)
!insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"
	SetShellVarContext all
	RMDir /r "\$INSTDIR"
	DeleteRegKey /ifempty HKLM "Software\\<% print appName %>"
	DeleteRegKey HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>"
	Delete "\$SMPROGRAMS\\<% print guiName %>\\<% print guiName %>.lnk"
	Delete "\$SMPROGRAMS\\<% print guiName %>\\Uninstall.lnk"
	RMDir "\$SMPROGRAMS\\<% print guiName %>"
	Delete "\$DESKTOP\\<% print guiName %>.lnk"
SectionEnd
