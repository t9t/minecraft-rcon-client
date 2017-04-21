package com.github.t9t.minecraftrconclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class RconClientCli {
    private static final int EXIT_CODE_SUCCESS = 0;
    private static final int EXIT_CODE_INVALID_ARGUMENTS = 1;
    private static final int EXIT_CODE_AUTH_FAILURE = 2;

    private static final int DEFAULT_PORT = 25575;
    private static final String QUIT_COMMAND = "\\quit";

    public static void main(String[] args) {
        int exitCode = run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static int run(String[] args) {
        if (args.length < 3) {
            return printUsage();
        }

        String[] hostAndPort = args[0].split(":");
        if (hostAndPort.length > 2) {
            return printUsage();
        }

        String host = hostAndPort[0];
        int port = hostAndPort.length == 2 ? Integer.parseInt(hostAndPort[1]) : DEFAULT_PORT;
        String password = args[1];

        List<String> commands = new ArrayList<>(Arrays.asList(args).subList(2, args.length));

        boolean terminalMode = commands.contains("-t");
        if (terminalMode && commands.size() != 1) {
            return printUsage();
        }

        try (RconClient client = RconClient.open(host, port, password)) {
            // This ensures the connection is closed when CTRL+C is used
            Runtime.getRuntime().addShutdownHook(new Thread(client::close));

            if (terminalMode) {
                System.out.println("Authenticated. Type \"" + QUIT_COMMAND + "\" to quit.");
                System.out.print("> ");
                Scanner scanner = new Scanner(System.in);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();

                    if (line.trim().equals(QUIT_COMMAND)) {
                        break;
                    }

                    String response = client.sendCommand(line);

                    System.out.println("< " + (response.isEmpty() ? "(empty response)" : response));
                    System.out.print("> ");
                }
            } else {
                for (String command : commands) {
                    System.out.println("> " + command);
                    String response = client.sendCommand(command);
                    System.out.println("< " + (response.isEmpty() ? "(empty response)" : response));
                }
            }
        } catch (AuthFailureException e) {
            System.err.println("Authentication failure");
            return EXIT_CODE_AUTH_FAILURE;
        }

        return EXIT_CODE_SUCCESS;
    }

    private static int printUsage() {
        System.out.println("Usage: java -jar jarcon-<version>.jar <host[:port]> <password> <-t|commands>"); // TODO: usage
        System.out.println();
        System.out.println("Example 1: java -jar jarcon-1.0.0.jar localhost:12345 hunter2 'say Hello, world' 'teleport Notch 0 0 0'");
        System.out.println("Example 2: java -jar jarcon-1.0.0.jar localhost:12345 hunter2 -t");
        System.out.println();
        System.out.println("The port can be omitted, the default is 25575.");
        System.out.println("\"-t\" enables terminal mode, to enter commands in an interactive terminal.");
        return EXIT_CODE_INVALID_ARGUMENTS;
    }
}
