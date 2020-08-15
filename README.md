# Mention
A LabyMod addon which highlights your name in chat when a player says your username.

## How it works
Unfortunately there is no universal way of splitting the sender of a chat message from the message itself on the 
client-side with the current events that LabyMod provides. In theory, it could be possible with regex, 
but it'd be a very long pattern and you'd have to account for different prefixes on different servers.

Due to these limitations, this addon simply splits messages using String.split() based on a single character (at this point in time) chat separator.
The addon sets this chat separator based on the current server that the player is on, and the player is able to set a chat separator for a server
with a command.

For example, the message:
> Username: Chat message

would be split at the character ":".

Keep in mind that this will **not** work on **every** server. For instance, it would be impossible to add support for servers like Mineplex that do not have chat 
separators (e.g. the chat format is "Username Message") without creating separate methods for specific servers.

## Commands
This addon currently provides 3 commands, which are:
- /mention list
- /mention add <split_type> <ip>
- /mention remove <ip>
