/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Credits:
 * Forked from NSIS Modern User Interface, Basic Example Script by Joost Verburg
 * http://nsis.sourceforge.net/Examples/Modern%20UI/Basic.nsi
 *
 * All the material from the NSIS wiki/site is licensed under the following zlib/libpng license (http://nsis.sourceforge.net/License):
 *
 * This software is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any damages arising from the use of this software.
 * Permission is granted to anyone to use this software for any purpose, including commercial applications, and to alter it and redistribute it freely, subject to the following restrictions:
 * 1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */
<%
	/*This file by itself is NOT a valid NSIS script; it's a template which is rendered to a
	NSIS script during the Gradle build, when some values fill the variables of the template
	See the renderNSISScript task in build.gradle for more relevant information.*/
%>

!include "MUI2.nsh"
!include "FileFunc.nsh"
!include "LogicLib.nsh"
!include "x64.nsh"

!define TRUE 1
!define FALSE 0

;--------------------------------
;Name and file
Name "<% print guiName %>"
OutFile "<% print outDir %>/<% print appName %>-setup.exe"
Unicode true
RequestExecutionLevel admin

;--------------------------------
;Include functions
!include "dirUtils.nsh"

;--------------------------------
;MUI2 Settings
!define MUI_ABORTWARNING
!define MUI_ICON "icon.ico"
!define MUI_UNICON "icon.ico"
!define MUI_WELCOMEFINISHPAGE_BITMAP "img.bmp"
!define MUI_FINISHPAGE_RUN "<% print appName %>.exe"
!define MUI_LANGDLL_REGISTRY_ROOT "HKLM"
!define MUI_LANGDLL_REGISTRY_KEY "Software\\<% print appName %>"
!define MUI_LANGDLL_REGISTRY_VALUENAME "InstallerLanguage"
!define MUI_LANGDLL_ALWAYSSHOW
!define MUI_FINISHPAGE_NOAUTOCLOSE

;--------------------------------
;Pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "<% print licenseFile %>"
!insertmacro MUI_PAGE_COMPONENTS
!define MUI_PAGE_CUSTOMFUNCTION_LEAVE verifyInstDir
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

;--------------------------------
;Languages

<%
	/* Groovy snippet to insert language strings */
	def languageFiles = new File(stringBundlesDir).listFiles();
	languageFiles.each {f ->
		Properties props = new Properties()
		props.load(new FileInputStream(f))
		def langName = props.getProperty("nsisLangName")
		/* load translations for NSIS built-in strings */
		println("!insertmacro MUI_LANGUAGE \"${langName}\"")
		/* load the custom strings */
		props.remove("nsisLangName")
		langName = langName.toUpperCase()
		props.each { k, v ->
			v = v.replaceAll("%guiName%", guiName).replaceAll("%minJREVersion%", minJREVersion)
			println("LangString ${k} \${LANG_${langName}} \"${v}\"")
		}
	}
%>

;--------------------------------
;Init functions
Var foundPreviousInstallation

!insertmacro MUI_RESERVEFILE_LANGDLL

!macro getInstDir
	StrCpy \$foundPreviousInstallation \${FALSE}
	;Default installation folder
	\${If} \${RunningX64}
		StrCpy \$INSTDIR "\$PROGRAMFILES64\\<% print appName %>"
		SetRegView 64
	\${Else}
		StrCpy \$INSTDIR "\$PROGRAMFILES\\<% print appName %>"
	\${EndIf}
	;Get installation folder from registry if available
	ReadRegStr \$0 HKLM "Software\\<% print appName %>" ""
	\${IfNot} \${Errors}
		StrCpy \$INSTDIR \$0
		StrCpy \$foundPreviousInstallation \${TRUE}
	\${EndIf}
	ClearErrors
!macroend

Function .onInit
	!insertmacro getInstDir
	!insertmacro MUI_LANGDLL_DISPLAY
FunctionEnd

Function un.onInit
	!insertmacro getInstDir
	!insertmacro MUI_UNGETLANGUAGE
FunctionEnd

;--------------------------------
;JRE detection
Var jvmVersionCheckerPath
Var regKey
Var javaIsWOW64

Function checkJavaVersion
	Pop \$regKey ;can be "SOFTWARE\\JavaSoft\\Java Runtime Environment", "SOFTWARE\\JavaSoft\\Java Development Kit", or their WOW6432Node equivalents
	StrCpy \$0 0 ;\$0=loop index
	cJV_loop:
	  EnumRegKey \$1 HKLM \$regKey \$0 ;\$1=subkey
	  StrCmp \$1 "" cJV_fail ;exit if we enumerated all the subkeys
		IntOp \$0 \$0 + 1 ;increment counter
	  ReadRegStr \$2 HKLM "\$regKey\\\$1" "JavaHome" ;\$2=home directory ov the JVM being analyzed
		DetailPrint "Processing JVM registry key \$regKey\\\$1..."
		DetailPrint "Checking version of JVM \$2..."
		ClearErrors
		ExecWait '"\$2\\bin\\java.exe" -jar "\$jvmVersionCheckerPath" "<% print minJREVersion %>"'
		IfErrors cJV_loop ;if command failed (nonzero exit code, outdated JVM) check for the next JVM in this key
		;if we get here, the JVM version is OK
		Return
	cJV_fail:
		SetErrors
		Return
FunctionEnd

Function noJava
	Delete "\$jvmVersionCheckerPath"
	MessageBox MB_OK|MB_ICONSTOP \$(noJava)
	SetErrorLevel 1
	Quit
FunctionEnd

Function checkWOW64Java
	;Show performance warning if flag for 32-bit Java on 64-bit OS was set
	;and set RegView back to 64, otherwise new keys would be put under WOW6432Node
	\${If} \$javaIsWOW64 = \${TRUE}
		MessageBox MB_OK|MB_ICONEXCLAMATION \$(wow64Java)
		SetRegView 64
	\${EndIf}
FunctionEnd

Function detectJRE
	!macro detectJavaVersion regKey
		Push "\${regKey}"
		Call checkJavaVersion
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
		!insertmacro detectJRE
		!insertmacro detectJDK ;The previous one returns from the function if JRE found
		;If we get here, neither JRE nor JDK were found
	\${EndIf}
	Call noJava
FunctionEnd

;--------------------------------
;Install directory validation

Function verifyInstDir
	\${If} \$foundPreviousInstallation = \${TRUE}
		;If the program is already installed, the new installation must overwrite the previous one
		ReadRegStr \$0 HKLM "Software\\<% print appName %>" ""
		\${If} \$0 != \$INSTDIR
			MessageBox MB_OK "\$(mustReplace)"
			Abort
		\${EndIf}
	\${Else}
		;If the program isn't already installed, the install directory must not exist or be empty
		\${If} \${DirExists} "\$INSTDIR"
			Push "\$INSTDIR"
			Call isEmptyDir
			Pop \$0
			\${If} \$0 = 0
				MessageBox MB_OK "\$(badInstDir)"
				Abort
			\${EndIf}
		\${EndIf}
	\${EndIf}
FunctionEnd

;--------------------------------
;Installer Sections

Section "<% print guiName %>" SecMain
	; Installation of core files can't be disabled
	SectionIn RO
	; Check for JRE
	GetTempFileName \$jvmVersionCheckerPath
	File "/oname=\$jvmVersionCheckerPath" jvm-version-check.jar
	Call detectJRE
	Delete "\$jvmVersionCheckerPath"
	; Install application's files
	SetOutPath "\$INSTDIR"
	File /r "<% print appName %>\\*.*"
	; Instruct the launcher that if a new version is found, the update must take place via the new installer (not by self replacing files)
	FileOpen \$4 "\$INSTDIR\\updateStrategy" w
	FileWrite \$4 "WIN_INSTALLER"
	FileClose \$4
	; Store installation folder
	WriteRegStr HKLM "Software\\<% print appName %>" "" \$INSTDIR
	; Show in add/remove programs
	WriteRegStr HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>" "DisplayName" "<% print guiName %>"
	WriteRegStr HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>" "DisplayVersion" "<% print verString %>"
	WriteRegStr HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>" "UninstallString" "\$\"\$INSTDIR\\Uninstall.exe\$\""
	WriteRegStr HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>" "QuietUninstallString" "\$\"\$INSTDIR\\Uninstall.exe\$\" /S"
	WriteRegStr HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>" "DisplayIcon" "\$\"\$INSTDIR\\<% print appName %>.exe\$\""
	WriteRegStr HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>" "Publisher" "<% print publisher %>"
	WriteRegDWORD HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>" "NoModify" 1
	WriteRegDWORD HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>" "NoRepair" 1
	WriteRegDWORD HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>" "VersionMajor" <% print verMajor %>
	WriteRegDWORD HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>" "VersionMinor" <% print verMinor %>
	\${GetSize} "\$INSTDIR" "/S=0K" \$0 \$1 \$2 ; Compute EstimatedSize
	IntFmt \$0 "0x%08X" \$0
	WriteRegDWORD HKLM "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\<% print appName %>" "EstimatedSize" "\$0"
	;Create uninstaller
	WriteUninstaller "\$INSTDIR\\Uninstall.exe"
SectionEnd

Section \$(startMenuShortcut) SecStartShortcut
	SetShellVarContext all
	SetOutPath "\$INSTDIR"
	CreateDirectory "\$SMPROGRAMS\\<% print guiName %>"
	CreateShortCut "\$SMPROGRAMS\\<% print guiName %>\\<% print guiName %>.lnk" "\$INSTDIR\\<% print appName %>.exe"
	CreateShortCut "\$SMPROGRAMS\\<% print guiName %>\\Uninstall.lnk" "\$INSTDIR\\Uninstall.exe"
SectionEnd

Section \$(desktopShortcut) SecDesktopShortcut
	SetShellVarContext all
	SetOutPath "\$INSTDIR"
	CreateShortCut "\$DESKTOP\\<% print guiName %>.lnk" "\$INSTDIR\\<% print appName %>.exe"
SectionEnd

;Assign language strings to sections
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
!insertmacro MUI_DESCRIPTION_TEXT \${SecMain} \$(desc_core)
!insertmacro MUI_DESCRIPTION_TEXT \${SecStartShortcut} \$(desc_startMenuShortcut)
!insertmacro MUI_DESCRIPTION_TEXT \${SecDesktopShortcut} \$(desc_desktopShortcut)
!insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller

Section "Uninstall"
	ClearErrors
	ExecWait '"\$INSTDIR\\<% print appName %>.exe" --cleanup'
	\${If} \${Errors}
		MessageBox MB_YESNO|MB_ICONEXCLAMATION \$(cleanupWarning) IDYES continue
		SetErrorLevel 1
		Quit
		continue:
	\${EndIf}
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
