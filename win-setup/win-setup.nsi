;Forked from NSIS Modern User Interface, Basic Example Script by Joost Verburg
; http://nsis.sourceforge.net/Examples/Modern%20UI/Basic.nsi
;Useful clarifications from https://nsis-dev.github.io/NSIS-Forums/html/t-356394.html
;--------------------------------
!include "MUI2.nsh"
!include "FileFunc.nsh"
!include 'LogicLib.nsh'
!include 'x64.nsh'
!include "WordFunc.nsh" ;For VersionCompare

!define TRUE 1
!define FALSE 0

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
;JRE detection
 ;Unfortunately, NSIS only has global variables
Var regKey
Var installedVersion
Var versionComparison
Var javaIsWOW64

Function detectJavaVersion
	Pop \$regKey
	ClearErrors
	ReadRegStr \$installedVersion HKLM \$regKey "CurrentVersion"
	IfErrors fail
	\${VersionCompare} "<% print minJREVersion %>" \$installedVersion \$versionComparison
	IntCmp \$versionComparison 1 fail ;if min is newer than installed, jump to JRE_old
	ClearErrors
	Return
	fail:
		SetErrors
		Return
FunctionEnd

Function noJava
	MessageBox MB_OK|MB_ICONSTOP "A suitable Java installation to run this program has not been found. This program requires Java Runtime Environment at least <% print minJREVersion %>; \\
		you can download the latest available version from https://www.java.com. Once you downloaded and installed it, run this installer again to install <% print guiName %>. The installer will now quit."
	SetErrorLevel 1
	Quit
FunctionEnd

Function checkWOW64Java
	;Show performance warning if flag for 32-bit Java on 64-bit OS was setup
	;and set RegView back to 64, otherwise new keys will be put under WOW6432Node
	\${If} \$javaIsWOW64 = \${TRUE}
		MessageBox MB_OK|MB_ICONEXCLAMATION "This is a 64-bit system, but only a 32-bit Java installation suitable for this program was found. \\
			This will limit the performance of the program you are installing (it won't be able to use more than 4GB of memory). \\
			You can install a 64-bit Java Runtime Environment from https://www.java.com. This program requires at least version <% print minJREVersion %>."
		SetRegView 64
	\${EndIf}
FunctionEnd

Function detectJRE
	!macro detectJavaVersion regKey
		Push "\${regKey}"
		Call detectJavaVersion
		\${IfNot} \${Errors}
			Call checkWOW64Java
			Return
		\${EndIf}
	!macroend
	!macro detectJRE
		!insertmacro detectJavaVersion "SOFTWARE\\JavaSoft\\Java Runtime Environment"
	!macroend
	!macro detectJDK
		!insertmacro detectJavaVersion "SOFTWARE\\JavaSoft\\Java Development Kit"
	!macroend
	StrCpy \$javaIsWOW64 \${FALSE}
	\${If} \${RunningX64}
    ;We are running on a 64-bit Windows installation
		DetailPrint "Running on a 64-Bit Windows installation."
		SetRegView 64
		!insertmacro detectJRE
		!insertmacro detectJDK ;The previous one returns from the function if JRE found
		;If we get here, no native (64-bit) JRE or JDK were found
		SetRegView 32 ;Look for the same keys, but will be redirected to the ones under WOW6432Node
		StrCpy \$javaIsWOW64 \${TRUE} ;flag to show warning message
		!insertmacro detectJRE
		!insertmacro detectJDK
		;If here, neither 64-bit or 32-bit JREs nor JDKs were found
		SetRegView 64
	\${Else}
    ;We are running on a 32-bit Windows installation
		DetailPrint "Running on a 32-Bit Windows installation."
		!insertmacro detectJRE
		!insertmacro detectJDK ;The previous one returns from the function if JRE found
		;If we get here, neither JRE nor JDK were found
	\${EndIf}
	Call noJava
FunctionEnd

;--------------------------------
;Installer Sections

Section "<% print guiName %>" SecMain
	;Installation of core files can't be disabled
	SectionIn RO
	; Check for JRE
	Call detectJRE
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
	\${If} \${RunningX64}
		SetRegView 64
	\${EndIf}
	RMDir /r "\$INSTDIR"
	DeleteRegKey /ifempty HKLM "Software\\<% print appName %>"
	DeleteRegKey HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>"
	Delete "\$SMPROGRAMS\\<% print guiName %>\\<% print guiName %>.lnk"
	Delete "\$SMPROGRAMS\\<% print guiName %>\\Uninstall.lnk"
	RMDir "\$SMPROGRAMS\\<% print guiName %>"
	Delete "\$DESKTOP\\<% print guiName %>.lnk"
SectionEnd
