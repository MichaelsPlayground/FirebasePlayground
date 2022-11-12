# Firebase setup

To run the app you need an individual google-services.json file, here are the steps to setup 
the Firebase service.

But before we are starting the flow it is important that you **exclude the google-services.json-file 
from uploading to a public GitHub repository**. This is important as the file contains an API-key 
and when you are on a Firebase billing plan this data could get abused.

In your Android Studio change the view to **Project** and find a file ".gitignore" in the top level 
folder structure. Open it and append one line:

```plaintext
*.iml
.gradle
...
google-services.json
```

This will prevend from any upload of the file.

This is the flow to setup a Firebase service:

1) To run this app you need a connection between your app and Google's Firebase service, so go to:

https://firebase.google.com

login with your Google account and proceed to **console**.

2) Add a new project (in German "Project hinzufuegen")

3) There are 3 steps to create a new Firebase project.

Step 1: give a name for the project, I named it "FirebaseChatApp"

Below you see an automatic generated (unambiguously) internal name (here it is something like "fir-chatapp-a5632, your name will differ).

Step 2: (don't) use Google Analytics within your project

I'm for sure that Analytics is a fantastic product but I'm disabling it for this project.

Proceed with "Project erstellen" = "create project"

Step 3: After some seconds your project is ready to use, press ("Weiter" = "proceed")

4) We are now on the project overview page and the first step is to add an "App" to the project, 
meaning to add the platform (Android) to the project. Simply press the Android icon and proceed

5) You need to register your app ("App registrieren") by the package name and the SHA-1-hash of your configuration.

The way to get both data is to switch to Android Studio and copy and paste the data.

For the package name data: got to the top of your MainActivity.java class and copy the package name 
(my paket name is "com.example.chatapp") and paste it to the webpage.

The second step is not necessary at the moment but for later steps you will some crazy error messages if 
Firebase does not have the information.

For the SHA-1 value ("SHA-1 Wert...") open the Gradle tab within Android Studio:

Press the "Execute Gradle Task button/icon", enter behind gradle signingReport and press return.

See the "Run" window in Android Studio and scroll down:

```plaintext
> Task :app:signingReport
Variant: debug
Config: debug
Store: /Users/michaelfehr/.android/debug.keystore
Alias: AndroidDebugKey
MD5: 4D:1E:D7:91:56:67:73:EA:54:73:BC:EC:D7:E5:9B:F5
SHA1: 19:22:A4:D7:01:A8:3D:09:8F:04:93:E9:8E:21:92:2D:5A:5F:B0:54
SHA-256: A7:A8:66:27:C7:76:6D:C3:3C:9E:3F:89:99:88:3E:A1:7B:ED:34:69:19:83:B6:EA:72:04:C9:13:8E:84:E0:90
Valid until: Samstag, 30. September 2051
----------
```

We do need just the value for the SHA-1 hash - SHA1-values and paste them to the webpage.

My value is "19:22:A4:D7:01:A8:3D:09:8F:04:93:E9:8E:21:92:2D:5A:5F:B0:54", your will differ.

Important note: the SHA-1 value is from the DEBUG configuration in my case. If you are going to 
publish your app on the PlayStore you need to give the SHA-1 value of the RELEASE configuration.

Now press the "App registrieren" button to register the app.

6) The Firebase wizard is creating a configuration file that you need to copy to the project in Android Studio:

Download the file by pressing on "google-services.json herunterladen"

Move the downloaded file "google-services.json" to the **app folder** of your app as shown in the screenshot:

One possible way is shown here: Change the view from "Android" to "Project" and drag the "google-services.json"
file from your download folder to the app folder

On the wizzard page press "Weiter" to proceed.

Step 3 of the wizzard shows how to append the Firebase SDK to the project (here done in the beginning).

Note: the page is giving the classpath of the google-services with the actual library version number and 
the firebase library version:

```plaintext
classpath 'com.google.gms:google-services:4.3.13'
...
implementation platform('com.google.firebase:firebase-bom:31.0.2')
```

On the wizzard page press "Weiter" to proceed.

Step 4 of the wizzard is "Nächste Schritte" = "next steps", proceed with the "Weiter zur Konsole" =
"go to console" button.

7) We are back on our overview page and we need to activate "Authentication", so please press "Authentication" 
on the left sided menu.

There is a short introduction and a video with a short explanation available on Youtube (I'm not the author):

https://youtu.be/8sGY55yxicA

Press the "Los gehts" = "start" button

8) We start with the "Sign-in method" tab of the Authentication menu. The different types of possible authentication 
are named "providers", we are choosing "Email-Adresse/Passwort" = "email address with password" for 
this tutorial.

Activate the first option "E-Mailadresse/Passwort" = "email address / password" but 
**NOT the second option** "E-Mail-Link" ("email link"). Proceed with "speichern" = "save".

The "Sign-in method"-tag shows that we have activated the authentication with an 
Email address and password ("Aktiviert" = "activated").

