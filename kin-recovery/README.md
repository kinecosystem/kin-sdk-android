# Backup and Restore Standalone Module

This module is an optional way to back up and/or restore your account.  
The module wraps the Kin SDK Android import and export functionalities with a UI that includes two flows - backup and restore.  
The UI uses a password to create a QR code, which is then used to back up the account and to restore it.

It is implemented as an Android library that can be incorporated into your code.  
This library is dependent on the kin-sdk library, and we assume that whoever needs to use it is already familiar with the kin-sdk library.
For more details on Kin SDK, go to [kin-sdk](https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-sdk).

## Installation

The Kin Recovery module for Android is implemented as an Android library.  
To include the library in your project, add these two statements to your `build.gradle` files.

###### Snippet: Modifying project build file

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
###### Snippet: Modifying module build files

```gradle
...
dependencies {
    ...
    implementation 'com.github.kinecosystem.kin-sdk-android:recovery:<latest release>'
}
```

For the latest release version, go to [https://github.com/kinecosystem/kin-sdk-android/releases](https://github.com/kinecosystem/kin-sdk-android/releases).

See the main repository at [github.com/kinecosystem/kin-sdk-android](https://github.com/kinecosystem/kin-sdk-android).


## Overview

Launching Backup and Restore flows requires the following steps:

1. Creating the backup and restore manager
2. Adding callbacks
3. Passing the result
4. Backup or Restore

### Step 1 - Creating the Backup and Restore Manager

First you will need to have a KinClient object for later use when you will invoke backup and/or restore methods.  
Also you need to create the BackupAndRestoreManager object.
###### Snippet: This is an example of how to create those objects:

```java
kinClient = new KinClient(context, Environment.TEST, "1acd")
...
backupAndRestoreManager = new BackupAndRestoreManager(context);
```


For more details on KinClient, see https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-sdk.

### Step 2 - Adding Callbacks

Both callbacks have 3 methods:
 - `onSuccess` is called when the operation in finished successfully and in restore it also have a `KinAccount` object which is the restored account.
- `onCancel` is called when the user is going back from the backup and restore activty to your activity.
- `onError()` is called if there was some error in the process of backup or restore.

```java
// For backup
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

// For restore  
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
##### NOTE:
Be sure to unregister from the module when no longer needed.  
When you want to unregister then please use release:
###### Snippet: unregister from the module and release all its resources
```java 
backupAndRestoreManager.release();
``` 
We recommend to register/unregister the callbacks in a way that will “survive” activity restart or similar situations.  
For example, you can register in Activity.onCreate and release in Activity.onDestroy methods.  
in MVP, for example, you can do it in the presenter Creation and Destruction.

#### Step 3 - Passing the Result

Because the module internally using `startActivityForResult` then in order for the module to work properly you need to implement `onActivityResult` in your activity and in that method you need to call
`backupAndRestore.onActivityResult(...);`
for example:
```java 
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    backupAndRestore.onActivityResult(requestCode, resultCode, data);
}
```

#### Step 4 - Backup or Restore

Finally you can now use backup or restore flow by simply calling:

```java 
// For backup
backupAndRestoreManager.backup(kinClient, kinAccount.getPublicAddress());

// For restore
backupAndRestoreManager.restore(kinClient)
```

### Error Handling

onError(BackupAndRestoreException e) can be called in the case of an error that has occured while in the process of trying to backup or restore.

### Testing

Both unit tests and instrumented tests are provided.
For a full list of tests see

- https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-recovery/recovery/src/test
- https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-recovery/recovery/src/androidTest


### Running Tests

For running both unit tests and instrumented tests and generating a code coverage report using Jacoco, use this script
```bash
$ ./run_integ_test.sh
```

### Building from Source

To build from source clone the repo:

```bash
$ git clone https://github.com/kinecosystem/kin-sdk-android.git
```
Now you can build the library using gradle, or open the project using Android Studio.

## Sample Code

`recovery-sample` app covers the entire functionality of `kin-recovery`, and serves as a detailed example on how to use the library.

Sample app source code can be found [here](https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-recovery/recovery-sample/).
