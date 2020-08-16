# Mention
A LabyMod addon which highlights your name in chat when a player says your username.

This is similar to Lunar Client's username highlight setting, except this one does not have the issue where your name will be highlighted every time
you send a message in chat. It also has more settings which allow the styles of the text and color of the text to be changed easily. By default, a sound also plays
when your name is mentioned, and the volume and the sound itself that plays ("note.harp" by default) can be edited.

## Commands
This addon currently provides 4 commands, which are:
- /mention
- /mention list
- /mention add (split_type) (ip)
- /mention remove (ip)

The available split types or "chat separators" include:
- colon
- double_right_arrow

The above is subject to change and new split types are likely to be added in the future.

## How it works
Unfortunately there is no universal way of splitting the sender of a chat message from the message itself on the 
client-side with the current events that LabyMod provides. In theory, it could be possible with regex, 
but it'd be a very long pattern and you'd have to account for different prefixes on different servers.

Due to these limitations, this addon simply splits messages using String.split() based on a single character chat separator (at this point in time).
The addon sets this chat separator based on the current server that the player is on, and the player is able to set a chat separator for a server
with a command. The main downside to this is that you have to manually set a chat separator/split type for a server if it isn't in the config by default.

For example, the message:
> Username: Chat message

would be split at the character ":".

Keep in mind that this will **not** work on **every** server. For instance, it would be impossible to add support for servers like Mineplex that do not have chat 
separators (e.g. the chat format is "Username Message") without creating separate methods for specific servers.

## Default server support
These are servers which this addon supports by default. You cannot remove these IPs or add the same IPs to the config through the /mention command.

'colon' split type:
- Hypixel
- GommeHD
- ROXBOT

'double_right_arrow' split type:
- The Hive
