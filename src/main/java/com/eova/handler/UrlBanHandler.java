package com.eova.handler;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jfinal.handler.Handler;
import com.jfinal.kit.StrKit;

/**
 * Ban掉禁止访问的资源
 *
 *
 * @创建者：Jieven
 * @创建时间：2017-2-20 下午3:14:06
 */
public class UrlBanHandler extends Handler {

    private Pattern skipedUrlPattern;

    public UrlBanHandler(String skipedUrlRegx, boolean isCaseSensitive) {
        if (StrKit.isBlank(skipedUrlRegx))
            throw new IllegalArgumentException("The para excludedUrlRegx can not be blank.");
        skipedUrlPattern = isCaseSensitive ? Pattern.compile(skipedUrlRegx) : Pattern.compile(skipedUrlRegx, Pattern.CASE_INSENSITIVE);
    }

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
        if (skipedUrlPattern.matcher(target).matches()) {
            try {
                response.sendError(404);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        } else {
            next.handle(target, request, response, isHandled);
        }
    }
}