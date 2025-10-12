[![Translation status](https://weblate.spiritcroc.de/widgets/schildichat/-/schildichat-android-next/svg-badge.svg)](https://weblate.spiritcroc.de/projects/schildichat/schildichat-android-next/)
[![SchildiChat-Android Matrix room #android:schildi.chat](https://img.shields.io/matrix/android:schildi.chat.svg?label=%23android:schildi.chat&logo=matrix&server_fqdn=matrix.org)](https://matrix.to/#/#android:schildi.chat)

# SchildiChat Android Next

Matrix client based on [Element Android X](https://github.com/element-hq/element-x-android)
in the tradition of the original [SchildiChat for Android](https://github.com/SchildiChat/SchildiChat-android)
which was based on the now deprecated Element Android codebase.

Similarly to Element X, this SchildiChat Android rewrite should still be considered beta,
as it lacks some functionality which one might expect from a fully-featured chat app, compared to the old SchildiChat implementation.

An overview over changes compared to Element X can be found [here](FEATURES.md).

<a href="https://f-droid.org/packages/chat.schildi.android" alt="Get it on F-Droid" target="_blank"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="80"></a>
<a href="https://play.google.com/store/apps/details?id=chat.schildi.android" alt="Get it on Google Play" target="_blank"><img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" height="80"></a>

**Note for F-Droid**: You can get faster updates directly from the developer by adding the [SpiritCroc F-Droid
repository](https://schildi.chat/android/next/install-from-sc-fdroid). These builds usually match the releases in the
official F-Droid repository thanks to [reproducible builds](https://f-droid.org/en/docs/Inclusion_How-To/#reproducible-builds),
but get built directly on app update and do not need to go through a possibly lengthy build process on the F-Droid servers.

**Beta builds** for testing pre-release versions are available from the [SpiritCroc testing F-Droid
repository](https://s2.spiritcroc.de/testing/fdroid/repo/?fingerprint=52d03f2fab785573bb295c7ab270695e3a1bdd2adc6a6de8713250b33f231225)
under a different package name, and can thus be installed next to the stable release.


## Translations

If you want to translate SchildiChat, visit [our weblate](https://weblate.spiritcroc.de/projects/schildichat/schildichat-android-next/).  
Translations that concern upstream Element code are best contributed directly to Element, who currently manage translations on [localazy](https://localazy.com/p/element).


## Screenshots

<img src="https://raw.githubusercontent.com/SchildiChat/schildichat-android-next/main/metadata/en-US/images/phoneScreenshots/1_en-US.png" height="500"/> <img src="https://raw.githubusercontent.com/SchildiChat/schildichat-android-next/main/metadata/en-US/images/phoneScreenshots/2_en-US.png" height="500"/><img src="https://raw.githubusercontent.com/SchildiChat/schildichat-android-next/main/metadata/en-US/images/phoneScreenshots/3_en-US.png" height="500"/>


## Building

In general, building works the same as for Element X or any common Android project.
Just import into Android Studio and make sure you have all the required SDKs ready.


## WYSIWYG development

To develop changes in our [matrix-rich-text-editor fork](https://github.com/SchildiChat/matrix-rich-text-editor):

### Build WYSIWYG locally

- Clone the repo
- Bump the version number to some future version that [doesn't exist yet](https://github.com/SchildiChat/matrix-rich-text-editor/tags)
  using `./update_version 1.2.3` where `1.2.3` is your chosen version number. By not re-using any existing version number you can make sure you're using your
  local build if the build of SchildiChat succeeds.
- Publish the wysiwyg by running `make android` in its directory. (Make sure you have `JAVA_HOME`, `ANDROID_NDK_HOME` and all the build dependencies setup)

### Include local-built WYSIWYG in SchildiChat

- Modify `settings.gradle.kts` to insert `mavenLocal()` into the `dependencyResolutionManagement {}` block.
  ([Cherry-pick](https://github.com/SchildiChat/schildichat-android-next/commit/de2a30082012a079d8978caf7d5af7e5764310a4))
- Change the version number of `wysiwyg` in `gradle/libs.versions.toml` to match the one you published locally.


## Contributing

Generally, contributions are welcome!  
Note that in order to ease upstream merges, we want to leave the smallest footprint possible on Element's sources
when implementing original features or patching Element's behaviour.

In particular (may change a bit while the project is still in alpha):
- Put code into additional files (`chat.schildi.*` package names) if reasonable
    - Prefer `schildi/lib` module if it doesn't depend on element modules (except maybe strings)
    - Prefer `schildi/components` module if it depend on some of Element's Design/UI components but nothing else
    - Otherwise, prefer element module where it makes most sense (or create a new module for bigger features, maybe)
- Put Schildi-specific drawables and other xml resources that override upstream resources into the `sc` build flavor
    - This way, we can use the same name and avoid merge conflicts
    - Compare e.g. `libraries/designsystem`: we define the flavor `sc` and thus put drawables in the `sc` instead of `main` directory.
      For new modules that do not feature a `sc` flavor yet, copy over the required `build.gradle.kts` content from a module that does.
- Put Schildi-specific strings into `schildi/lib`
    - Never touch upstream strings! If we want to change Element's strings, we'll either want a script that patches them,
      so we can restore upstream strings before upstream merges and re-do our changes automatically after the merge,
      or alternative put it into an `sc` build flavor, if we do not need to touch multiple translations.
- Don't worry too much about code style if violating it can ease upstream merges
    - When putting upstream code into a new block (e.g., putting it in an `if`-statement), don't indent the upstream code, but rather add comments like
        `// Wrong indention for merge-ability - start` and `// Wrong indention for merge-ability - end`
