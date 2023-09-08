package command;


import client.Player;
import common.SocketManager;
import game.scheduler.entity.WorldSave;
import game.world.World;
import jakarta.xml.bind.DatatypeConverter;
import kernel.Config;
import kernel.Main;
import util.lang.Lang;
import game.GameClient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
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
    protected final static String CERTIFICATE_PATH = "server.pem";
    //protected final static String CERTIFICATE_PATH = "C:\\wamp64\\www\\server.pem";


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
        public boolean timerStart = false;
        public Timer timer;

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
                    this.parseCommand(command.split(" "));
                }
            } catch (Exception e) {
                //System.out.println("Ici -" +  e.getMessage() );
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
                case "save":
                    //Command is : give playerId itemId quantity
                   save(Integer.valueOf(command[1]));
                    break;
                case "reboot":
                    //Command is : give playerId itemId quantity
                    reboot(Integer.valueOf(command[1]),Integer.valueOf(command[2]));
                    //giveCommand(Integer.valueOf(command[1]), Integer.valueOf(command[2]), Integer.valueOf(command[3]));
                    break;
                default:
                    break;
            }
        }

        public void reboot(int launch,int min){
            World.world.reboot(launch,min);
        }

        private void save(int playerID){
            Player player = World.world.getPlayer(playerID);
            WorldSave.cast(1);
            String mess = "Sauvegarde lancée!";
            player.sendMessage(mess);
        }


    }
}