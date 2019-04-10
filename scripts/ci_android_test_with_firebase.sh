#!/usr/bin/env bash
# expected arguments:
# $1 = dir name $2 = module name $3 = flaky test retry number
set -e #exit on any command failure
./gradlew :$1:$2-sample:assembleDebug
./gradlew :$1:$2:assembleAndroidTest
openssl aes-256-cbc -K $encrypted_ea088b084227_key -iv $encrypted_ea088b084227_iv -in firebase-test-lab-key.json.enc -out firebase-test-lab-key.json -d
if [ ! -f ./google-cloud-sdk/bin/gcloud ]; then
  wget https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-241.0.0-linux-x86_64.tar.gz
  tar xf google-cloud-sdk-241.0.0-linux-x86_64.tar.gz
fi
./google-cloud-sdk/bin/gcloud config set project remotedevicetest-e89f4
./google-cloud-sdk/bin/gcloud auth activate-service-account --key-file firebase-test-lab-key.json
coverageDir=kin-sdk-android-${TRAVIS_JOB_NUMBER}
./google-cloud-sdk/bin/gcloud firebase test android run  --type instrumentation --app $1/$2-sample/build/outputs/apk/debug/$2-sample-debug.apk --test  $1/$2/build/outputs/apk/androidTest/debug/$2-debug-androidTest.apk --device model=Nexus5X,version=25,locale=en --no-record-video  --no-performance-metrics --num-flaky-test-attempts=$3 --environment-variables coverage=true,coverageFile="/sdcard/coverage.ec" --directories-to-pull /sdcard --results-dir ${coverageDir}
coverageFile=`./google-cloud-sdk/bin/gsutil ls "gs://test-lab-inaujqqz4p0pm-ha62k9dmaid32/${coverageDir}/**/*.ec" | tail -1`
echo ${coverageFile}
./google-cloud-sdk/bin/gsutil cp $coverageFile $1/$2/build
./gradlew :$1:$2:jacocoTestReport