#!/usr/bin/env bash

set -e

if [[ `git status --porcelain` ]]; then
    echo "There are local changes, aborting."
    exit 1
fi

if [[ `git symbolic-ref --short -q HEAD` != "master" ]]; then
    echo "Must be on master branch, aborting."
    exit 1
fi

echo "Current version: `cat version`"
read -p "Next version: " next_version

echo "$next_version" > version

./gradlew clean build

read -p "Do you want to release version '$next_version'? " yn
case $yn in
    [Yy]* ) ;;
    * ) exit 1;;
esac

git add version && git commit -m "Release $next_version"
git push origin master

json=$(printf '{"tag_name":"%s","name":"%s","body":"%s"}' "$next_version" "$next_version" "Release $next_version.")

username=`git remote get-url origin | cut -d '/' -f 1 | cut -d ':' -f 2`
token=`cat ~/$username.token`

curl --user $username:$token --request POST --data $json https://api.github.com/repos/$username/log4j2-json-layout/releases

./gradlew uploadArchives closeAndReleaseRepository -Prelease
