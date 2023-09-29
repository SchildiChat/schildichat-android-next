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
- Put Schildi-specific drawables and other resources into the `sc` build flavor
    - This one is new for schildichat-android-next and needs some evaluation if it can fully replace the next point
- Put new resources like strings in dedicated files suffixed with `_sc`, such as `strings_sc.xml`
    - Never touch upstream strings! If we want to change Element's strings, we'll want a script that patches them,
      so we can restore upstream strings before upstream merges and re-do our changes automatically after the merge
- Don't worry too much about code style if violating it can ease upstream merges
    - When putting upstream code into a new block (e.g., putting it in an `if`-statement), don't indent the upstream code, but rather add comments like
        `// Wrong indention for upstream merges` and `// Wrong indention for upstream merges - end`
