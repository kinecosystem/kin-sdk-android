# Standalone Backup and Restore Module

This module is an optional way to back up and/or restore your account.
The module's UI includes two flows, backup and restore. The UI wraps the Kin SDK Android import and export functionalities, on which these flows are based.
The UI uses a password to create a QR code, which is then used to back up the account and to restore it.

It is implemented as an Android library that can be incorporated into your code.
This library is dependent on the kin-sdk library, and we assume that whoever needs to use it is already familiar with the kin-sdk library.  
For more details on the Kin SDK, go to [kin-sdk on github](https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-sdk)
and/or to our docs on the website - [kin-sdk docs](https://kinecosystem.github.io/kin-website-docs/docs/documentation/android-sdk).

## Installation

To include the Kin Backup and Restore module library in your project, add these two statements to your `build.gradle` files.

###### Modifying Project Build File

```gradle
allprojects {
    repositories {
...
        maven {
            url 'https://jitpack.io'
        }
    }
}
```
###### Modifying Module Build Files
This module is available only from release 1.0.4 of the Kin SDK.

```gradle
...
dependencies {
    ...
    implementation 'com.github.kinecosystem.kin-sdk-android:kin-backup-and-restore-lib:<latest release>'
}
```

For the latest release version, go to [https://github.com/kinecosystem/kin-sdk-android/releases](https://github.com/kinecosystem/kin-sdk-android/releases).

See the main repository at [github.com/kinecosystem/kin-sdk-android](https://github.com/kinecosystem/kin-sdk-android).


## Overview

Launching the Backup and Restore flows requires the following steps:

1. Creating the Backup and Restore manager
2. Adding callbacks
3. Passing the result to the Backup and Restore module
4. Performing a Backup and/or Restore action

### Step 1 - Creating the Backup and Restore Manager

You need to create a BackupAndRestoreManager object.  
Creating this object requires the following parameters:
- Activity (the activity from which this object is being created) and 
- Two request codes for later use in 'onActivityResult'.
###### Example of how to create this object:
This is only an example; you can use different values and names.
```java
...
private static final int REQ_CODE_BACKUP = 9000;
private static final int REQ_CODE_RESTORE = 9001;
...

backupAndRestoreManager = new BackupAndRestoreManager(activity, REQ_CODE_BACKUP, REQ_CODE_RESTORE);
```

### Step 2 - Adding Backup and Restore Callbacks

Both callbacks have the same 3 methods:
 - `onSuccess` is called when the operation is completed successfully. In the Restore callback, it has a `KinClient`(the updated one) and `KinAccount` object, which is the restored account.  
- `onCancel` is called when the user leaves the backup or restore activity and returns to the previous activity.  
- `onFailure()` is called if there is an error in the backup or restore process.

###### Creating Backup Callbacks

```java
backupAndRestoreManager.registerBackupCallback(new BackupCallback() {

    @Override
    public void onSuccess() {
        // Here you can handle the success.
    }
    
    @Override
    public void onCancel() {
        // Here you can handle the cancellation.
    }
    
    @Override
    public void onFailure(BackupAndRestoreException throwable) {
        // Here you can handle the failure.
    }
});
```

###### Creating Restore Callbacks
```java  
backupAndRestoreManager.registerRestoreCallback(new RestoreCallback() {

    @Override
    public void onSuccess(KinClient kinClient, KinAccount kinAccount) {
        // Here you can handle the success. 
    }
    
    @Override
    public void onCancel() {
        // Here you can handle the cancellation.
    }
    
    @Override
    public void onFailure(BackupAndRestoreException throwable) {
        // Here you can handle the failure.
    }
});
```

NOTE:
Be sure to unregister from the module when it is no longer needed.
To unregister from the module and release all its resources, use this code:


```java 
backupAndRestoreManager.release();
``` 
You should register/unregister the callbacks in a way that will “survive” an activity restart or similar situations.
To achieve that, you should register in `Activity.onCreate` and release in `Activity.onDestroy`. 

#### Step 3 - Passing the Result to the Backup and Restore Module

Since the module internally uses `startActivityForResult`, for it to work properly, you have to implement `onActivityResult` in your activity. In that method, you need to call
`backupAndRestoreManager.onActivityResult(...);`

For example:
```java 
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQ_CODE_BACKUP || requestCode == REQ_CODE_RESTORE) {
        backupAndRestore.onActivityResult(requestCode, resultCode, data);
    }
}
```

#### Step 4 - Backing Up or Restoring an Account
Before you start using the Backup and Restore flows, you need to create a kinClient object.
If you want to back up, you also need the KinAccount object, which represents the account that you want to back up.
###### Example of How to Create a kinClient Object:

```java
kinClient = new KinClient(context, Environment.TEST, "1acd")
...
kinAccount = kinClient.getAccount(0);
...
// And just like step 1:
backupAndRestoreManager = new BackupAndRestoreManager(activity, REQ_CODE_BACKUP, REQ_CODE_RESTORE);
```
For more details on KinClient and KinAccount, see our repo in github for [KinAccount](https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-sdk#creating-and-retrieving-a-kin-account) and [KinClient](https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-sdk#accessing-the-kin-blockchain)
or see our website docs for [KinClient](https://kinecosystem.github.io/kin-website-docs/docs/documentation/android-sdk#accessing-the-kin-blockchain)
and [KinAccount](https://kinecosystem.github.io/kin-website-docs/docs/documentation/android-sdk#creating-and-retrieving-a-kin-account)

Now you can use the Backup and Restore flows by calling these functions:

- For backup:
```java 

backupAndRestoreManager.backup(kinClient, kinAccount);

```
- For restore:
```java 

backupAndRestoreManager.restore(kinClient)
```
### Error Handling

`onFailure(BackupAndRestoreException e)` can be called if an error has occured while you attempt to back up or restore an account.

### Testing

Both unit tests and instrumented tests are provided.
For a full list of tests, see

- https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-backup-and-restore/kin-backup-and-restore-lib/src/test
- https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-backup-and-restore/kin-backup-and-restore-lib/src/androidTest


#### Running Tests

For running both unit tests and instrumented tests and generating a code coverage report using Jacoco, use those scripts one after another:
```bash
$ ./gradlew :kin-backup-and-restore:kin-backup-and-restore-lib:connectedDebugAndroidTest
$ ./gradlew :kin-backup-and-restore:kin-backup-and-restore-lib:jacocoTestReport
```

A report is generated and can be found at:
kin-backup-and-restore/kin-backup-and-restore-lib/build/reports/jacoco/jacocoTestReport/html/index.html.

### Building from Source

To build from source, clone the repo:

```bash
$ git clone https://github.com/kinecosystem/kin-sdk-android.git
```
Now you can build the library using gradle or open the project using Android Studio.

## Sample App Code

The `kin-backup-and-restore-sample` app covers the entire functionality of `kin-backup-and-restore` and serves as a detailed example of how to use the library.

The sample app source code can be found [here](https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-backup-and-restore/kin-backup-and-restore-sample/).
