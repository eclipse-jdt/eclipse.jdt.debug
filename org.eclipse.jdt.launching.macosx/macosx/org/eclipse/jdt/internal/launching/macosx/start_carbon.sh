
#********************************************************************** 
# Copyright (c) 2002, 2003 IBM Corp.  All rights reserved.
# This file is made available under the terms of the Common Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/cpl-v10.html
# 
# Andre Weinand, OTI - Initial version
#********************************************************************** 

# 
# This script wrappers a SWT based Java application on the fly as an
# application bundle and launches it.
#
# Why is it necessary?
# When using Carbon via JNI some magic is necessary to make MacOS X
# recognize the Java program as a 'real Mac application' with its own menu bar 
# and an entry in the Dock. Since this 'magic' does not seem to be public and
# documented API I don't know how to call it from the SWT startup code.
# As a workaround I've tried to simulate what ProjectBuilder or MRJAppBuilder
# do if they launch a SWT based Java application.
#
# How is it used?
# Basically by replacing the standard Java VM ('/usr/bin/java') with this script.
# Since this script is a replacement for the VM it takes roughly the same arguments.

#
# Where to build the temporary application bundle
#
TMP_APP_DIR="/tmp/swt_stubs"

#
# We remember the current working directory
# so that we can later define the property "WorkingDirectory"
#
CURRENT_DIR="$PWD"

#echo $* > /dev/console

# extract JVM version from 1st argument
JVM_VERSION="1.3.1"
if test "$1" = "/System/Library/Frameworks/JavaVM.framework/Versions/1.4.1/Home/bin/java"
then
	JVM_VERSION="1.4.1"
fi

# skip 1st argument
shift

#
# Process command line arguments until we see the main class...
#
VM_OPTIONS=""
while [ $# -gt 0 ]; do
	case "$1" in
		-classpath | -cp )
			CLASS_PATH="$2"
			shift;
			;;	
		-* )
			VM_OPTIONS="$VM_OPTIONS<string>$1</string>"
			;;
		* )
			MAIN_CLASS="$1"
			shift;
			break;
			;;
	esac
	shift
done

while [ $# -gt 0 ]; do
	PARAMETERS="$PARAMETERS<string>$1</string>"
	shift
done

#
# Application name is name of main class without package prefix 
#
APP_NAME=`echo $MAIN_CLASS | awk -F. '{ print $(NF) }' `
LAUNCHER="$APP_NAME"

#
# Create the parent directory for the application bundle 
#
mkdir -p "$TMP_APP_DIR"
cd "$TMP_APP_DIR"

#
# Create the application bundle 
#
rm -rf "$APP_NAME.app"
mkdir -p "$APP_NAME.app/Contents/MacOS"

cd "$APP_NAME.app/Contents"

#
# Copy the JavaAppLauncher into the bundle 
#
cp "$JAVASTUB"/JavaApplicationStub MacOS
chmod +x MacOS/JavaApplicationStub

#
# Create the Info.plist file.
#
cat > Info.plist <<End_Of_Input
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple Computer//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>CFBundleDevelopmentRegion</key>
		<string>English</string>
	<key>CFBundleExecutable</key>
		<string>JavaApplicationStub</string>
	<key>CFBundleGetInfoString</key>
		<string>$APP_NAME</string>
	<key>CFBundleInfoDictionaryVersion</key>
		<string>6.0</string>
	<key>CFBundleName</key>
		<string>$APP_NAME</string>
	<key>CFBundlePackageType</key>
		<string>APPL</string>
	<key>CFBundleShortVersionString</key>
		<string>2.0.1</string>
	<key>CFBundleSignature</key>
		<string>????</string>
	<key>CFBundleVersion</key>
		<string>1.0.1</string>
	<key>Java</key>
	<dict>
	    <key>JVMVersion</key>
			<string>$JVM_VERSION</string>
		<key>VMOptions</key>
			<array>$VM_OPTIONS</array>
		<key>ClassPath</key>
			<string>$CLASS_PATH</string>
		<key>MainClass</key>
			<string>$MAIN_CLASS</string>
		<key>WorkingDirectory</key>
			<string>$CURRENT_DIR</string>
		<key>Arguments</key>
			<array>$PARAMETERS</array>
	</dict>
</dict>
</plist>
End_Of_Input

#
# Start the JavaAppLauncher by replacing this shell script
# to ensure that the process id is preserved.
#
exec "$TMP_APP_DIR/$APP_NAME.app/Contents/MacOS/JavaApplicationStub"

#
# not reached (as long as the exec from above succeeds).
#
