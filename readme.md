# schildichat-android-next

Matrix client based on [Element Android X](https://github.com/vector-im/element-x-android)
in the tradition of the original [SchildiChat for Android](https://github.com/SchildiChat/SchildiChat-android)
which was based on the now deprecated Element Android codebase.

Similarly to Element X, this SchildiChat Android rewrite is still in alpha state and lacks most original features,
and hasn't even been entirely rebranded yet.


## Translations

TODO


## Screenshots

TODO - I should look into fastlane or something to generate these automatically this time


## Contributing

Generally, contributions are welcome!  
Note that in order to ease upstream merges, we want to leave the smallest footprint possible on Element's sources
when implementing original features or patching Element's behaviour.

In particular (may change a bit while the project is still in alpha):
- Put code into additional files (`chat.schildi.*` package names) if reasonable
    - Prefer `schildilib` module if it doesn't depend on element modules
    - Otherwise, prefer element module where it makes most sense (or create a new module for bigger features, maybe)
- Put Schildi-specific drawables and other xml resources into the `sc` build flavor
    - This way, we can easily overwrite upstream resources as well, by using the same name
    - Compare e.g. `libraries/designsystem`: we define the flavor `sc` and thus put drawables in the `sc` instead of `main` directory.
      For new modules that do not feature a `sc` flavor yet, copy over the required `build.gradle.kts` content from a module that does.
- Put Schildi-specific strings into `schildilib`
    - Never touch upstream strings! If we want to change Element's strings, we'll either want a script that patches them,
      so we can restore upstream strings before upstream merges and re-do our changes automatically after the merge,
      or alternative put it into an `sc` build flavor, if we do not need to touch multiple translations.
- Don't worry too much about code style if violating it can ease upstream merges
    - When putting upstream code into a new block (e.g., putting it in an `if`-statement), don't indent the upstream code, but rather add comments like
        `// Wrong indention for merge-ability - start` and `// Wrong indention for merge-ability - end`
