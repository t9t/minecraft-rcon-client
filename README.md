# Minecraft RCON Client
A client to communicate with a Minecraft server using the RCON (remote control) interface. Contains both a Java
client to be used in other projects, as well as a command-line application.

## Configuring RCON for your Minecraft server
Modify your `server.properties` to include the following properties:

```
enable-rcon=true
rcon.password=<your password>
rcon.port=<1-65535>
```

The default port is 25575. 

## Using the client library in your own project
Add the following Maven dependency:

```xml
<dependency>
  <groupId>com.github.t9t.minecraft-rcon-client</groupId>
  <artifactId>minecraft-rcon-client</artifactId>
  <version>1.0.0</version>
</dependency>
```

Be sure to use the latest version. Check [The Maven Central Repository](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.t9t.minecraft-rcon-client%22%20a%3A%22minecraft-rcon-client%22)
to see which that is.

Use the client in your code as follows:
```java
import com.github.t9t.minecraftrconclient.RconClient;

public class Teleporter {
  public void teleport(String player, int x, int y, int z) {
    try (RconClient client = RconClient.open("localhost", 25575, "hunter2")) {
      client.sendCommand(String.format("say Teleporting player %s to (%d, %d, %d)", player, x, y, z));
      client.sendCommand(String.format("teleport %s %d %d %d", player, x, y, z));
    }
  }
}
```

Note that a connection is only opened when `open()` is called and when the connection becomes broken (eg. because of a
network failure), no attempt is made to re-establish the connection when `sendCommand()` is called.

## Using the command-line application

### Downloading the client
You can download the JAR from The Maven Central Repository, at
[com.github.t9t.minecraft-rcon-client:minecraft-rcon-client](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.t9t.minecraft-rcon-client%22%20a%3A%22minecraft-rcon-client%22),
by clicking `jar` in the Download column on the right. Be sure to fetch the latest version.

### General usage
The command-line application supports to modes: it can either send a list of specified commands to server and quit
immediately, or it can be started as an interactive terminal to type in and send commands. In general, the usage is:

```
java -jar minecraft-rcon-client-<version>.jar <host[:port]> <password> <-t|commands>
```

The port number is optional. When omitted, the default of `25575` will be used.

### Send a list of commands from the command line
Specify a list of commands as arguments separated by spaces. Please take care to understand how to write arguments which
contain spaces for your particular terminal. For example:

```
skankhunt42@battlestation:~$ export PLAYER=skankhunt42 COORDS='0 200 0'
skankhunt42@battlestation:~$ java -jar minecraft-rcon-client-1.0.0.jar localhost:12587 hunter2 \
  "say Teleporting $PLAYER to ($COORDS)" \
  "teleport $PLAYER $COORDS"
> say Teleporting skankhunt42 to (0 200 0)
< (empty response)
> teleport skankhunt42 0 200 0
< (empty response)
skankhunt42@battlestation:~$ 
```


### Start in interactive terminal mode
Just use `-t` instead of any commands. You will be presented with an interactive terminal where you can type a command.
Press Enter to send the command. The response will be displayed. In a lot of cases when a command was successfully
sent and executed, the server will return with an empty response however, in which case the client shows
`(empty response)`, so as not to confuse the user.

To exit the terminal write `\quit`, [use your terminal's keyboard shortcut to send an end-of-file (`^D` on most
terminals)](https://superuser.com/questions/169051/whats-the-difference-between-c-and-d-for-unix-mac-os-x-terminal),
or use `^C`.

For example:

```
skankhunt42@battlestation:~$ java -jar minecraft-rcon-client-1.0.0.jar localhost:12587 hunter2 -t
> say Teleporting skankhunt42 to (0 200 0)
< (empty response)
> teleport skankhunt42 0 200 0
< (empty response)
> \quit
skankhunt42@battlestation:~$  
```

## References
- Page describing the Minecraft RCON protocol: http://wiki.vg/RCON
- Minecraft `server.properties` documentation: http://minecraft.gamepedia.com/Server.properties
