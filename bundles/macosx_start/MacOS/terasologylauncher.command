#!/bin/bash
cd "$(dirname "$0")/../Resources/"
jre/bin/java -Xms128m -Xmx512m -jar lib/TerasologyLauncher.jar
