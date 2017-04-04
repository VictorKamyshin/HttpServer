package ru.mail.park;

/**
 * Created by Пользователь on 26.03.2017.
 */
public class RequestParser {

    public static String getURI(String request){
        try {
            final Integer begin = request.indexOf('/');
            Integer end;
            if (begin >= 0) {
                end = request.substring(begin).indexOf('?');
            } else {
                end = request.indexOf('?');
            }
            if (end < 0) {
                end = request.substring(begin).indexOf("HTTP") - 1;
            }
            if (end != 1) {
                return request.substring(begin + 1, begin + end);
            } else {
                return null;
            }
        }catch(StringIndexOutOfBoundsException e){
            return null;
        }
    }

    public static String getMethod(String request){
        try {
            final Integer end = request.indexOf(' ');
            return request.substring(0, end);
        }catch(StringIndexOutOfBoundsException e){
            return "POST";
        }
    }
}
