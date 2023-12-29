# Features and changes compared to upstream

Note that following list of changes compared to Element X is likely incomplete, since it is only occassionally updated.


## General UI

- Schildi theme with more neutral colors and some design tweaks †
- Schildi layout tweaks †
- Faster screen transitions †
- Non-expanding compact app bar in the chat overview †
- Sort room list by unread first ‡
- App icon & branding ⸸


## Conversation screen

- Floating date header while scrolling †


## Experimental/unstable features

- Color usernames in rooms by member powerlevel †


## Other changes

- Force-disable all analytics
- Use schildi.chat servers for rageshake bug reports and FCM push gateway server
- Streamlined settings framework to make settings-controlled features faster to implement


†: Can be enabled or disabled by user setting  
‡: Powered by [our own rust-sdk fork](https://github.com/SchildiChat/matrix-rust-sdk) until its possible to control upstream  
⸸: Work in progress  
