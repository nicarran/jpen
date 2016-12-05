#!/bin/bash

# gets the major version (on std out) given a full version string as first parameter
#
# Expected parameters when calling this script:
# 1: the full version string formatted (x(.y(.z))-)w

if [ $# != 1 ]
then
	echo unexpected number of params
	exit 1
fi

version=$(echo $1 | awk -F '-' '{print($1)}' | awk -F '.' '{print($1)}')
if [ "$version" -eq "$version" ] 2>/dev/null # test if it's an integer
then
	echo $version
else # if the given full version string is not on the expected format then get the version from the last version tag 
	git describe --abbrev=0 | awk -F 'v' '{print($2)}' | awk -F '-' '{print($1)}' | awk -F '.' '{print($1)}'
fi
