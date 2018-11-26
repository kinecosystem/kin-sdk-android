![Kin Token](kin_android.png)
# Kin SDK for Android - Working in progress.
# for stable version please refer to kin-core-android: https://github.com/kinecosystem/kin-core-android
[![Build Status](https://travis-ci.org/kinecosystem/kin-sdk-android.svg?branch=dev)](https://travis-ci.org/kinecosystem/kin-sdk-android)
[![codecov](https://codecov.io/gh/kinecosystem/kin-sdk-android/branch/dev/graph/badge.svg)](https://codecov.io/gh/kinecosystem/kin-sdk-android)

Android library responsible for creating and managing KIN accounts.


# Android

## Add Kin SDK to your project

Add this to your module's `build.gradle` file.

```gradle
repositories {
    ...
    maven {
        url 'https://jitpack.io'
    }
}
...
dependencies {
    ...

    compile "com.github.kinecosystem:kin-sdk-android:<latest release>"
}
```

For latest release version go to [https://github.com/kinecosystem/kin-sdk-android/releases](https://github.com/kinecosystem/kin-sdk-android/releases).

The main repository is at [github.com/kinecosystem/kin-sdk-android](https://github.com/kinecosystem/kin-sdk-android).

## Get Started

### Connecting to a service provider

Create a new `KinClient`, with an `Environment` object that provides details of how to access the kin blockchain end point, Environment provides the predefined 
`Environment.TEST` and `Environment.PRODUCTION`.<br/>
`appId` is a 4 character string which represent the application id which will be added to each transaction.
appId must contain only upper and/or lower case letters and/or digits and that the total string length is exactly 4.<br/>
An optional parameter is `storeKey` which can be used to create a multiple accounts data set,
each different `storeKey` will have a separate data, an example use-case - store multiple users accounts separately.


The example below creates a `KinClient` that will be used to connect to the kin test network:

```java
kinClient = new KinClient(context, Environment.TEST, "1acd", "user1")
```

### Creating and retrieving a KIN account

The first time you use `KinClient` you need to create a new account, 
the details of the created account will be securely stored on the device.
Multiple accounts can be created using `addAccount`.

```java
KinAccount account;
try {
    if (!kinClient.hasAccount()) {
        account = kinClient.addAccount();
    }
} catch (CreateAccountException e) {
    e.printStackTrace();
}
```

Calling `getAccount` with the existing account index, will retrieve the account stored on the device.

```java
if (kinClient.hasAccount()) {
    account = kinClient.getAccount(0);
}
```

You can delete your account from the device using `deleteAccount`, 
but beware! you will lose all your existing KIN if you do this.

```java
kinClient.deleteAccount(int index);
```

## Onboarding

Before an account can be used on the configured network, it must be funded with the native network asset,
This step must be performed by a service, see [Fee token faucet service](fee-faucet.md).

For more details see [Onboarding](onboarding.md), also take a look at Sample App [OnBoarding](https://github.com/kinecosystem/kin-sdk-android/blob/master/sample/src/main/java/kin/sdk/sample/OnBoarding.java) class for a complete example.

## Account Information

### Public Address

Your account can be identified via it's public address. To retrieve the account public address use:

```java
account.getPublicAddress();
```

### Query Account Status

Current account status on the blockchain can be queried using `getStatus` method,
status will be one of the following 2 options:

* `AccountStatus.NOT_CREATED` - Account is not created (funded with native asset) on the network.
* `AccountStatus.CREATED` - Account was created, account can send and receive KIN.

```java
Request<Integer> statusRequest = account.getStatus();
statusRequest.run(new ResultCallback<Integer>() {
    @Override
    public void onResult(Integer result) {
        switch (result) {
            case AccountStatus.CREATED:
                //you're good to go!!!
                break;
            case AccountStatus.NOT_CREATED:
                //first create an account on the blockchain.
                break;
        }
    }

    @Override
    public void onError(Exception e) {

    }
});
```

### Retrieving Balance

To retrieve the balance of your account in KIN call the `getBalance` method: 

```java
Request<Balance> balanceRequest = account.getBalance();
balanceRequest.run(new ResultCallback<Balance>() {

    @Override
    public void onResult(Balance result) {
        Log.d("example", "The balance is: " + result.value(2));
    }

    @Override
        public void onError(Exception e) {
            e.printStackTrace();
        }
});
```

## Transactions

### Transferring KIN to another account

To transfer KIN to another account, you need the public address of the account you want to transfer the KIN to.
Also in case your app is not in the kin whitelist then you need to also use fee.
Amount of 1 fee equals to 1/100000 KIN.
If you are in the whitelist then look after the next example to see how you can send a whitelist transaction.

The following code will transfer 20 KIN to the recipient account "GDIRGGTBE3H4CUIHNIFZGUECGFQ5MBGIZTPWGUHPIEVOOHFHSCAGMEHO".

```java

String toAddress = "GDIRGGTBE3H4CUIHNIFZGUECGFQ5MBGIZTPWGUHPIEVOOHFHSCAGMEHO";
BigDecimal amountInKin = new BigDecimal("20");

// we could use here some custom fee or we can can call the blockchain in order to retrieve
// the current minimum fee by calling kinClient.getMinimumFee() or kinClient.getMinimumFeeSync().
// Then when you get the minimum fee returned and you can start the 'send transaction flow'
// with this fee.(see the sample app).
int fee = 100;

// Build the transaction and get a Request<Transaction> object.
buildTransactionRequest = account.buildTransaction(toAddress, amountInKin, fee);
// Actually run the build transaction code in a background thread and get 
// notify of success/failure methods (which runs on the main thread)
buildTransactionRequest.run(new ResultCallback<TransactionId>() {

    @Override
    public void onResult(Transaction transaction) {
        // Here we already got a Transaction object before actually sending the transaction. This means
        // that we can, for example, send the transaction id to our servers or save it locally  
        // in order to use it later. For example if we lose network just after sending 
        // the transaction then we will not know what happened with this transaction. 
        // So when the network is back we can check what is the status of this transaction.
        Log.d("example", "The transaction id before sending: " + transaction.getId().id());

        // Create the send transaction request
        sendTransactionRequest = account.sendTransaction(transaction);
        // Actually send the transaction in a background thread.
        sendTransactionRequest.run(new ResultCallback<TransactionId>() {

            @Override
            public void onResult(TransactionId id) {
                Log.d("example", "The transaction id: " + id);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }
});

```
### Transferring KIN to another account using whitelist service
The flow is very similar to the above code but here there is a middle stage in which you get the 'WhitelistableTransaction' object from the 'Transaction' object just after you build the transaction and you send it to the whitelist service.
Then you just use the method 'sendWhitelistTransaction(String whitelist)' and the parameter for that method is what you got from that service.

```java
String toAddress = "GDIRGGTBE3H4CUIHNIFZGUECGFQ5MBGIZTPWGUHPIEVOOHFHSCAGMEHO";
BigDecimal amountInKin = new BigDecimal("20");
// because it is white list then no fee is needed.
// even if the user will enter an amount bigger then zero it will not be deduced from his balance.
int fee = 0

buildTransactionRequest = account.buildTransaction(toAddress, amountInKin, fee);
buildTransactionRequest.run(new ResultCallback<TransactionId>() {

    @Override
    public void onResult(Transaction transaction) {
        Log.d("example", "The transaction id before sending: " + transaction.getId().id());

        // depends on your service but you could probably do it this way
        // or give it some listener or some other way.
        String whitelistTransaction = whitelistService.whitelistTransaction(transaction.getWhitelistableTransaction())

        // Create the send the white list transaction request
        sendTransactionRequest = account.sendWhitelistTransaction(whitelistTransaction);
        sendTransactionRequest.run(new ResultCallback<TransactionId>() {

            @Override
            public void onResult(TransactionId id) {
                Log.d("example", "The transaction id: " + id);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }
});

```


#### Memo

Arbitrary data can be added to a transfer operation using the memo parameter,
the memo can contain a utf-8 string up to 21 bytes in length. A typical usage is to include an order number that a service can use to verify payment.

```java
String memo = "arbitrary data";

buildTransactionRequest = account.buildTransaction(toAddress, amountInKin, fee, memo);
buildTransactionRequest.run(new ResultCallback<TransactionId>() {

    @Override
    public void onResult(Transaction transaction) {
        Log.d("example", "The transaction id before sending: " + transaction.getId.().id());

        sendTransactionRequest = account.sendTransaction(transaction);
        sendTransactionRequest.run(new ResultCallback<TransactionId>() {

            @Override
            public void onResult(TransactionId id) {
                Log.d("example", "The transaction id: " + id);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }
    
});
```

## Account Listeners

### Listening to payments

Ongoing payments in KIN, from or to an account, can be observed,
by adding payment listener using `BlockchainEvents`:

```java
ListenerRegistration listenerRegistration = account
            .addPaymentListener(new EventListener<PaymentInfo>() {
                @Override
                public void onEvent(PaymentInfo payment) {
                    Log.d("example", String
                        .format("payment event, to = %s, from = %s, amount = %s", payment.sourcePublicKey(),
                            payment.destinationPublicKey(), payment.amount().toPlainString());
                }
            });
```

### Listening to balance changes

Account balance changes, can be observed by adding balance listener using `BlockchainEvents`:

```java
ListenerRegistration listenerRegistration = account.addBalanceListener(new EventListener<Balance>() {
            @Override
            public void onEvent(Balance balance) {
                Log.d("example", "balance event, new balance is = " + balance.value().toPlainString());
            }
        });
```

### Listening to account creation

Account creation on the blockchain network, can be observed, by adding create account listener using `BlockchainEvents`:

```java
ListenerRegistration listenerRegistration = account.addAccountCreationListener(new EventListener<Void>() {
            @Override
            public void onEvent(Void result) {
                Log.d("example", "Account has created.");
            }
        });
```

For unregister any listener use `listenerRegistration.remove()` method.

## Sync vs Async

Asynchronous requests are supported by our `Request` object. The `request.run()` method will perform the requests sequentially on a single background thread and notify success/failure using `ResultCallback` on the android main thread.
In addition, `cancel(boolean)` method can be used to safely cancel requests and detach callbacks.

A synchronous version (with the 'Sync' suffix) of these methods is also provided, as SDK requests performs network IO operations, make sure you call them in a background thread.

```java
try {
    Balance balance = account.getBalanceSync();
} catch (OperationFailedException e) {
   // something went wrong - check the exception message
}

try {
    // build the transaction
    Transaction transaction = account.buildTransactionSync(toAddress, amountInKin, fee)
    // send the transaction
    TransactionId transactionId = account.sendTransactionSync(transaction);
} catch (OperationFailedException e){
    // something else went wrong - check the exception message
}
```

## Error Handling

`kin-sdk` wraps errors with exceptions, synchronous methods can throw exceptions and asynchronous requests has `onError(Exception e)` callback.

### Common Errors

`AccountNotFoundException` - Account is not created (funded with native asset) on the network.  
`InsufficientKinException` - Account has not enough kin funds to perform the transaction.  
`InsufficientFeeException` - Transaction has not enough fee to perform the transaction.  
for all the exception you can look [here](https://github.com/kinecosystem/kin-sdk-android/tree/master/kin-sdk/src/main/java/kin/sdk/exception/).

## Sample Application

![Sample App](../.github/android_sample_app_screenshot.png)

Sample app covers the entire functionality of `kin-sdk`, and serves as a detailed example on how to use the library.
Sample app source code can be found [here](https://github.com/kinecosystem/kin-sdk-android/tree/dev/sample/).

## Building from Source

Clone the repo:

```bash
$ git clone https://github.com/kinecosystem/kin-sdk-android.git
```
Now you can build the library using gradle, or open the project using Android Studio.

### Tests

Both Unit tests and instrumentation tests are provided, Android tests include integration tests that run on a remote test network, these tests are marked as `@LargeTest`, because they are time consuming, and depends on the network.

### Running Tests

For running both unit tests and instrumentation tests and generating a code coverage report using Jacoco, use this script
```bash
$ ./run_integ_test.sh
```

Running tests without integration tests

```bash
$ ./gradlew jacocoTestReport  -Pandroid.testInstrumentationRunnerArguments.notClass=kin.sdk.KinAccountIntegrationTest
```

Generated report can be found at:  
`kin-sdk/build/reports/jacoco/jacocoTestReport/html/index.html`.


## Contributing
Please review our [CONTRIBUTING.md](CONTRIBUTING.md) guide before opening issues and pull requests.

## License
The kin-sdk-android library is licensed under [MIT license](LICENSE.md).
