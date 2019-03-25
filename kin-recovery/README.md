# Backup and Restore Standalone Module

This module is an optional way to back up and/or restore your account.
The module wraps the Kin SDK Android import and export functionalities with a UI that includes two flows - backup and restore.
The UI uses a password to create a QR code, which is then used to back up the account and to restore it.

It is implemented as an Android library that can be incorporated into your code.
This library is dependent on the kin-sdk library, and we assume that whoever needs to use it is already familiar with the kin-sdk library.
For more details on Kin SDK, go to [kin-sdk](https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-sdk).

## Installation

The Kin backup-and-restore module for Android is implemented as an Android library.
To include the library in your project, add these two statements to your `build.gradle` files.

###### Modifying project build file

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
###### Modifying module build files

```gradle
...
dependencies {
    ...
    implementation 'com.github.kinecosystem.kin-sdk-android:backup-and-restore:<latest release>'
}
```

For the latest release version, go to [https://github.com/kinecosystem/kin-sdk-android/releases](https://github.com/kinecosystem/kin-sdk-android/releases).

See the main repository at [github.com/kinecosystem/kin-sdk-android](https://github.com/kinecosystem/kin-sdk-android).


## Overview

Launching the Backup and Restore flows requires the following steps:

1. Creating the Backup and Restore manager
2. Adding callbacks
3. Passing the result to the Backup and Restore module
4. Backup or Restore

### Step 1 - Creating the Backup and Restore Manager

You need to create a BackupAndRestoreManager object.
###### Example of how to create this object:

```java
backupAndRestoreManager = new BackupAndRestoreManager(context);
```

### Step 2 - Adding Backup and Restore callbacks

Both callbacks have the same 3 methods:
 - `onSuccess` is called when the operation is completed successfully. In the Restore callback, it has a `KinAccount` object, which is the restored account.
- `onCancel` is called when the user leaves the backup or restore activity and returns to the previous activity .
- `onError()` is called if there is an error in the backup or restore process.
###### Creating Backup callbacks
```java
backupAndRestoreManager.registerBackupCallback(new BackupCallback() {
    @Override
    public void onSuccess() {
        // here you can handle the success.
    }
    
    @Override
    public void onCancel() {
        // here you can handle the cancellation.
    }
    
    @Override
    public void onFailure(BackupException throwable) {
        // here you can handle the failure.
    }
});
}
});
```
###### Creating Restore callbacks
```java  
backupAndRestoreManager.registerRestoreCallback(new RestoreCallback() {
    @Override
    public void onSuccess(KinAccount kinAccount) {
        // here you can handle the success.
    }
    
    @Override
    public void onCancel() {
        // here you can handle the cancellation.
    }
    
    @Override
    public void onFailure(BackupException throwable) {
        // here you can handle the failure.
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
In order to achieve that you should register in `Activity.onCreate` and release in `Activity.onDestroy`.

#### Step 3 - Passing the Result to the Backup and Restore module

Since the module internally uses `startActivityForResult`, for it to work properly, you have to implement `onActivityResult` in your activity. In that method, you need to call
`backupAndRestore.onActivityResult(...);`

For example:
```java 
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    backupAndRestore.onActivityResult(requestCode, resultCode, data);
}
```

#### Step 4 - Backup or Restore
Before you start using the Backup and Restore flows, you need to create a kinClient object.
If you want to back up, you need the KinAccount object, which represents the account that you want to back up.
###### Example of how to create a kinClient object:

```java
kinClient = new KinClient(context, Environment.TEST, "1acd")
...
backupAndRestoreManager = new BackupAndRestoreManager(context);
```
For more details on KinClient and KinAccount, see [KinClient](https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-sdk#Accessing-the-Kin-blockchain)
and [KinAccount](https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-sdk#Creating-and-retrieving-a-Kin-account)

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

`onError(BackupAndRestoreException e)` can be called if an error has occured while trying to back up or restore.

### Testing

Both unit tests and instrumented tests are provided.
For a full list of tests, see

- https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-backup-and-restore/backup-and-restore/src/test
- https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-backup-and-restore/backup-and-restore/src/androidTest


#### Running Tests

For running both unit tests and instrumented tests and generating a code coverage report using Jacoco, use this script:
```bash
$ ./gradlew :kin-recovery:recovery jacocoTestReport
```

Generated report can be found at:
kin-recovery/recovery/build/reports/jacoco/jacocoTestReport/html/index.html.

### Building from Source

To build from source, clone the repo:

```bash
$ git clone https://github.com/kinecosystem/kin-sdk-android.git
```
Now you can build the library using gradle or open the project using Android Studio.

## Sample App Code

The `backup-and-restore-sample` app covers the entire functionality of `kin-backup-and-restore` and serves as a detailed example of how to use the library.

The sample app source code can be found [here](https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-backup-and-restore/backup-and-restore-sample/).
