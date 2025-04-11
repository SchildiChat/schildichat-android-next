# Features and changes compared to upstream

Note that following list of changes compared to Element X is likely incomplete, since it is only occassionally updated.


## General UI & App Behaviour

- Schildi theme with more neutral colors and some design tweaks †
- Schildi layout tweaks †
- Faster screen transitions †
- App icon & branding ⸸
- Customizable colors for message bubbles †


## Chat overview ("room list")

- Filter for spaces, including support for hierarchical spaces †‡
- Filter for favorites, unreads, DMs, and group chats via our spaces navigation
- Configure room list sort order to optionally: †‡
    - Show unread chats on top (while optionally ignoring muted unreads)
    - Pin favorites
    - Show low priority on bottom
- Non-expanding compact app bar in the chat overview †
- Show start-new-chat action in the top app bar rather than as floating action button †
- Show unread counts †
- Show alt text for inline images / custom emotes, instead of not showing them at all
- When filtering for DMs, also treat DMs with more than 2 members as DMs if marked as such


## Conversation screen

- Floating date header while scrolling †
- Bigger stickers
- Differentiate notices from normal text messages by adding some transparency
- Render collapsible `<details>` tags in messages
- Suggest and record frequently used emoji reactions (synced with desktop clients via `io.element.recent_emoji` account data)

- Allow sending freeform reactions
- Don't waste horizontal space in message bubbles with forced line-breaks that do not make full use of the available width
- Render inline images such as custom emotes in text messages †
- Setting for reply preview max lines †

- Disable Element's pinned message overlay on top of the conversation screen †
- Access pinned messages via toolbar action when the pinned message overlay is disabled †


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
- Bigger emoji-only messages: upstreamed at v0.5.2
- Show avatar placeholders instead of blank space for avatars that failed to load
- Copy URLs to clipboard in text messages on longclick
- Copy user MXIDs to clipboard by long-pressing on the member-detail screen


†: Can be partly enabled or disabled by user setting  
‡: Powered by [our own rust-sdk fork](https://github.com/SchildiChat/matrix-rust-sdk)  
⸸: Work in progress  
