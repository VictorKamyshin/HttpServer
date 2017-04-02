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

    public ConcurrentLinkedQueue<Socket> tasks = new ConcurrentLinkedQueue<Socket>();

    public WorkerThread(int id, String rootDir){
        this.myId = id;
        this.rootDir = rootDir;


        MainThread.freeThreads.add(this);
        System.out.println("I am thread with id "+myId+". Ready to work. "+MainThread.freeThreads.isEmpty());
        setDaemon(true);
        setPriority(NORM_PRIORITY);
        start();
    }

    @Override
    public void run(){
        while(true){
            if(!tasks.isEmpty()) {
                System.out.println("I am thread with id "+myId+" and i'm gonna perform this taks.");
                performTask(tasks.poll());
                try{
                    Thread.sleep(100);
                } catch(InterruptedException e){
                    e.printStackTrace();
                }
                if(!MainThread.freeThreads.contains(this)) {
                    MainThread.freeThreads.add(this);
                }
            }

        }
    }

    private void performTask(Socket s){

        try{
            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();

            byte buf[] = new byte [64*1024];

            int r = is.read(buf);

            String data = new String(buf,0,r);

            data= URLDecoder.decode(data, "UTF-8");

            System.out.println(data);

            String uri = RequestParser.getURI(data);

            final HttpResponse response = new HttpResponse();
            response.setStatus(200);

            final String method = RequestParser.getMethod(data);
            //System.out.println(method);
            response.setRequestMethod(method);

            if(uri!=null) {
                if (uri.equals("favicon.ico")) {
                    s.close();
                    return;
                }
            }

            response.setBodyByPath(rootDir, uri);

            os.write(response.getAsBytes());

            s.close();
            System.out.println("I am thread with id "+myId+" and i'm finish my work. I have " +
                    tasks.size() + " another tasks.");
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
