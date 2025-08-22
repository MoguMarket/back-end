// notification/service/impl/NotificationTemplateResolverImpl.java
package com.lionkit.mogumarket.notification.service.Impl;

import com.lionkit.mogumarket.notification.service.NotificationTemplateResolver;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationTemplateResolverImpl implements NotificationTemplateResolver {

    @Override
    public Template groupBuyReached(Long groupBuyId) {
        return new Template(
                "공동구매 목표 달성!",
                "지금 결제가 가능해요. 놓치지 마세요 👀",
                Map.of(
                        "type","GROUPBUY",
                        "event","REACHED",
                        "groupBuyId", String.valueOf(groupBuyId),
                        "deeplink","/groupbuy/" + groupBuyId
                )
        );
    }

    @Override
    public Template groupBuyClosingSoon(Long groupBuyId) {
        return new Template(
                "마감 임박",
                "곧 공동구매가 종료됩니다. 마지막 기회를 잡아보세요!",
                Map.of(
                        "type","GROUPBUY",
                        "event","CLOSING_SOON",
                        "groupBuyId", String.valueOf(groupBuyId),
                        "deeplink","/groupbuy/" + groupBuyId
                )
        );
    }

    @Override
    public Template groupBuyClosed(Long groupBuyId) {
        return new Template(
                "공동구매 종료",
                "해당 공동구매가 종료되었습니다.",
                Map.of(
                        "type","GROUPBUY",
                        "event","CLOSED",
                        "groupBuyId", String.valueOf(groupBuyId),
                        "deeplink","/groupbuy/" + groupBuyId
                )
        );
    }
}