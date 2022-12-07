package command;


import client.Player;
import command.administration.AdminUser;
import common.SocketManager;
import game.world.World;
import jakarta.xml.bind.DatatypeConverter;
import kernel.Main;
import util.lang.Lang;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

public class AzuriomCommands implements Runnable {

    private ServerSocket listen_socket;
    protected final static String KEYSTORE_PASSWORD = "SUPERCOMPLICATEDPASSWORD148936257412356";
    protected final static String CERTIFICATE_PATH = "/home/aegnor/key/server.pem";


    public AzuriomCommands(int port)
    {
        try {
            listen_socket = PEMImporter.createSSLFactory(new File(CERTIFICATE_PATH), KEYSTORE_PASSWORD).createServerSocket(port);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    @Override
    public void run() {
        while (true) {
            try{
                (new Thread(new ClientSocket(listen_socket.accept()))).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        (new Thread(this)).start();
    }

    /**
     * https://stackoverflow.com/a/48173910
     */
    private static class PEMImporter {

        public static SSLServerSocketFactory createSSLFactory(File certificatePem, String password) throws Exception {
            final SSLContext context = SSLContext.getInstance("TLS");
            final KeyStore keystore = createKeyStore(certificatePem, password);
            final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keystore, password.toCharArray());
            final KeyManager[] km = kmf.getKeyManagers();
            context.init(km, null, null);
            return context.getServerSocketFactory();
        }

        public static KeyStore createKeyStore(File certificatePem, final String password)
                throws Exception, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
            final X509Certificate[] cert = createCertificates(certificatePem);
            final KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(null);
            final PrivateKey key = createPrivateKey(certificatePem);
            keystore.setKeyEntry(certificatePem.getName(), key, password.toCharArray(), cert);
            keystore.setCertificateEntry("server", cert[0]);
            return keystore;
        }

        private static PrivateKey createPrivateKey(File privateKeyPem) throws Exception {
            final BufferedReader r = new BufferedReader(new FileReader(privateKeyPem));
            String s = r.readLine();
            while (!s.contains("BEGIN PRIVATE KEY")) {
                s = r.readLine();
                if (s == null){
                    r.close();
                    throw new IllegalArgumentException("No PRIVATE KEY found");
                }
            }

            final StringBuilder b = new StringBuilder();
            s = "";
            while (s != null) {
                if (s.contains("END PRIVATE KEY")) {
                    break;
                }
                b.append(s);
                s = r.readLine();
            }
            r.close();
            final String hexString = b.toString();
            final byte[] bytes = DatatypeConverter.parseBase64Binary(hexString);
            return generatePrivateKeyFromDER(bytes);
        }

        private static X509Certificate[] createCertificates(File certificatePem) throws Exception {
            final List<X509Certificate> result = new ArrayList<X509Certificate>();
            final BufferedReader r = new BufferedReader(new FileReader(certificatePem));

            String s = r.readLine();
            while (!s.contains("BEGIN CERTIFICATE")) {
                s = r.readLine();
                if (s == null){
                    r.close();
                    throw new IllegalArgumentException("No CERTIFICATE found");
                }
            }

            StringBuilder b = new StringBuilder();
            while (s != null) {
                if (s.contains("END CERTIFICATE")) {
                    String hexString = b.toString();
                    final byte[] bytes = DatatypeConverter.parseBase64Binary(hexString);
                    X509Certificate cert = generateCertificateFromDER(bytes);
                    result.add(cert);
                    b = new StringBuilder();
                } else {
                    if (!s.startsWith("----")) {
                        b.append(s);
                    }
                }
                s = r.readLine();
            }
            r.close();

            return result.toArray(new X509Certificate[result.size()]);
        }

        private static RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
            final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            final KeyFactory factory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) factory.generatePrivate(spec);
        }

        private static X509Certificate generateCertificateFromDER(byte[] certBytes) throws CertificateException {
            final CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
        }

    }

    private class ClientSocket implements Runnable
    {
        private Socket socket;
        private boolean timerStart = false;
        private Timer timer;

        public ClientSocket(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            BufferedReader in;
            try {
                in = new BufferedReader (new InputStreamReader(this.socket.getInputStream()));
                String command = null;
                while ((command = in.readLine()) != null) {
                    System.out.println("Command : "+command);
                    this.parseCommand(command.split(" "));
                }
            } catch (Exception e) {
                System.out.println("Ici -" +  e.getMessage() );
            }
        }

        private void parseCommand(String[] command){
            switch (command[0].toLowerCase()) {
                case "hello":
                    for(Player player : World.world.getOnlinePlayers()) {
                        player.sendServerMessage(Lang.get(player, 13));
                    }
                    break;
                case "give":
                    //Command is : give playerId itemId quantity
                    //giveCommand(Integer.valueOf(command[1]), Integer.valueOf(command[2]), Integer.valueOf(command[3]));
                    break;
                case "reboot":
                    //Command is : give playerId itemId quantity
                    reboot(Integer.valueOf(command[1]));
                    //giveCommand(Integer.valueOf(command[1]), Integer.valueOf(command[2]), Integer.valueOf(command[3]));
                    break;
                default:
                    break;
            }
        }

        private void reboot(int min){
            int time = 30, OffOn = 0;
            try {
                time = min;
            } catch (Exception e) {
                // ok
            }

            if (OffOn == 1 && this.isTimerStart())// demande de demarer le reboot
            {
                System.out.println("Reboot deja en cours");
            } else if (OffOn == 1 && !this.isTimerStart()) {
                if (time <= 5) {
                    for(Player player : World.world.getOnlinePlayers()) {
                        player.sendServerMessage(Lang.get(player, 14));
                        player.send("M13");
                    }
                    Main.INSTANCE.setFightAsBlocked(true);
                }
                this.setTimer(createTimer(time));
                this.getTimer().start();
                this.setTimerStart(true);
                String timeMSG = "minutes";
                if (time <= 1)
                    timeMSG = "minute";
                SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;" + time + " " + timeMSG);
                System.out.println("Reboot programmé.");
            } else if (OffOn == 0 && this.isTimerStart()) {
                this.getTimer().stop();
                this.setTimerStart(false);
                for(Player player : World.world.getOnlinePlayers())
                    player.sendServerMessage(Lang.get(player, 15));
                Main.INSTANCE.setFightAsBlocked(false);
                System.out.println("Reboot arrêté.");
            } else if (OffOn == 0 && !this.isTimerStart()) {
                System.out.println("Aucun reboot n'est lancé.");
            }

        }

        // POUR LA GESTION DES REBOOT
        public boolean isTimerStart() {
            return timerStart;
        }

        public void setTimerStart(boolean timerStart) {
            this.timerStart = timerStart;
        }

        public Timer getTimer() {
            return timer;
        }

        public void setTimer(Timer timer) {
            this.timer = timer;
        }

        public Timer createTimer(final int timer) {
            ActionListener action = new ActionListener() {
                int time = timer;

                public void actionPerformed(ActionEvent event) {
                    time = time - 1;
                    if (time == 1)
                        SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;" + time + " minute");
                    else
                        SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;" + time + " minutes");
                    if (time <= 0) Main.INSTANCE.stop("Shutdown by an administrator");
                }
            };
            return new Timer(60000, action);
        }

    }
}