9) As we would like to sign-in with our Google account please press "Neuen Anbieter hinzufuegen"  
(="add a new authentication provider") and click on the Google icon: 

Activate the switch and choose the support EMail for this project ("Support E-Mail fuer das Project"), then 
press "speichern" = "save".

10) Now we are ready to use Firebase Authentication with email address and password. Below on the page 
you get a notice that there is a registration limit for new users, at the moment it is limited to register 
100 new email addresses per hour. The limit may change in the future or when using a Firebase 
payed plan, I'm using a free "Spark" plan for my tutorial.

For now the app is connected with the Firebase server and allows to sign-up and sign-in (new) users. There is 
one part missing and that is the storage part because the chat messages were stored in Firebase as well.

11) In the menu on the left side (click on "Entwickeln" = "develop" to the full menu) click on "Realtime Database"

12) Click on "Datenbank erstellen" (="create a database")

step 1: choose the location of the database server - as this cannot changed later it is import to choose the "right"  
location, best is the region near to your expected users (in my example I'm choosing Belgium)

step 2: choose the security rules

In my example I'm using the "Testmodus" (="test mode") that means that **for 30 days your users can work as expected but 
this is a "trial period"**. After the trial period you receive a notification mail and the access get denied. To get a 
permanent access you need to define your own security rules but this is explained in a following article.

To proceed press "aktivieren" (="activate")

14) we are in the Realtime Database section and the 4 tabs with data ("Daten"), rules ("=Regeln"),  
backups ("Sicherungen") and usage ("Nutzung"). click on "Regeln" to see the activated rules that show the trial period:

```plaintext
{
  "rules": {
    ".read": "now < 1670626800000",  // 2022-12-10
    ".write": "now < 1670626800000",  // 2022-12-10
  }
}
```

change rules to:
```plaintext
{
  "rules": {
    ".read": "auth.uid != null",
       ".write": "auth.uid != null"
  }
}
```

But nothing happens, see the log:

**Firebase Database connection was forcefully killed by the server. Will not attempt reconnect. Reason: Database lives in a different region.**

Solutions from https://stackoverflow.com/questions/68806876/firebase-realtime-database-connection-killed-different-region

```plaintext
It looks like the google-services.json file that you use doesn't contain the Realtime Database URL, probably because you downloaded it before the database was created. In such cases the SDK assumes that the database is in the US (the original region), and you get an error that there's a mismatch.

There are two possible solutions:

Download an updated google-services.json from the Firebase console, and add that to your Android app.

Specify the database URL in your code instead, like this: FirebaseDatabase.getInstance("https://vax-in-60807-default-rtdb.asia-southeast1.firebasedatabase.app")...

Both have the same result, so pick whichever one seems easiest to you.
```

So go to your Firebase console, click on the settings symbol right to "Projectuebersicht" (="project overview") and choose 
"Projecteinstellungen" (="project settings") in the sub menu:

Scroll down to "Meine Apps" (="my apps") and download the google-services.json file again and don't forget to place 
the file in the app folder again as well.

Now run the app and see what is happening...



As a last view let's move to the "Users" tab in Authentication submenu. At the moment 
there are no users because we haven't signed up any user but you could add a user 
manually by pressing the button "Nutzer hinzufuegen" = "add a new user".

still missing: Firebase Storage to add user based content

15) In the menu on the left side (click on "Entwickeln" = "develop" to the full menu) click on "Storage"

16) Click on "Jetzt starten" (="start now")

step 1: choose the security rules

In my example I'm using the "Testmodus" (="test mode") that means that **for 30 days your users can work as expected but
this is a "trial period"**. After the trial period you receive a notification mail and the access get denied. To get a
permanent access you need to define your own security rules but this is explained in a following article.

test mode:
```plaintext
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if
          request.time < timestamp.date(2022, 12, 11);
    }
  }
}
```

production mode:
```plaintext
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: true
    }
  }
}
```

Click on "weiter" (="next step")

step 2: choose the location of the database server - as this cannot changed later it is import to choose the "right" 
location, best is the region near to your expected users (in my example I'm choosing Europe-West)

click on "Fertig" (="ready")

17) we are in the Storage section and the 3 tabs with Files ("Dateien"), Rules ("=Regeln"), 
    and usage ("Nutzung"). click on "Regeln" to see the activated rules that show the trial period:

click on "Rules" and change the rules to:

```plaintext
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
      // or allow write: if true;
    }
  }
}
```

Now the same happens - we need to download the google-services.json file again:

So go to your Firebase console, click on the settings symbol right to "Projectuebersicht" (="project overview") and choose
"Projecteinstellungen" (="project settings") in the sub menu:

Scroll down to "Meine Apps" (="my apps") and download the google-services.json file again and don't forget to place
the file in the app folder again as well.

Now the app is ready to build and deploy but keep in mind: for using a release version you need 
another SHA-1 hash !
