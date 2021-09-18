package org.wancr.jaskier;

public class Jaskier {

    public static void main(String[] args) {
        final DiscordClient client = new DiscordClientBuilder(args[0]).build();
        client.login().block();
    }
}
