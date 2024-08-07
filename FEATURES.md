# Features and changes compared to upstream

Note that following list of changes compared to Element X is likely incomplete, since it is only occassionally updated.


## General UI

- Schildi theme with more neutral colors and some design tweaks †
- Schildi layout tweaks †
- Faster screen transitions †
- Show avatar placeholders instead of blank space for avatars that failed to load
- App icon & branding ⸸


## Chat overview ("room list")

- Filter for spaces, including support for hierarchical spaces †‡
- Filter for favorites, unreads, DMs, and group chats via our spaces navigation
- Sort room list by unread first ‡
- Non-expanding compact app bar in the chat overview †
- Show unread counts ([MSC2654](https://github.com/matrix-org/matrix-spec-proposals/pull/2654)) ‡[^1]
- Experimental client-side unread sorting to respect client-side mention detection and more †
- Option to show favorites on top †


## Conversation screen

- Floating date header while scrolling †
- Bigger stickers
- Differentiate notices from normal text messages by adding some transparency
- Render collapsible `<details>` tags in messages
- Suggest and record frequently used emoji reactions (synced with desktop clients via `io.element.recent_emoji` account data)

- Allow sending freeform reactions
- Bigger emoji-only messages
- Don't waste horizontal space in message bubbles with forced line-breaks that do not make full use of the available width


## Notifications

- Setting to only alert once for successive messages in the same chat


## Experimental/unstable features

- Color usernames in rooms by member powerlevel †


## Other changes

- Force-disable all analytics
- Use schildi.chat servers for rageshake bug reports and FCM push gateway server
- Streamlined settings framework to make settings-controlled features faster to implement
- Use [UnifiedPush's FOSS FCM distributor](https://github.com/UnifiedPush/android-foss_embedded_fcm_distributor) instead of bundling Google's closed source FCM libraries
- Misc bug fixes related to upstream bugs (if they're annoying me enough to not wait for Element to fix them. Usually we'll try to contribute these back upstream)


## Historic

- Render media captions ([MSC2530](https://github.com/matrix-org/matrix-spec-proposals/pull/2530)): has been implemented upstream at v0.4.7


†: Can be partly enabled or disabled by user setting  
‡: Powered by [our own rust-sdk fork](https://github.com/SchildiChat/matrix-rust-sdk)  
⸸: Work in progress  

[^1]: Unread counts for muted chats require `msc2654_enabled: true` in your synapse's `experimental_features` config and a [patched sliding-sync-proxy](https://github.com/SpiritCroc/matrix-sliding-sync/commit/785ce8ca4cac1a17509e3e611d117c1f6860ef2b)
