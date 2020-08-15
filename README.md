# Mention
A LabyMod addon which highlights your name in chat when a player says your username.

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
with a command.

For example, the message:
> Username: Chat message

would be split at the character ":".

Keep in mind that this will **not** work on **every** server. For instance, it would be impossible to add support for servers like Mineplex that do not have chat 
separators (e.g. the chat format is "Username Message") without creating separate methods for specific servers.
