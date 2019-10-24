#!/usr/bin/env bash
# download and extract google cloud sdk, if wasn't cache yet.
if [ ! -f ./google-cloud-sdk/bin/gcloud ]; then
    wget https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-241.0.0-linux-x86_64.tar.gz
    tar xf google-cloud-sdk-241.0.0-linux-x86_64.tar.gz
fi
echo $FIREBASE_KEY_FILE | ./google-cloud-sdk/bin/gcloud auth activate-service-account --key-file=-
./google-cloud-sdk/bin/gcloud --quiet config set project ${FIREBASE_PROJECT_NAME}
