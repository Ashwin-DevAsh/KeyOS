#!/bin/bash

if [ $DRONE_BRANCH = "beta" ] || [ $DRONE_BRANCH = "stable" ]; then
    BUILD_COMMAND="assembleQuickstepKioskPlahRelease"
else
    BUILD_COMMAND="assembleQuickstepKioskCiOptimized"
fi

echo "Running $BUILD_COMMAND"
bash ./gradlew $BUILD_COMMAND
