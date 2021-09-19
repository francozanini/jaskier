package org.wancr.jaskier.commands;

import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public interface SlashCommand {
    String getName();
    
    Mono<Void> handle(SlashCommandEvent event);
}
