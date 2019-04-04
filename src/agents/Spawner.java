package agents;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class Spawner {
    public static void main(String[] args){

        Runtime rt = Runtime.instance();
        Profile p1 = new ProfileImpl();
        //p1.setParameter(...);
        ContainerController mainContainer = rt.createMainContainer(p1);

        //QUANTIDADE DE PRODUTOS
        String[] produtos = new String[10];
        for(int i = 0;i<produtos.length;i++){
            produtos[i] = "produto_" + i;
        }

        //QUANTIDADE DE FARMÁCIAS
        int[] pharms = IntStream.rangeClosed(1, 5).toArray();
        for (int pharm : pharms)
        {
            int x = ThreadLocalRandom.current().nextInt(0, 100 + 1);
            int y = ThreadLocalRandom.current().nextInt(0, 100 + 1);
            Object[] agentArgs = {x,y,produtos};
            AgentController ac;
            try {
                ac = mainContainer.createNewAgent("pharm_" + pharm,"agents.Pharm", agentArgs);
                ac.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }

        //QUANTIDADE DE CLIENTES
        int[] clients = IntStream.rangeClosed(1, 1000).toArray();
        for(int client : clients){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Object[] agentArgs = {produtos};
            AgentController ac1;
            try {
                ac1 = mainContainer.createNewAgent("client_" + client,"agents.Client", agentArgs);
                ac1.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }


    }
}
