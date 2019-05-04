# LEDDimmerWidget
Is a Android App that can communicate with a Python based [REST-Server](https://github.com/terminator-ger/LedDimmerServer) on your Raspi that lets you control attached LED-Strips.

## Install 
Just install [Android Studio](https://developer.android.com/studio/) and import the project. Gradle handles it all. 

In LedDimmerWidget\app\src\main\java\lechnersoft\leddimmerwidget\DimmerWidget.java you need to specify the IP under which your Raspi can be found.

```java
 private static String destinationAdress = "http://192.168.1.123/";
``` 

Then attach your Android phone, get it into [Developer Mode](https://developer.android.com/studio/debug/dev-options.html#enable) and install the compiled widget.