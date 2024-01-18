# Features and changes compared to upstream

Note that following list of changes compared to Element X is likely incomplete, since it is only occassionally updated.


## General UI

- Schildi theme with more neutral colors and some design tweaks †
- Schildi layout tweaks †
- Faster screen transitions †
- App icon & branding ⸸


## Chat overview ("room list")

- Sort room list by unread first ‡
- Non-expanding compact app bar in the chat overview †
- Show unread counts ([MSC2654](https://github.com/matrix-org/matrix-spec-proposals/pull/2654)) ‡[^1]
- Filter for spaces, including support for hierarchical spaces †‡


## Conversation screen

- Floating date header while scrolling †
- Render media captions ([MSC2530](https://github.com/matrix-org/matrix-spec-proposals/pull/2530)) ‡
- Bigger stickers


## Experimental/unstable features

- Color usernames in rooms by member powerlevel †


## Other changes

- Force-disable all analytics
- Use schildi.chat servers for rageshake bug reports and FCM push gateway server
- Streamlined settings framework to make settings-controlled features faster to implement


†: Can be partly enabled or disabled by user setting  
‡: Powered by [our own rust-sdk fork](https://github.com/SchildiChat/matrix-rust-sdk)  
⸸: Work in progress  

[^1]: Unread counts for muted chats require `msc2654_enabled: true` in your synapse's `experimental_features` config and a [patched sliding-sync-proxy](https://github.com/SpiritCroc/matrix-sliding-sync/commit/785ce8ca4cac1a17509e3e611d117c1f6860ef2b)
