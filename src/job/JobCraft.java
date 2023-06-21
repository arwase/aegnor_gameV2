package job;

import client.Player;
import common.SocketManager;

public class JobCraft {

    public Player player;
    public Thread thread;
    public JobAction jobAction;
    private int time = 0;
    private boolean itsOk = true;

    public JobCraft(JobAction jobAction, Player player) {
        this.jobAction = jobAction;
        this.player = player;

        this.thread = new Thread(() -> {
            try { Thread.sleep(150); } catch(Exception ignored) {
                ignored.printStackTrace();
            }
            try {
                if (itsOk)
                {
                    jobAction.craft(false, -1);
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
            try { Thread.sleep(150); } catch(Exception ignored) { }
            if (!itsOk) repeat(time+1, time, player);
        });
        this.thread.start();
    }

    public void setAction(int time) {
        this.time = time;
        this.jobAction.broken = false;
        this.itsOk = false;
    }

    public void repeat(final int time1, final int time2, final Player player) {
        this.player.sendMessage("La création est instantannée, cela peut créer un bug de l'interface");

        this.jobAction.player = player;
        this.jobAction.isRepeat = true;
        boolean isOneShotCraft = false;
        if (this.jobAction.broke || this.jobAction.broken || player.getExchangeAction() == null || !player.isOnline()) {
            if (player.getExchangeAction() == null)
                this.jobAction.broken = true;
            if (player.isOnline())
                SocketManager.GAME_SEND_Ea_PACKET(this.jobAction.player, this.jobAction.broken ? "2" : "4");
            this.end();
            return;
        } else {
                SocketManager.GAME_SEND_EA_PACKET(this.jobAction.player, 0 + "");
                this.jobAction.craft(this.jobAction.isRepeat, time1-1);
                isOneShotCraft = true;
        }

        if (time2 <= 0 || isOneShotCraft) this.end();
        else {
            try { Thread.sleep(150); } catch(Exception ignored) { }
            this.repeat(time1, (time2 - 1), player);
        }
    }

    public void end() {
        SocketManager.GAME_SEND_Ea_PACKET(this.jobAction.player, "1");
        if (!this.jobAction.data.isEmpty())
            SocketManager.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.jobAction.player, 'O', "+", this.jobAction.data);

        this.jobAction.ingredients.clear();
        this.jobAction.isRepeat = false;
        this.jobAction.setJobCraft(null);
        this.thread.interrupt();
    }
}