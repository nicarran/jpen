#! bash

mkdir -p dist/temp
revision=`svn info . | grep -e 'Revision:' | sed -e 's/[^0-9]//g'`

echo -n $revision > dist/temp/revision
echo -n jpen-2_$revision > dist/temp/dist_name
