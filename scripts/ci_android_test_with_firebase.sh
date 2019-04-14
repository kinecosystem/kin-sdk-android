#!/usr/bin/env bash
# expected arguments:
# $1 = dir name $2 = module name $3 = flaky test retry number
set -e #exit on any command failure
# Build lib sampel app apk, firebase requires app apk, even when just android tests are running, can be replaced with empty APK in the future
./gradlew :$1:$2-sample:assembleDebug
# Build android tests apk
./gradlew :$1:$2:assembleAndroidTest
# decrypt firebase credentials
openssl aes-256-cbc -K $encrypted_ea088b084227_key -iv $encrypted_ea088b084227_iv -in firebase-test-lab-key.json.enc -out firebase-test-lab-key.json -d
# download and extract google cloud sdk, if wasn't cache yet.
if [ ! -f ./google-cloud-sdk/bin/gcloud ]; then
  wget https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-241.0.0-linux-x86_64.tar.gz
  tar xf google-cloud-sdk-241.0.0-linux-x86_64.tar.gz
fi
./google-cloud-sdk/bin/gcloud config set project ${FIREBASE_PROJECT_NAME}
./google-cloud-sdk/bin/gcloud auth activate-service-account --key-file firebase-test-lab-key.json
# use travis job number as an unique value, for the folder that firebase will save coverage files
coverageDir=kin-sdk-android-${TRAVIS_JOB_NUMBER}
# run tests with firebase, generate coverage file (.ec) and upload it to "coverageDir" in google cloud storage
./google-cloud-sdk/bin/gcloud firebase test android run  --type instrumentation --app $1/$2-sample/build/outputs/apk/debug/$2-sample-debug.apk --test  $1/$2/build/outputs/apk/androidTest/debug/$2-debug-androidTest.apk --device model=Nexus5X,version=25,locale=en --no-record-video  --no-performance-metrics --num-flaky-test-attempts=$3 --environment-variables coverage=true,coverageFile="/sdcard/coverage.ec" --directories-to-pull /sdcard --results-dir ${coverageDir}
# extract the .ec file name from google cloud
coverageFile=`./google-cloud-sdk/bin/gsutil ls "gs://${FIREBASE_GOOGLE_CLOUD_FOLDER}/${coverageDir}/**/*.ec" | tail -1`
echo ${coverageFile}
# copy it locally to our lib build folder
./google-cloud-sdk/bin/gsutil cp $coverageFile $1/$2/build
# jacocoTestReport task will convert .ec file to jacoco format that codecov recognize, codecove will collect those files at the "after script" stage
./gradlew :$1:$2:jacocoTestReport