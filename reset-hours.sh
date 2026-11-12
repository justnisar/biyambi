#!/bin/bash
# Restores hwork/Hours.java from the pristine skeleton (for timed rehearsals).
cp hskeleton/Hours.java hwork/Hours.java
sed -i '' 's/^package hskeleton;/package hwork;/' hwork/Hours.java 2>/dev/null \
  || sed -i 's/^package hskeleton;/package hwork;/' hwork/Hours.java
echo "reset: hwork/Hours.java restored to skeleton"
