#!/usr/bin/env bash
set -e #exit on any command failure
./gradlew testDebugUnitTest
./gradlew :kin-backup-and-restore:kin-backup-and-restore-lib:jacocoTestReport