#!/usr/bin/env bash
set -e #exit on any command failure
./gradlew testDebugUnitTest
./gradlew jacocoTestReport