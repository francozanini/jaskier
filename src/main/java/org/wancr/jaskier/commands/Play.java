package org.wancr.jaskier.commands;

import discord4j.core.event.domain.interaction.SlashCommandEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class Play implements SlashCommand {
    
    
    @Override
    public String getName() {
        return "ping";
    }
    
    @Override
    public Mono<Void> handle(SlashCommandEvent event) {
        //We reply to the command with "Pong!" and make sure it is ephemeral (only the command user can see it)
        System.out.println(event);
        return event.reply()
                    .withEphemeral(true)
                    .withContent("Pong!");
    }
}
