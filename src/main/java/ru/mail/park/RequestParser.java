package ru.mail.park;

/**
 * Created by Пользователь on 26.03.2017.
 */
public class RequestParser {

    public static String getURI(String request){
        //System.out.println(request);
        final Integer begin = request.indexOf('/');
        ///?|HTTP
        Integer end = request.substring(begin).indexOf("?");
        if(end<0){
            end = request.substring(begin).indexOf("HTTP")-1;
        }
        System.out.println(begin);
        System.out.println(end);
        if(end!=1) {
            return request.substring(begin + 1, begin + end);
        } else {
            return null;
        }
    }

    public static String getMethod(String request){
        final Integer begin = 0;
        final Integer end = request.indexOf(" ");
        return request.substring(0, end);
    }
}
