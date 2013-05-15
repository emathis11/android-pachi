# Pachi for Android

This is the Android version of [Pachi][1], a Go game engine developed by Petr Baudis and Jean-Loup Gailly.

## How to compile

1. Create an android application project in the `android` folder.
2. Create an android library project for each project located in the `libprojects` folder.
3. Reference these library projects from your application project.
4. Compile pachi with the [Android NDK][2].
   To do this, open the `android` folder and enter the command `ndk-build` (this will take some time).
   If you get errors, try to use the NDK version r8b. Sometimes newer versions require to make some changes to the code.
5. Build the android project and it should run !

## Credits

- [The Pachi project](http://pachi.or.cz)
- [elygo-lib](http://github.com/Daimas/elygo-lib)
- [ActionBarSherlock](http://actionbarsherlock.com)

## License

Pachi for Android is distributed under the GPLv2 license (see the COPYING file for details and full text of the license). You are welcome to tweak it as you wish and distribute it freely, but only together with the source code.

  [1]: http://pachi.or.cz
  [2]: http://developer.android.com/tools/sdk/ndk/index.html
