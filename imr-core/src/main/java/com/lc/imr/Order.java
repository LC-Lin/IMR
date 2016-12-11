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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * The interface Order.
 */
public interface Order {

    /**
     * Gets message.
     *
     * @return the message
     */
    @NonNull
    String getMessage();

    /**
     * Gets group number.
     *
     * @return the group number
     */
    long getGroupNumber();

    /**
     * Gets sender number.
     *
     * @return the sender number
     */
    long getSenderNumber();

    /**
     * Gets sender nick.
     *
     * @return the sender nick
     */
    @NonNull
    String getSenderNick();

    /**
     * The interface Factory.
     */
    interface Factory {
        /**
         * 如果可行就将�?个对象包装成{@link Order}<br/>
         * 如果不可行就返回null
         *
         * @param source 希望被包装的对象
         * @return �?个Order对象或�?�null order
         */
        @Nullable
        Order wrapSource(Object source);// TODO: 2016/12/10 check null and unsupported type
    }

}