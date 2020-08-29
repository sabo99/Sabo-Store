package com.sabo.sabostore.Common;

import com.sabo.sabostore.Model.OrderModel;

public interface ILoadOffSetTimeListener {
    void onLoadTimeSuccess(OrderModel orderModel, long offSetTime);
    void onLoadTimeFailed(String message);
}
