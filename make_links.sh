#!/bin/bash
dir1="$(pwd)/"
dir2="/home/mario/OB/openbravo-erp/modules/"
cd "$dir1"
for file_name in $(ls | grep  "com.*") ; do
echo $file_name
ln -s "$dir1$file_name" "$dir2"/$file_name
done
