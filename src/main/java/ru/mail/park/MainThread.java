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

    private static String rootDir; //сюда запишем ROOTDIR

    private static final String NUMBEROFTHREADSKEY = "-c";

    private static Integer numberOfThreads;

    private static final Integer PORT = 80;

    public static ConcurrentLinkedQueue<WorkerThread> freeThreads= new ConcurrentLinkedQueue<WorkerThread>();

    private static ArrayList<WorkerThread> allThreads = new ArrayList<WorkerThread>();

    private static CircleListIndex index;

    private static ConcurrentHashMap<String,HttpResponse> cache= new ConcurrentHashMap<String,HttpResponse>();

    public static void addFreeWorker(WorkerThread freeWorker){
        if(!freeThreads.contains(freeWorker)) {
            freeThreads.add(freeWorker);
        }
    }

    public static void addToCache(String request,HttpResponse response){
        cache.put(request, response);
    }

    public static HttpResponse getFromCache(String request){
        return cache.get(request);
    }

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
                } catch(ArrayIndexOutOfBoundsException|NumberFormatException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        try(final ServerSocket server = new ServerSocket(PORT,0, InetAddress.getByName("localhost"))) {
            /*for(Integer i = 0; i < numberOfThreads; i++){
                allThreads.add(new WorkerThread(i, rootDir)); //Создаем работников и они сами строятся
                //в очередь
            }*/

            System.out.println("Server started! ");
            while(true){
                final Socket socket = server.accept(); //ждем запросов
                new WorkerThread(0, rootDir, socket);
                /*if(!freeThreads.isEmpty()) { //если свободные воркеры есть, то отдаем им
                    final WorkerThread wt = freeThreads.poll();
                    wt.addTask(socket);
                } else { //если нет - просто раскидываем по кругу
                    allThreads.get(index.getIndex()).addTask(socket);
                    index.increment();
                }*/
            }
        } catch(UnknownHostException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }

    }

}
