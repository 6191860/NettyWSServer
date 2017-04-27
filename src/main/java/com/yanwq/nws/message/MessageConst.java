package com.yanwq.nws.message;

/**
 * Created by dodoca_android on 2017/4/27.
 */
public class MessageConst {
    public static final String SEPARATOR = "<$-$>";

    /**
     * @param message received message
     * @return [0]:msg_id,[1]:data
     */
    public static String[] getArray(String message) {
        return message.split(SEPARATOR);
    }

    public static String wrapMsg(String... args) {
        String result = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (i != 0) {
                result += SEPARATOR;
            }
            result += arg;
        }

        return result;
    }
}