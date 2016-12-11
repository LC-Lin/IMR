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

import android.util.Log;

import com.lc.imr.annotation.AtNumbers;
import com.lc.imr.annotation.GroupId;
import com.lc.imr.annotation.IntParam;
import com.lc.imr.annotation.LongParam;
import com.lc.imr.annotation.Nick;
import com.lc.imr.annotation.ReplySender;
import com.lc.imr.annotation.Param;
import com.lc.imr.annotation.ReplyType;
import com.lc.imr.annotation.SendId;
import com.lc.imr.annotation.Message;
import com.lc.imr.annotation.RawOrder;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.lc.imr.IMR.DEFAULT;
import static com.lc.imr.IMR.REPLY_TYPE_WORDS;
import static com.lc.imr.IMR.REPLY_TYPE_XML;

final class RealClient implements Client {

    private final MethodFinder finder;

    private final Order.Factory orderFactory;

    private final Reply.Sender sender;

    private final InfoProvider infoProvider;

    RealClient(ArrayList<Object> objects, Order.Factory orderFactory, Reply.Sender sender) {
        this(objects, orderFactory, sender, null);
    }

    RealClient(ArrayList<Object> objects, Order.Factory orderFactory, Reply.Sender sender, InfoProvider infoProvider) {
        this.finder = new MethodFinder(objects);
        this.orderFactory = orderFactory;
        this.sender = sender;

        if (infoProvider == null) {
            this.infoProvider = new InfoProvider() {
                @Override
                public long getSelfNumber() {
                    return 0;
                }
            };
        } else {
            this.infoProvider = infoProvider;
        }
    }

    @Override
    public void receive(Object source) {
        final Order order = orderFactory.wrapSource(source);
        if (order == null) return;
        finder.asyncFindMethod(order.getMessage(), new MethodFinder.CallBack() {
            @Override
            public void onFound(Method method, Object object) {
                // TODO: 2016/12/10 check permission
                new MethodInvoker(method, object, order).invoke();
            }
        });
    }

    private class MethodInvoker {

        private static final String TAG = "MethodInvoker";

        private final Method method;

        private final Object object;

        private final Order order;

        private MethodInvoker(Method method, Object object, Order order) {
            this.method = method;
            this.object = object;
            this.order = order;
        }

        private void invoke() {
            Object result = null;
            try {
                result = method.invoke(object, matchParams());
            } catch (IllegalAccessException e) {
                Log.e(TAG, "invoke: The method to deal with the message should be public", e);
            } catch (InvocationTargetException e) {
                Log.e(TAG, "invoke: The method to deal with the message throws an exception", e);
            }
            dealWithResult(result);
        }

        private Object[] matchParams() {
            String[] array = order.getMessage().split(" +");
            Annotation[][] mAnnotations = method.getParameterAnnotations();
            int length = mAnnotations.length;
            Object[] params = new Object[length];
            for (int i = 0; i < length; i++) {
                Class annotationType;
                for (Annotation a : mAnnotations[i]) {
                    annotationType = a.annotationType();
                    if (annotationType == IntParam.class) {
                        params[i] = Integer.parseInt(array[((IntParam) a).value()]);
                    } else if (annotationType == LongParam.class) {
                        params[i] = Long.parseLong(array[((LongParam) a).value()]);
                    } else if (annotationType == Param.class) {
                        params[i] = array[((Param) a).value()];
                    } else if (annotationType == Message.class) {
                        params[i] = order.getMessage();
                    } else if (annotationType == RawOrder.class) {
                        params[i] = order;
                    } else if (annotationType == GroupId.class) {
                        params[i] = order.getGroupNumber();
                    } else if (annotationType == SendId.class) {
                        params[i] = order.getSenderNumber();
                    } else if (annotationType == Nick.class) {
                        params[i] = order.getSenderNick();
                    } else if (annotationType == AtNumbers.class) {
                        params[i] = null;// TODO: 2016/12/10 add @ support
                    } else if (annotationType == ReplySender.class) {
                        if (sender == null)
                            throw new IllegalArgumentException("sender == null");
                        params[i] = sender;
                    } else {
                        throw new IllegalArgumentException("The parameter should be marked with the annotation under package com.lc.sqp.annotation");
                    }
                }
            }
            return params;
        }

        private void dealWithResult(final Object result) {
            // TODO: 2016/12/10  wrap reply
            if (result == null)
                throw new IllegalArgumentException("the method must not return null\n" + method);
            if (result instanceof Reply) {
                sender.send((Reply) result);
                return;
            }
            if (result instanceof String) {
                final int replyTypeCode, serviceId;
                ReplyType replyType = method.getAnnotation(ReplyType.class);
                if (replyType == null) {
                    replyTypeCode = DEFAULT;
                    serviceId = 1;
                } else {
                    replyTypeCode = replyType.value();
                    serviceId = replyType.serviceId();
                }
                Reply reply;
                switch (replyTypeCode) {
                    case REPLY_TYPE_WORDS: {
                        reply = new Reply() {
                            @Override
                            public String getContent() {
                                return (String) result;
                            }

                            @Override
                            public long getTargetGroupNumber() {
                                return order.getGroupNumber();
                            }
                        };
                        break;
                    }
                    case REPLY_TYPE_XML: {
                        reply = new XmlReply() {
                            @Override
                            public int getServiceId() {
                                return serviceId;
                            }

                            @Override
                            public String getContent() {
                                return (String) result;
                            }

                            @Override
                            public long getTargetGroupNumber() {
                                return order.getGroupNumber();
                            }
                        };
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("The value of ReplyType annotation should be the constant in the class IMR");
                }
                sender.send(reply);
            }
        }
    }
}