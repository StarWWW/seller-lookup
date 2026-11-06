Seller Lookup gets you to a seller in one move. Hover a listing in a server's auction house and hold **Shift**: the screen closes and the chat opens with `/ah <seller>` already typed. Hold **Ctrl** for `/stats <seller>`. Nothing is sent until you press Enter, so you can still change the command or press Esc.

**Features**

- **Hold, don't tap:** the command is only typed after the key has been held over the same listing for a moment (0.4 s by default). A quick Shift press or a shift-click still works as usual, and clicking while the key is held cancels it.
- **Shows what it will type:** the listing's tooltip gets a line such as `Hold Shift: /ah Notch`, so you can see the seller was read right before pressing anything.
- **Your own commands:** change both commands in the settings, for example `/msg {seller} still for sale?`. `{seller}` becomes the seller's name; an empty command turns that key off.
- **Reads the seller loosely:** lines such as `Seller: [VIP] Notch`, `Satıcı » .BedrockName` or `Owner:` with the name on the next line all work. Ranks are skipped, and Bedrock names keep their `.` or `*`. For servers that word it differently, set your own regular expression in the config file.
- **Any screen with a seller:** chest-like screens and server confirmation windows. Some servers only name the seller in the window you get by right-clicking a listing ("Are you sure you want to buy this?"). The mod never clicks anything there, so nothing is bought unless you press Yes yourself.
- The screen is closed the same way Esc closes it, so the server is told.
- Settings screen, Mod Menu support, a key you can bind for the settings, and 19 languages.

**Requirements**

- Minecraft 26.1.x, 26.2 or 26.3, Fabric Loader 0.19 or newer, Java 25
- Fabric API
- Optional: Mod Menu

Client-side only: it only reads the lines the server already shows you and types into your chat box; the server needs nothing.
