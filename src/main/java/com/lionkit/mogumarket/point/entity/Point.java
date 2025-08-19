package com.lionkit.mogumarket.point.entity;

import com.test.oauth.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Point {

    @Version
    private Long version; // 낙관적 락

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    /**
     * 다른 창/주문에서 결제 진행중이라 lock 을 걸어둔 포인트를 의미합니다.
     */
    @Column(nullable=false)
    private Integer hold;

    /**
     * 보유한 총 포인트를 의미합니다.
     * 즉,  balance == lock 되지 않은 포인트 + lock 된 포인트 .
     * 즉, 현재 사용 가능한 포인트 == lock 되지 않은 포인트  == balance - hold.
     */
    @Column(nullable=false)
    private Integer balance;




}
