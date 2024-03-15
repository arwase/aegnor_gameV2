package game;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import client.Account;
import client.Player;
import com.sun.istack.NotNull;
import database.Database;
import exchange.ExchangeClient;
import game.world.World;
import kernel.Config;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class GameServer {

    public static short MAX_PLAYERS = 700;
    public static GameServer INSTANCE = new GameServer();

    private final static @NotNull
    ArrayList<Account> waitingClients = new ArrayList<>();
    private final static @NotNull Logger log = (Logger) LoggerFactory.getLogger(GameServer.class);

    private final @NotNull IoAcceptor acceptor;

   // static {

    //}

    private GameServer(){
        acceptor = new NioSocketAcceptor();
        TextLineCodecFactory line = new TextLineCodecFactory(StandardCharsets.UTF_8, LineDelimiter.NUL, new LineDelimiter("\n\0"));
        line.setDecoderMaxLineLength(16384);
        acceptor.getFilterChain().addLast("codec",  new ProtocolCodecFilter(line));
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 60 * 10 /*10 Minutes*/);
        acceptor.setHandler(new GameHandler());
    }



    public static String getServerTime() {
        return "BT" + (new Date().getTime() + 3600000 * 2);
    }

    public boolean start() {
        log.setLevel(Level.ALL);
        if (acceptor.isActive()) {
            log.warn("Error already start but try to launch again");
            return false;
        }

        try {
            acceptor.bind(new InetSocketAddress(Config.INSTANCE.getGamePort()));
            log.info("Game server started on address : {}:{}", Config.INSTANCE.getIp(), Config.INSTANCE.getGamePort());
            return true;
        } catch (IOException e) {
            log.error("Error while starting game server", e);
            return false;
        }
    }

    public void stop() {
        if (!acceptor.isActive()) {
            acceptor.getManagedSessions().values().stream()
                    .filter(session -> session.isConnected() || !session.isClosing())
                    .forEach(session -> session.closeNow());
            acceptor.dispose();
            acceptor.unbind();
        }

        log.error("The game server was stopped.");
    }

    public static List<GameClient> getClients() {
        return INSTANCE.acceptor.getManagedSessions().values().stream()
                .filter(session -> session.getAttribute("client") != null)
                .map(session -> (GameClient) session.getAttribute("client"))
                .collect(Collectors.toList());
    }

    public static int getPlayersNumberByIp() {
        return (int) getClients().stream().filter(client -> client != null && client.getAccount() != null)
                .map(client -> client.getAccount().getCurrentIp())
                .distinct().count();
    }

    public void setState(int state) {
        ExchangeClient.INSTANCE.send("SS" + state);
    }

    public static Account getAndDeleteWaitingAccount(int id){
        Iterator<Account> it = waitingClients.listIterator();
        while(it.hasNext()){
            Account account = it.next();
            if(account.getId() == id){
                it.remove();
                return account;
            }
        }
        return null;
    }

    public static void addWaitingAccount(Account account) {
        if(!waitingClients.contains(account)) waitingClients.add(account);
    }

    public static void a(String err) {
        log.warn("Unexpected behaviour detected : "+ err);
    }

    public void kickAll(boolean kickGm) {




        for (Player player : World.world.getOnlinePlayers()) {

            if (player != null && player.getGameClient() != null) {
                if (player.getGroupe() != null && !player.getGroupe().isPlayer() && kickGm)
                    continue;
                Database.getStatics().getPlayerData().update(player);
                player.send("M04");
                player.getGameClient().kick();
            }
        }

        for (Account client : waitingClients){
            client.getGameClient().kick();
        }

    }




}
