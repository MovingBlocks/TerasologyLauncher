
#include <Windows.h>
#include <iostream>
#include <string>
#include <vector>
#include <memory>

using namespace std;

static const wstring jliDllPath = L"jre\\bin\\jli.dll";

struct FreeLibraryDeleter
{
	typedef HMODULE pointer;

	void operator()(HMODULE h) { ::FreeLibrary(h); }
};

typedef std::unique_ptr<HMODULE, FreeLibraryDeleter> unique_library;

extern "C" {
	typedef int(*JLI_Launch)(int argc, char ** argv,              /* main argc, argc */
		int jargc, const char** jargv,          /* java args */
		int appclassc, const char** appclassv,  /* app classpath */
		const char* fullversion,                /* full version defined */
		const char* dotversion,                 /* dot version defined */
		const char* pname,                      /* program name */
		const char* lname,                      /* launcher name */
		bool javaargs,                      /* JAVA_ARGS */
		bool cpwildcard,                    /* classpath wildcard*/
		bool javaw,                         /* windows-only javaw */
		int ergo                               /* ergonomics class policy */
		);
}

static vector<wstring> getWideArgs() {
	LPWSTR wcmdline = GetCommandLineW();
	int numargs = 0;
	LPWSTR *cmdLine = CommandLineToArgvW(wcmdline, &numargs);
	vector<wstring> result;
	for (int i = 0; i < numargs; ++i) {
		result.push_back(wstring(cmdLine[i]));
	}
	return result;
}

static wstring getLastErrorString() {
	DWORD errorCode = GetLastError();
	LPTSTR message;

	DWORD fmtMsgRes = FormatMessage(
		FORMAT_MESSAGE_ALLOCATE_BUFFER|FORMAT_MESSAGE_FROM_SYSTEM,
		NULL,
		errorCode,
		0,
		(LPTSTR)&message,
		0,
		0);

	if (!fmtMsgRes) {
		return wstring(L"Unknown Error (") + to_wstring(errorCode) + wstring(L")");
	}

	wstring result(message);
	LocalFree(message);
	return result;
}

static HMODULE loadLibrarySafe(wstring path) {
	HMODULE result = LoadLibrary(path.c_str());

	// Load the Java Library
	if (!result) {
		auto sysError = getLastErrorString();

		wstring errorMessage = L"Your Terasology installation is missing required files.\nUnable to find ";
		errorMessage += path;
		errorMessage += L"\n" + sysError;
		MessageBox(NULL, errorMessage.c_str(), L"Missing Required Files", MB_OK | MB_ICONERROR);
		return 0;
	}

	return result;
}


static const char* const_progname = "java";

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR pCmdLine, int nCmdShow)
{
	wstring wideOwnPath = getWideArgs()[0];
	wideOwnPath = wideOwnPath.substr(0, wideOwnPath.find_last_of(L"\\/") + 1);
	
	wstring dllPath = wideOwnPath + jliDllPath;
	unique_library jliDll(loadLibrarySafe(dllPath));

	if (!jliDll)
		return -1;

	JLI_Launch jliLaunch = (JLI_Launch)GetProcAddress(jliDll.get(), "JLI_Launch");

	if (!jliLaunch) {
		MessageBox(NULL, L"Your jli.dll is corrupted.", L"Corrupted Installation", MB_OK | MB_ICONERROR);
		return -2;
	}

	int realArgc = __argc + 2;
	char ** realArgv = new char*[realArgc];
	for (int i = 0; i < __argc; ++i) {
		realArgv[i] = __argv[i];
	}
	realArgv[__argc] = "-jar";
	
	string ownPath = __argv[0];
	ownPath = ownPath.substr(0, ownPath.find_last_of("\\/") + 1);
	string jarPath = ownPath + "lib\\TerasologyLauncher.jar";

	realArgv[__argc + 1] = const_cast<char*>(jarPath.c_str());

	int result = jliLaunch(
		realArgc, realArgv,
		0, 0,
		0, 0,
		"1.8",
		"1.8",
		*__argv,
		*__argv,
		false,
		true, 
		true, 
		0
		);

	delete[] realArgv;

	return result;
}
