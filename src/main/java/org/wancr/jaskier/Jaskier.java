package org.wancr.jaskier;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.voice.AudioProvider;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Jaskier {
    private static final Map<String, Command> commands = new HashMap<>();
    
    static {
        commands.put("ping", event -> event.getMessage().getChannel()
                                           .flatMap(channel -> channel.createMessage("Pong!"))
                                           .then());
    }
    
    
    public static void main(String[] args) {
        // Creates AudioPlayer instances and translates URLs to AudioTrack instances
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    
        // This is an optimization strategy that Discord4J can utilize.
        // It is not important to understand
        playerManager.getConfiguration()
                     .setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
    
        // Allow playerManager to parse remote sources like YouTube links
        AudioSourceManagers.registerRemoteSources(playerManager);
    
        // Create an AudioPlayer so Discord4J can receive audio data
        final AudioPlayer player = playerManager.createPlayer();
    
        // We will be creating LavaPlayerAudioProvider in the next step
        AudioProvider provider = new LavaPlayerAudioProvider(player);
    
        commands.put("join", event -> Mono.justOrEmpty(event.getMember())
                                          .flatMap(Member::getVoiceState)
                                          .flatMap(VoiceState::getChannel)
                                          // join returns a VoiceConnection which would be required if we were
                                          // adding disconnection features, but for now we are just ignoring it.
                                          .flatMap(channel -> channel.join(spec -> spec.setProvider(provider)))
                                          .then());
    
        final TrackScheduler scheduler = new TrackScheduler(player);
        commands.put("play", event -> Mono.justOrEmpty(event.getMessage().getContent())
                                          .map(content -> Arrays.asList(content.split(" ")))
                                          .doOnNext(command -> playerManager.loadItem(command.get(1), scheduler))
                                          .then());
        
        final GatewayDiscordClient client = DiscordClientBuilder.create(args[0]).build()
                                                                .login()
                                                                .block();
        
        client.getEventDispatcher().on(MessageCreateEvent.class)
              .flatMap(event -> Mono.justOrEmpty(event.getMessage().getContent())
                                    .flatMap(content -> Flux.fromIterable(commands.entrySet())
                                                            .filter(entry -> content.startsWith("$" + entry.getKey()))
                                                            .flatMap(entry -> entry.getValue().execute(event))
                                                            .next()))
              .subscribe();
        
        client.onDisconnect().block();
    }
}
