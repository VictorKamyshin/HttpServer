package ru.mail.park;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by victor on 01.04.17.
 */
public class MainThread {

    private static final String ROOTDIRKEY = "-r";

    private static String rootDir = "/home/victor/GURPS/"; //сюда запишем ROOTDIR

    private static final String NUMBEROFTHREADSKEY = "-c";

    private static Integer numberOfThreads;

    private static final Integer PORT = 8080;

    public static ConcurrentLinkedQueue<WorkerThread> freeThreads= new ConcurrentLinkedQueue<WorkerThread>();

    private static ArrayList<WorkerThread> allThreads = new ArrayList<WorkerThread>();

    private static CircleListIndex index;

    public static ConcurrentHashMap<String,HttpResponse> cache= new ConcurrentHashMap<String,HttpResponse>();

    public static void main(String[] args){
        for (Integer i =0; i < args.length; i++) {
            if(ROOTDIRKEY.equals(args[i])){
                try{
                    rootDir=args[i+1];
                } catch(ArrayIndexOutOfBoundsException e){
                    e.printStackTrace();
                    return;
                }
            }

            if(NUMBEROFTHREADSKEY.equals(args[i])){
                try{
                    numberOfThreads=Integer.parseInt(args[i+1]);
                    index = new CircleListIndex(numberOfThreads);
                } catch(ArrayIndexOutOfBoundsException e){
                    e.printStackTrace();
                    return;
                } catch (NumberFormatException e){
                    e.printStackTrace();
                    return;
                }
            }
        }

        try {
            final ServerSocket server = new ServerSocket(PORT,0, InetAddress.getByName("localhost"));
            for(Integer i = 0; i < numberOfThreads; i++){
                allThreads.add(new WorkerThread(i, rootDir)); //Создаем работников и они сами строятся
                //в очередь
            }

            System.out.println("Server started! ");
            while(true){
                final Socket socket = server.accept(); //ждем запросов
                if(!freeThreads.isEmpty()) { //если свободные воркеры есть, то отдаем им
                    final WorkerThread wt = freeThreads.poll();
                    wt.tasks.add(socket);
                    System.out.println("Get request "+ freeThreads.size());
                } else { //если нет - просто раскидываем по кругу
                    System.out.println("No avalialble threads");
                    allThreads.get(index.getIndex()).tasks.add(socket);
                    index.increment();
                }
            }
        } catch(UnknownHostException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }

    }

}
