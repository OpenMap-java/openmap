
AC_DEFUN(AC_PROG_JAVA,
[if test "x$with_java" = "xno"; then
  # user specified no java...
  AC_MSG_ERROR([cannot configure without java])
elif test "x$with_java" = "xyes" || test "x$with_java" = "x" ; then
  # user specified to use java, but didn't help to locate it
  #   OR
  # user didn't identify a potential path for 'java'
  AC_PATH_PROG(JAVA, java)
  if test -z "$JAVA"; then
    AC_MSG_ERROR([no acceptable java found in \$PATH])
  fi
else
  # with_java is not no, or yes, or empty.  Assume it is a path.
  AC_MSG_CHECKING([for java])
  JAVA=$with_java
  if test -f $JAVA && test -x $JAVA; then
    AC_SUBST(JAVA)
    AC_MSG_RESULT($JAVA)
  else
    AC_MSG_ERROR([$JAVA does not exist or is not an executable file.])
  fi
fi
# FIXME Check version number to be sure it is acceptable.
AC_CHECK_JAVA_VERSION
])


AC_DEFUN(AC_CHECK_JAVA_VERSION,
[AC_MSG_CHECKING([java version])
JAVA_VERSION=`$JAVA -version 2>&1 | head -1 | sed 's/^.*"\(.*\)".*/\1/'`
# convert 1.2beta4 to 1.2.beta4
ac_java_canon_version=`echo $JAVA_VERSION | sed 's/beta/\.beta/'`
JAVA_VERSION_MAJOR=`echo $ac_java_canon_version | sed 's/\(.\)\..\..*/\1/'`
JAVA_VERSION_MINOR=`echo $ac_java_canon_version | sed 's/.\.\(.\)\..*/\1/'`
JAVA_VERSION_TEENY=`echo $ac_java_canon_version | sed 's/.\..\.\(.*\)/\1/'`
AC_SUBST(JAVA_VERSION)
AC_SUBST(JAVA_VERSION_MAJOR)
AC_SUBST(JAVA_VERSION_MINOR)
AC_SUBST(JAVA_VERSION_TEENY)
AC_MSG_RESULT($JAVA_VERSION)
dnl AC_MSG_RESULT(major: $ac_java_major)
dnl AC_MSG_RESULT(minor: $ac_java_minor)
dnl AC_MSG_RESULT(teeny: $ac_java_teeny)
])

AC_DEFUN(AC_PROG_JAVAC,
[if test "x$with_javac" = "xno"; then
  # user specified no javac...
  AC_MSG_ERROR([cannot configure without javac])
elif test "x$with_javac" = "xyes" || test "x$with_javac" = "x" ; then
  # user specified to use javac, but didn't help to locate it
  #   OR
  # user didn't identify a potential path for 'javac'
  AC_PATH_PROG(JAVAC, javac)
  if test -z "$JAVAC"; then
    AC_MSG_ERROR([no acceptable javac found in \$PATH])
  fi
else
  # with_java is not no, or yes, or empty.  Assume it is a path.
  AC_MSG_CHECKING([for javac])
  JAVAC=$with_javac
  if test -f $JAVAC && test -x $JAVAC; then
    AC_SUBST(JAVAC)
    AC_MSG_RESULT($JAVAC)
  else
    AC_MSG_ERROR([$JAVAC does not exist or is not an executable file.])
  fi
fi
# FIXME Check that it actually works by compiling and running a test program
AC_TEST_JAVAC
])


AC_DEFUN(AC_TEST_JAVAC,
[AC_MSG_CHECKING([the java compiler])
changequote(, )dnl
	cat >test.java <<EOF
	public class test { public static void main(String[] args) { 
		System.out.println("present");
	} }
EOF
changequote([, ])dnl
	$JAVAC test.java
	case `$JAVA test` in
		*present*) AC_MSG_RESULT([it seems to work]) ;;
		*)	AC_MSG_ERROR([$JAVAC failed to compile a simple test program]) ;;
	esac
	rm -f test.java test.class
])

dnl AC_PATH_JAR(VARIABLE, JAR-TO-CHECK-FOR, OVERRIDE-VALUE, PATH)
AC_DEFUN(AC_PATH_JAR,
[# check the files in $N(CLASSPATH) for the named jar file.
# then check the directories in $N(CLASSPATH) for the named jar file.
# then check some likely candidates...
set dummy $2; ac_word=[$]2
AC_MSG_CHECKING([for $ac_word])
AC_CACHE_VAL(ac_cv_path_$1,
dnl If no 3rd arg is given, leave the cache variable unset,
dnl so AC_PATH_PROGS will keep looking.
ifelse([$3], , [ ac_override=""], [ ac_override=[$]$3
])dnl

if test -n "$ac_override"; then
  echo -n  "overriding... "
  ac_cv_path_$1=$ac_override
else
  [IFS="${IFS= 	}"; ac_save_ifs="$IFS"; IFS="${IFS}:"
  dnl $ac_dummy forces splitting on constant user-supplied paths.
  dnl bash word splitting is done only on the output of word expansions,
  dnl not every word.  This closes a longstanding sh security hole.
  for entry in ifelse([$4], , $CLASSPATH, [$4$ac_dummy]); do
    if test -f $entry && test "`basename $entry`" = "$ac_word"; then
      ac_cv_path_$1=$entry
      break
    fi
  done
  IFS="$ac_save_ifs"
  ])dnl close AC_CACHE_VAL
fi
$1="$ac_cv_path_$1"
if test -n "[$]$1"; then
  AC_MSG_RESULT([$]$1)
else
  AC_MSG_RESULT(no)
fi
AC_SUBST($1)dnl
])

dnl AC_FIND_SWING_JAR(PATH)
AC_DEFUN(AC_FIND_SWING_JAR,
[# locate swing in the classpath
AC_MSG_CHECKING([for swing])
ac_swing_path=ifelse([$1], , $CLASSPATH, [$1])
ac_swing_jar=""
case $with_swing in
  no)
    AC_MSG_ERROR([cannot configure without swing])
    ;;
  /*)
    ac_swing_jar=$with_swing
    ;;
  "" | yes)
    AC_FIND_REGEX_IN_PATH(ac_swing_jar, "swing.*\.jar", $ac_swing_path)
    ;;
  *)
    AC_FIND_REGEX_IN_PATH(ac_swing_jar, $with_swing, $ac_swing_path)
    ;;
esac
if test -n "$ac_swing_jar"; then
  SWING_JAR=$ac_swing_jar
  AC_SUBST(SWING_JAR)
  AC_MSG_RESULT([using $ac_swing_jar])
else
  AC_MSG_ERROR([couldn't locate swing in CLASSPATH])
fi
])

dnl AC_FIND_ENTRY_IN_PATH_REGEX(VARIABLE, REGEX, PATH)
AC_DEFUN(AC_FIND_REGEX_IN_PATH,
[# comment
  IFS="${IFS= 	}"; ac_save_ifs="$IFS"; IFS="${IFS}:"
dnl $ac_dummy forces splitting on constant user-supplied paths.
dnl bash word splitting is done only on the output of word expansions,
dnl not every word.  This closes a longstanding sh security hole.
  for entry in $3; do
    if basename $entry | grep -q $2; then
      $1=$entry
      break
    fi
  done
  IFS="$ac_save_ifs"
])
