/**
 *   Copyright 2016 LC-Lin
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.lc.imr;

import com.lc.imr.annotation.TargetMsg;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Cecil Lin on 2016/12/10.
 */

final class MethodFinder {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final ArrayList<Object> objects;

    MethodFinder(ArrayList<Object> objects) {
        this.objects = objects;
    }

    Map<Object, List<Method>> findMethods(String message) {
        Map<Object, List<Method>> map = new HashMap<>();
        TargetMsg targetMsg;
        for (Object object : objects) {
            for (Method method : object.getClass().getDeclaredMethods()) {
                targetMsg = method.getAnnotation(TargetMsg.class);
                if (targetMsg == null) continue;
                if (!checkMessage(targetMsg, message)) continue;
                if (map.containsKey(object)){
                    map.get(object).add(method);
                }else {
                    ArrayList<Method> list = new ArrayList<>();
                    list.add(method);
                    map.put(object,list);
                }
            }
        }
        return map;
    }

    void asyncFindMethod(String message, final CallBack callBack) {
        TargetMsg targetMsg;
        for (final Object object : objects) {
            for (final Method method : object.getClass().getDeclaredMethods()) {
                targetMsg = method.getAnnotation(TargetMsg.class);
                if (targetMsg == null) continue;
                if (!checkMessage(targetMsg, message)) continue;
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onFound(method, object);
                    }
                });
            }
        }
    }

    interface CallBack {
        void onFound(Method method, Object object);
    }

    private boolean checkMessage(TargetMsg targetMsg, String message) {
        return message.matches(targetMsg.value()) || targetMsg.value().equals("");
    }
}