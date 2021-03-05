package ru.lyuchkov;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import ru.lyuchkov.interfaces.Command;
import ru.lyuchkov.parse.Parser;
import ru.lyuchkov.player.GuildMusicManager;
import ru.lyuchkov.player.TrackScheduler;
import ru.lyuchkov.utils.InputUtils;
import ru.lyuchkov.utils.OutputUtils;
import ru.lyuchkov.utils.UrlUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Bot {
    private static final ConcurrentHashMap<String, Command> commands = new ConcurrentHashMap<>();
    private static AudioPlayerManager playerManager;
    @SuppressWarnings("FieldMayBeFinal")
    private static Map<Long, GuildMusicManager> musicManagers = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        final GatewayDiscordClient client = DiscordClientBuilder.create(args[0]).build()
                .login()
                .block();
        assert client != null;
        playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration()
                .setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager);

        putCommands();
        client.getEventDispatcher()
                .on(MessageCreateEvent.class)
                .subscribe(event -> {
                    final String content = event.getMessage().getContent();
                    for (final Map.Entry<String, Command> entry : commands.entrySet()) {
                        if (content.startsWith('$' + entry.getKey())) {
                            entry.getValue().execute(event);
                            break;
                        }
                    }
                });
        client.onDisconnect().block();
    }

    public static synchronized GuildMusicManager getGuildPlayerManager(Guild guild) {
        long guildId = guild.getId().asLong();
        GuildMusicManager musicManager = musicManagers.get(guildId);
        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }
        return musicManager;
    }

    private static void putCommands() {
        commands.put("p", Bot::play);
        commands.put("pt", Bot::playTop);
        commands.put("pause", Bot::pause);
        commands.put("resume", Bot::resume);
        commands.put("fs", Bot::skip);
        commands.put("join", Bot::join);
        commands.put("exit", Bot::exit);
        commands.put("q", Bot::printQueue);
        commands.put("alive", Bot::printAlive);
        commands.put("help", Bot::printHelp);
        commands.put("clr", Bot::clearQueue);
        commands.put("volume", Bot::setVolume);
        commands.put("remove", Bot::deleteElement);
        commands.put("np", Bot::printNowPlay);
        commands.put("grab", Bot::grab);
        commands.put("lyrics", Bot::printLyrics);
        commands.put("loop", Bot::loop);
        commands.put("unloop", Bot::unLoop);
    }



    private synchronized static void playTop(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) join(event);
        guildMusicManager.player.addListener(guildMusicManager.scheduler);
        AudioLoadResultHandler audioLoadResultHandler = new AudioLoadResultHandler() {


            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                guildMusicManager.scheduler.setFirstAtQueue(audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                guildMusicManager.scheduler.setFirstAtQueueList(audioPlaylist.getTracks());
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {
                e.printStackTrace();
            }
        };
        String content = InputUtils.getValidCommand("pt", event.getMessage().getContent());
        List<String> command = InputUtils.getValidList(content);
        if (command.isEmpty()) return;
        try {
            if (UrlUtils.isUrl(command.get(0))) {
                playerManager.loadItem(command.get(0), audioLoadResultHandler);
                Objects.requireNonNull(event.getMessage()
                        .getChannel().block()).
                        createMessage("Добавил в начало").block();
            } else {
                String url = Parser.getYoutubeUrl(command);
                if (!url.equals("error")) {
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block()).
                            createMessage("Нашел: " + url).block();
                    playerManager.loadItem(url, audioLoadResultHandler);
                } else {
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block()).
                            createMessage("Не нашел.").block();
                }
            }
        } catch (FriendlyException e) {
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block()).
                    createMessage("Проблема с доступом в джойказино.").block();
        }

    }

    private synchronized static void play(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) join(event);
        guildMusicManager.player.addListener(guildMusicManager.scheduler);
        String content = InputUtils.getValidCommand("p", event.getMessage().getContent());
        List<String> command = InputUtils.getValidList(content);
        if (command.isEmpty()) return;
        try {
            if (UrlUtils.isUrl(command.get(0))) {
                String url = command.get(0);
                playerManager.loadItemOrdered(guildMusicManager, url, guildMusicManager.resultHandler);
                Objects.requireNonNull(event.getMessage()
                        .getChannel().block()).
                        createMessage("Добавил").block();
            } else {
                String url = Parser.getYoutubeUrl(command);
                if (!url.equals("error")) {
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block()).
                            createMessage("Нашел: " + url).block();
                    playerManager.loadItemOrdered(guildMusicManager, url, guildMusicManager.resultHandler);
                } else {
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block()).
                            createMessage("Не нашел.").block();
                }
            }
        } catch (FriendlyException e) {
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block()).
                    createMessage("Проблема с доступом в джойказино.").block();
        }
    }
    public static synchronized void deleteElement(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        final String numAsStr = InputUtils.getValidCommand("remove", event.getMessage().getContent()).replaceAll(" ", "");
        int position;
        int size = TrackScheduler.getQueue().size();
        if (!numAsStr.isEmpty()) {
            try {
                position = Integer.parseInt(numAsStr);
                if (position > size || position < 1) {
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block())
                            .createMessage("Тебя в школе считать не научили?").block();
                } else {
                    guildMusicManager.scheduler.delete(position - 1);
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block())
                            .createMessage("Удалил.").block();
                }
            } catch (NumberFormatException e) {
                Objects.requireNonNull(event.getMessage()
                        .getChannel().block())
                        .createMessage("Некорректный формат команды.").block();
            }
        }
    }

    private synchronized static void skip(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        guildMusicManager.scheduler.nextTrack();
        Objects.requireNonNull(event.getMessage()
                .getChannel().block())
                .createMessage("Мне тоже не нравится").block();
    }

    private synchronized static void pause(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        guildMusicManager.scheduler.setPause(true);
    }

    private synchronized static void resume(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        guildMusicManager.scheduler.setPause(false);

    }

    private synchronized static void join(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        final Member member = event.getMember().orElse(null);
        if (member != null) {
            final VoiceState voiceState = member.getVoiceState().block();
            if (voiceState != null) {
                final VoiceChannel channel = voiceState.getChannel().block();
                if (channel != null) {
                    channel.join(spec -> spec.setProvider(guildMusicManager.provider)).block();
                    guildMusicManager.setConnected(true);
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block()).
                            createMessage("Готов врубать фонк").block();
                }
            }
        }
    }

    private synchronized static void exit(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        final Member member = event.getMember().orElse(null);
        if (member != null) {
            final VoiceState voiceState = member.getVoiceState().block();
            if (voiceState != null) {
                final VoiceChannel channel = voiceState.getChannel().block();
                if (channel != null) {
                    channel.sendDisconnectVoiceState().block();
                    guildMusicManager.player.stopTrack();
                    guildMusicManager.setConnected(false);
                    clearQueue(event);
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block()).
                            createMessage("Пока").block();
                }
            }
        }
    }

    private synchronized static void setVolume(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        final String content = InputUtils.getValidCommand("volume", event.getMessage().getContent()).replaceAll(" ", "");
        if (!content.isEmpty()) {
            try {
                int volume = Integer.parseInt(content);
                if (volume >= 200 || volume < 0) {
                    Objects.requireNonNull(event.getMessage()
                            .getChannel().block())
                            .createMessage("Вот скажи мне, ты идиот?").block();
                    return;
                }
                guildMusicManager.player.setVolume(volume);
                Objects.requireNonNull(event.getMessage()
                        .getChannel().block())
                        .createMessage("Меняю.").block();
            } catch (NumberFormatException e) {
                Objects.requireNonNull(event.getMessage()
                        .getChannel().block())
                        .createMessage("Некорректный формат команды.").block();
            }
        }
    }


    private synchronized static void printQueue(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        Objects.requireNonNull(event.getMessage()
                .getChannel().block())
                .createMessage(OutputUtils.printQueue(TrackScheduler.getQueue())).block();
    }

    public synchronized static void printNowPlay(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        String title = guildMusicManager.player.getPlayingTrack().getInfo().title;
        if (title != null) {
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block())
                    .createMessage("♫Сейчас играет♫" + "\n" + title).block();
        } else {
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block())
                    .createMessage("Сейчас ничего не играет.").block();
        }
    }

    private synchronized static void clearQueue(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        guildMusicManager.scheduler.clear();
        Objects.requireNonNull(event.getMessage()
                .getChannel().block())
                .createMessage("Теперь очередь пуста").block();
    }

    private synchronized static void grab(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        final Member member = event.getMember().orElse(null);
        if (member != null) {
            PrivateChannel privateChannel = member.getPrivateChannel().block();
            assert privateChannel != null;
            String title = InputUtils.getValidString(guildMusicManager.player.getPlayingTrack().getInfo().title);
            String uri = guildMusicManager.player.getPlayingTrack().getInfo().uri;
            privateChannel.createMessage("Как ты просил." + "\n" + "Название: " + title + "\n" + uri).block();
        }
    }

    private synchronized static void printHelp(MessageCreateEvent event) {
        Objects.requireNonNull(event.getMessage()
                .getChannel().block())
                .createMessage(OutputUtils.printCommands()).block();
    }

    private synchronized static void printAlive(MessageCreateEvent event) {
        Objects.requireNonNull(event.getMessage()
                .getChannel().block())
                .createMessage("Жив ЭЖЖИ").block();
    }

    private synchronized static void printLyrics(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        String tittle = guildMusicManager.player.getPlayingTrack().getInfo().title;
        String text = Parser.getText(tittle);
        if (!text.isEmpty()) {
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block())
                    .createMessage(tittle + "\n" + "\n" + text).block();
        } else {
            Objects.requireNonNull(event.getMessage()
                    .getChannel().block())
                    .createMessage("Не нашел текст").block();
        }
    }
    private synchronized static void loop(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        guildMusicManager.scheduler.loop();
        Objects.requireNonNull(event.getMessage()
                .getChannel().block())
                .createMessage("Теперь будешь только это слушать").block();
    }
    private synchronized static void unLoop(MessageCreateEvent event) {
        GuildMusicManager guildMusicManager = getGuildPlayerManager(Objects.requireNonNull(event.getGuild().block()));
        if (guildMusicManager.isConnected()) return;
        guildMusicManager.scheduler.unLoop();
        Objects.requireNonNull(event.getMessage()
                .getChannel().block())
                .createMessage("Надоело?").block();
    }
}