# **********************************************************************
# 
# <copyright>
# 
#  BBN Technologies, a Verizon Company
#  10 Moulton Street
#  Cambridge, MA 02138
#  (617) 873-8000
# 
#  Copyright (C) BBNT Solutions LLC. All rights reserved.
# 
# </copyright>
# **********************************************************************
# 
# $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/plotLayer/thin.pl,v $
# $RCSfile: thin.pl,v $
# $Revision: 1.1.1.1 $
# $Date: 2003/02/14 21:35:48 $
# $Author: dietrick $
# 
# **********************************************************************
#!/usr/local/bin/perl -w
##
######################################################################
##
##  FILE: thin.pl
##  AUTH: George Keith <gkeith@bbn.com>
##
##  DESC: Thin out a large data file
##
##  USAGE: 
##
##  Created on: Wed Oct 14 11:03:15 1998
##  $Id: thin.pl,v 1.1.1.1 2003/02/14 21:35:48 dietrick Exp $
##
##  $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/plotLayer/thin.pl,v $
##  $RCSfile: thin.pl,v $
##  $Author: dietrick $
##  $Date: 2003/02/14 21:35:48 $
##
##  Modification history:
##  $Log: thin.pl,v $
##  Revision 1.1.1.1  2003/02/14 21:35:48  dietrick
##  Moving source code to new src directory, with directories to separate code that relies on non-J2SE packages
##
##  Revision 1.2  2002/04/02 22:25:12  bmackiew
##  Updated with revised copyright information.
##
##  Revision 1.1  1998/10/15 19:36:37  gkeith
##  initial checkin of the plotlayer
##
##
######################################################################


open (INFILE, "<AT.gst.txt") or die "can't open inputfile\n";
open (OUTFILE, ">AT.gst_thin.txt") or die "can't open inputfile\n";

my $linecount = 0;

while (<INFILE>){
  @dataline = split / +/;
#  print (join (":",@dataline));
#   print "$dataline[6], $dataline[7]\n";
  if (($dataline[6] > -30)
      && ($dataline[7] < 0)){
    $linecount++;
    print OUTFILE;
  }
  if ($linecount > 0 &&
      ($linecount / 1000) == 0){
    print "$linecount lines\n";
  }
}

print "Yielded $linecount lines\n";
close(INFILE);
close(OUTFILE);

