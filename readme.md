[![Translation status](https://weblate.spiritcroc.de/widgets/schildichat/-/schildichat-android-next/svg-badge.svg)](https://weblate.spiritcroc.de/projects/schildichat/schildichat-android-next/)
[![SchildiChat-Android Matrix room #android:schildi.chat](https://img.shields.io/matrix/android:schildi.chat.svg?label=%23android:schildi.chat&logo=matrix&server_fqdn=matrix.org)](https://matrix.to/#/#android:schildi.chat)

# schildichat-android-next

Matrix client based on [Element Android X](https://github.com/element-hq/element-x-android)
in the tradition of the original [SchildiChat for Android](https://github.com/SchildiChat/SchildiChat-android)
which was based on the now deprecated Element Android codebase.

Similarly to Element X, this SchildiChat Android rewrite should still be considered beta,
as it lacks some functionality which one might expect from a fully-featured chat app, compared to the old SchildiChat implementation.

An overview over changes compared to Element X can be found [here](FEATURES.md).

<a href="https://schildi.chat/android/next/install-from-sc-fdroid" alt="Get it on F-Droid" target="_blank"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="80"></a>
<a href="https://play.google.com/store/apps/details?id=chat.schildi.android" alt="Get it on Google Play" target="_blank"><img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" height="80"></a>


## Translations

If you want to translate SchildiChat, visit [our weblate](https://weblate.spiritcroc.de/projects/schildichat/schildichat-android-next/).  
Translations that concern upstream Element code are best contributed directly to Element, who currently manage translations on [localazy](https://localazy.com/p/element).


## Screenshots

<img src="https://raw.githubusercontent.com/SchildiChat/schildichat-android-next/main/metadata/en-US/images/phoneScreenshots/1_en-US.png" height="500"/> <img src="https://raw.githubusercontent.com/SchildiChat/schildichat-android-next/main/metadata/en-US/images/phoneScreenshots/2_en-US.png" height="500"/><img src="https://raw.githubusercontent.com/SchildiChat/schildichat-android-next/main/metadata/en-US/images/phoneScreenshots/3_en-US.png" height="500"/>


## Building

In general, building works the same as for Element X or any common Android project.
Just import into Android Studio and make sure you have all the required SDKs ready.

Currently, SchildiChat uses a [forked matrix-rust-sdk](https://github.com/SchildiChat/matrix-rust-sdk)
published on [GitHub packages](https://github.com/SchildiChat/matrix-rust-components-kotlin/packages/),
which unfortunately [still does not provide unauthenticated access](https://github.com/orgs/community/discussions/26634).
Accordingly, before building, create a GitHub token with the `read:packages` permission, and configure it in your `local.properties`:
```
gpr.user=...
gpr.token=...
```

Alternatively, you may export the `GPR_USER` and `GPR_TOKEN` environment variables before building.

If you do not have a GitHub account, you can also download the appropriate `.aar` file from
[here](https://github.com/SchildiChat/matrix-rust-components-kotlin/releases) and put it into `./libraries/rustsdk/matrix-rust-sdk.aar`.


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
