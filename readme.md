# Firebase Playground

This app is a playground for working with Firebase

It includes the following tasks:

**Authentication**: if a client is accessing the Firebase service it needs to be "registered" user 
and the following services are running here:

- sign in with email and password
- sign in with email and password (email verification)
- sign in using an Google account
- sign in anonymously
- sign out the existing user


**Ressources used for this playground**

Most ressources are from Google's Firebase developer site

**General**: 

start with https://firebase.google.com/docs/android/setup


**Authentication**: 

Sign in with email and password: https://firebase.google.com/docs/auth/android/start

Sign in with an Google account: https://firebase.google.com/docs/auth/android/google-signin

**Database**:

01 Firebase Realtime Database: https://firebase.google.com/docs/database

02 Choose a Database: Cloud Firestore or Realtime Database: https://firebase.google.com/docs/database/rtdb-vs-firestore

03 Connect your App to Firebase: https://firebase.google.com/docs/database/android/start

04 Structure Your Database: https://firebase.google.com/docs/database/android/structure-data

05 Read and Write Data on Android: https://firebase.google.com/docs/database/android/read-and-write

06 Work with Lists of Data on Android: https://firebase.google.com/docs/database/android/lists-of-data

07 Enabling Offline Capabilities on Android: https://firebase.google.com/docs/database/android/offline-capabilities

**Storage**:


**code snippets that are used in the descriptions**: https://github.com/firebase/snippets-android

**complete code example apps**:

- Authentication: https://github.com/firebase/quickstart-android/tree/master/auth

- Database: https://github.com/firebase/quickstart-android/tree/master/database

- Storage: https://github.com/firebase/quickstart-android/tree/master/storage

**dependencies**:

```plaintext

in settings.gradle:
buildscript {
    repositories {
        // Make sure that you have the following two repositories
        google()  // Google's Maven repository
        mavenCentral()  // Maven Central repository
    }
    dependencies {
        // Add the dependency for the Google services Gradle plugin
        classpath 'com.google.gms:google-services:4.3.14'
    }

in build.gradle (root/project):
    ...
    // Add the Google services Gradle plugin
    id 'com.google.gms.google-services'
    
in build.gradle (app):
    // Import the Firebase BoM
    implementation platform('com.google.firebase:firebase-bom:31.0.2')
    // When using the BoM, you don't specify versions in Firebase library dependencies
    // add the dependencies for Firebase products you want to use
    // See https://firebase.google.com/docs/android/setup#available-libraries
    // For example, add the dependencies for Firebase Authentication
    implementation 'com.google.firebase:firebase-auth'
    // auth with Google account
    // Also add the dependency for the Google Play services library and specify its version
    implementation 'com.google.android.gms:play-services-auth:20.3.0' 
    // Add the dependency for the Realtime Database library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation 'com.google.firebase:firebase-database'       
}


```

some technical details:
```plaintext

package de.androidcrypto.firebaseplayground
SHA1: 19:22:A4:D7:01:A8:3D:09:8F:04:93:E9:8E:21:92:2D:5A:5F:B0:54
```

