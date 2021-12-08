#!/bin/sh
#
# Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
# and other contributors as indicated by the @author tags.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


DIRNAME=`dirname "$0"`
PROGNAME=`basename "$0"`
GREP="grep"

# Use the maximum available, or set MAX_FD != -1 to use that
MAX_FD="maximum"

# tell linux glibc how many memory pools can be created that are used by malloc
MALLOC_ARENA_MAX="${MALLOC_ARENA_MAX:-1}"
export MALLOC_ARENA_MAX

# OS specific support (must be 'true' or 'false').
cygwin=false;
darwin=false;
linux=false;
solaris=false;
freebsd=false;
other=false
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;

    Darwin*)
        darwin=true
        ;;
    FreeBSD)
        freebsd=true
        ;;
    Linux)
        linux=true
        ;;
    SunOS*)
        solaris=true
        ;;
    *)
        other=true
        ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$UBLHUB_HOME" ] &&
        UBLHUB_HOME=`cygpath --unix "$UBLHUB_HOME"`
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$JAVAC_JAR" ] &&
        JAVAC_JAR=`cygpath --unix "$JAVAC_JAR"`
fi

# Setup UBLHUB_HOME
RESOLVED_UBLHUB_HOME=`cd "$DIRNAME/.." >/dev/null; pwd`
if [ "x$UBLHUB_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    UBLHUB_HOME=$RESOLVED_UBLHUB_HOME
else
 SANITIZED_UBLHUB_HOME=`cd "$UBLHUB_HOME"; pwd`
 if [ "$RESOLVED_UBLHUB_HOME" != "$SANITIZED_UBLHUB_HOME" ]; then
   echo ""
   echo "   WARNING:  UBLHUB_HOME may be pointing to a different installation - unpredictable results may occur."
   echo ""
   echo "             UBLHUB_HOME: $UBLHUB_HOME"
   echo ""
   sleep 2s
 fi
fi
export UBLHUB_HOME

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

# determine the default base dir, if not set
if [ "x$UBLHUB_BASE_DIR" = "x" ]; then
   UBLHUB_BASE_DIR="$UBLHUB_HOME/standalone"
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    UBLHUB_HOME=`cygpath --path --windows "$UBLHUB_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
fi

# Display our environment
echo "========================================================================="
echo ""
echo "  Ublhub Bootstrap Environment"
echo ""
echo "  UBLHUB_HOME: $UBLHUB_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "========================================================================="
echo ""

cd "$UBLHUB_HOME";
while true; do
   if [ "x$LAUNCH_UBLHUB_IN_BACKGROUND" = "x" ]; then
      # Execute the JVM in the foreground
      eval \"$JAVA\" \
         -jar \""$UBLHUB_HOME"/quarkus-run.jar\"
      UBLHUB_STATUS=$?
   else
      # Execute the JVM in the background
      eval \"$JAVA\" \
         -jar \""$UBLHUB_HOME"/quarkus-run.jar\"
      UBLHUB_PID=$!
      # Trap common signals and relay them to the ublhub process
      trap "kill -HUP  $UBLHUB_PID" HUP
      trap "kill -TERM $UBLHUB_PID" INT
      trap "kill -QUIT $UBLHUB_PID" QUIT
      trap "kill -PIPE $UBLHUB_PID" PIPE
      trap "kill -TERM $UBLHUB_PID" TERM
      if [ "x$UBLHUB_PIDFILE" != "x" ]; then
        echo $UBLHUB_PID > $UBLHUB_PIDFILE
      fi
      # Wait until the background process exits
      WAIT_STATUS=128
      while [ "$WAIT_STATUS" -ge 128 ]; do
         wait $UBLHUB_PID 2>/dev/null
         WAIT_STATUS=$?
         if [ "$WAIT_STATUS" -gt 128 ]; then
            SIGNAL=`expr $WAIT_STATUS - 128`
            SIGNAL_NAME=`kill -l $SIGNAL`
            echo "*** UblhubAS process ($UBLHUB_PID) received $SIGNAL_NAME signal ***" >&2
         fi
      done
      if [ "$WAIT_STATUS" -lt 127 ]; then
         UBLHUB_STATUS=$WAIT_STATUS
      else
         UBLHUB_STATUS=0
      fi
      if [ "$UBLHUB_STATUS" -ne 10 ]; then
            # Wait for a complete shudown
            wait $UBLHUB_PID 2>/dev/null
      fi
      if [ "x$UBLHUB_PIDFILE" != "x" ]; then
            grep "$UBLHUB_PID" $UBLHUB_PIDFILE && rm $UBLHUB_PIDFILE
      fi
   fi
   if [ "$UBLHUB_STATUS" -eq 10 ]; then
      echo "Restarting application server..."
   else
      exit $UBLHUB_STATUS
   fi
done
