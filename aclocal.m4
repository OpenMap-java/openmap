dnl AC_CHECK_JAVA_VERSION(PATH)
AC_DEFUN(AC_CHECK_JAVA_VERSION,
[AC_MSG_CHECKING([java version])
JAVA_VERSION=`$JAVA -version 2>&1 | head -1 | sed 's/^.*"\(.*\)".*/\1/'`
AC_MSG_RESULT($JAVA_VERSION)
JAVA2=`echo $JAVA_VERSION | sed -e 's#^\(1\.2.*\)#\1#'`
if test ! -z "${JAVA2}" ; then
  JAVA2_SRCS="\${JAVA2_SRCS}"
else
  JAVA2_SRCS=""
fi
AC_SUBST(JAVA2_SRCS)
])
