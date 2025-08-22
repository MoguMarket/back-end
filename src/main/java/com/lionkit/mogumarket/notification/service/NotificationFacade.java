// notification/service/NotificationFacade.java
package com.lionkit.mogumarket.notification.service;

import java.util.List;
import java.util.Map;

public interface NotificationFacade {

    void groupBuyReached(Long groupBuyId, List<Long> userIds);

    void groupBuyClosingSoon(Long groupBuyId, List<Long> userIds);

    void groupBuyClosed(Long groupBuyId, List<Long> userIds);

    // (옵션) 공용 메서드: 특정 제목/본문/데이터로 사용자들에게 전송
    void notifyUsers(List<Long> userIds, String title, String body, Map<String,String> data);
}