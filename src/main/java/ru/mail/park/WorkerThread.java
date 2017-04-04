package ru.mail.park;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Пользователь on 25.03.2017.
 */
public class WorkerThread extends Thread {

    private String rootDir; //сюда запишем ROOTDIR

    private int myId;

    private ConcurrentLinkedQueue<Socket> tasks = new ConcurrentLinkedQueue<>();

    public void addTask(Socket socket){
        tasks.add(socket);
    }


    public WorkerThread(int id, String rootDir){
        this.myId = id;
        this.rootDir = rootDir;


        MainThread.addFreeWorker(this);
        //System.out.println("I am thread with id "+myId+". Ready to work. "+MainThread.freeThreads.isEmpty());
        setDaemon(true);
        setPriority(NORM_PRIORITY);
        start();
    }

    @Override
    public void run(){
        while(true){
            if(!tasks.isEmpty()) {
                //System.out.println("I am thread with id "+myId+" and i'm gonna perform this taks.");
                performTask(tasks.poll());
                try{
                    Thread.sleep(100);
                } catch(InterruptedException e){
                    e.printStackTrace();
                }
                MainThread.addFreeWorker(this);

            }

        }
    }

    private void performTask(Socket s){

        try{
            final InputStream is = s.getInputStream();
            final OutputStream os = s.getOutputStream();

            final byte[] buf = new byte [64*1024];

            final int r = is.read(buf);

            String data = new String(buf,0,r);

            data= URLDecoder.decode(data, "UTF-8");

            //System.out.println(data);

            final String uri = RequestParser.getURI(data);

            if(uri!=null) {
                if (MainThread.getFromCache(uri) != null) {
                    final HttpResponse response = MainThread.getFromCache(uri);
                    response.setRequestMethod(RequestParser.getMethod(data));
                    response.setDate();

                    os.write(response.getAsBytes());
                    //System.out.println("Get response from cache");
                } else {
                    final HttpResponse response = new HttpResponse();
                    response.setRequestMethod(RequestParser.getMethod(data));
                    response.setDate();
                    response.setBodyByPath(rootDir, uri);

                    os.write(response.getAsBytes());

                    MainThread.addToCache(uri, response);
                    //System.out.println("Get not response from cache");
                }
            } else {
                final HttpResponse response = new HttpResponse();
                response.setRequestMethod(RequestParser.getMethod(data));
                response.setDate();
                response.setBodyByPath(rootDir, null);

                os.write(response.getAsBytes());
                //System.out.println("Get not response from cache");
            }


            s.close();
        } catch(Exception e){

            System.out.println("UNHANDLED EXCEPTION");
            e.printStackTrace();
        }

    }
}
