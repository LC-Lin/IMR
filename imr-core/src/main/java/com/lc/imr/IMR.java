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

import java.util.ArrayList;
import java.util.Collections;

public final class IMR {

    public static final int DEFAULT = 0;

    public static final int PERMISSION_NORMAL = DEFAULT;

    public static final int PERMISSION_JUNIOR = 1;

    public static final int PERMISSION_SENIOR = 2;

    public static final int REPLY_TYPE_XML = DEFAULT;

    public static final int REPLY_TYPE_WORDS = 1;

    private IMR() {
        //no instance
    }

    public static String describeOrder(Order order) {
        if (order == null) throw new IllegalArgumentException("order == null");
        return new StringBuilder("OrderInfo:")
                .append("\n    GroupNumber->").append(order.getGroupNumber())
                .append("\n    SenderNumber->").append(order.getSenderNumber())
                .append("\n    SenderNick->").append(order.getSenderNick())
                .append("\n    MessageContent->").append(order.getMessage())
                .toString();
    }

    public static ClientBuilder clientBuilder(){
        return new ClientBuilder();
    }

    public static final class ClientBuilder {
        private ArrayList<Object> objectArrayList = new ArrayList<>();

        private Order.Factory orderFactory;

        private Reply.Sender sender;

        public ClientBuilder registerObject(Object... object){
            for (Object o:object){
                if (o == null) throw new IllegalArgumentException("One of the objects is null");
                if (objectArrayList.contains(o)) throw new IllegalArgumentException("One of the objects has been registered");
            }
            Collections.addAll(objectArrayList, object);
            return this;
        }

        public ClientBuilder setOrderFactory(Order.Factory orderFactory) {
            if (orderFactory == null) throw new IllegalArgumentException("orderFactory == null");
            this.orderFactory = orderFactory;
            return this;
        }

        public ClientBuilder setSender(Reply.Sender sender) {
            if (sender == null) throw new IllegalArgumentException("sender == null");
            this.sender = sender;
            return this;
        }

        public Client build(){
            return new RealClient(objectArrayList,orderFactory,sender);
        }
    }

}