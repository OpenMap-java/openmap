#!/usr/bin/perl

# recursively finds all files below current working directory that are
# named GNUmakefile and renames them to gnuMakefile.


if (@ARGV < 2) {
    print_usage() && die "\n";
}

$oldname = $ARGV[0];
$newname = $ARGV[1];

print "Renaming all files named $oldname to $newname\n";

find_and_rename(".", "$oldname", "$newname");

sub print_usage {
    print "usage: rename.pl old-name new-name\n";
}

sub find_and_rename {
    local($dir, $target, $newname) = @_;
    local @subdirs;
    opendir(DIR,$dir) || die "unable to open directory $dir";
    foreach $name (sort readdir(DIR)) {
	if ((-d $dir . "/" . $name) && ($name ne "..") && ($name ne ".") && ($name ne "CVS")) {
	    push(@subdirs, $name);
	}
#	print "comparing $name to $target\n";
	if ($name eq $target) {
	    $oldname = $dir . "/" . $name;
	    $mvname = $dir . "/" . $newname;
	    print "moving $oldname to $mvname\n";
	    rename($oldname, $mvname) || die ("Can't rename $oldname to $mvname");
	}
    }
    closedir(DIR);
    while ($subdir = pop(@subdirs)) {
	find_and_rename($dir . "/" . $subdir, $target, $newname);
    }
}
