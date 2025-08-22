package com.lionkit.mogumarket.point.service;


import com.lionkit.mogumarket.global.base.response.exception.BusinessException;
import com.lionkit.mogumarket.global.base.response.exception.ExceptionType;
import com.lionkit.mogumarket.payment.entity.PaymentHistory;
import com.lionkit.mogumarket.point.dto.request.PointEarnRequestDto;
import com.lionkit.mogumarket.point.dto.response.PointSnapshotResponseDto;
import com.lionkit.mogumarket.point.entity.Point;
import com.lionkit.mogumarket.point.entity.PointHistory;
import com.lionkit.mogumarket.point.enums.PointEventType;
import com.lionkit.mogumarket.point.repository.PointHistoryRepository;
import com.lionkit.mogumarket.point.repository.PointRepository;
import com.lionkit.mogumarket.user.entity.User;
import com.lionkit.mogumarket.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository historyRepository;
    private final UserRepository userRepository;


    /**
     * 멱등키를 활용하여 이미 처리된 안건인지 확인합니다.
     * @param idemKey
     * @return
     */
    private boolean alreadyProcessed(String idemKey) {
        return idemKey != null && historyRepository.findByIdempotencyKey(idemKey).isPresent();
    }

    /** 영속성 컨텍스트 동기화  */
    private Point mutate(Point p) { return p; }



    /**
     * 유저의 회원가입과 함께 해당 유저의 point wallet 을 생성합니다.
     */
    public Point createPointWallet(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(()->new BusinessException(ExceptionType.USER_NOT_FOUND ));

        Point pointWallet = Point.builder()
                .user(user)
                .balance(0)
                .hold(0)
                .build();

        return pointRepository.save(pointWallet);

    }


    /**
     * point 관련 event 대해 pointHistory 를 생성하므로써 기록을 남깁니다.
     * @param wallet
     * @param type
     * @param delta
     * @param ph
     * @param idemKey
     * @param beforeAvail
     * @param afterAvail
     * @return
     */
    private PointHistory persistHistory(Point wallet,
                                PointEventType type,
                                long delta,
                                PaymentHistory ph,
                                String idemKey,
                                long beforeAvail,
                                long afterAvail) {

            PointHistory h = PointHistory.builder()
                    .point(wallet)
                    .type(type)
                    .paymentHistory(ph) // 비-결제 이벤트면 null
                    .delta(delta)
                    .beforeBalanceSnapshot(beforeAvail)
                    .afterBalanceSnapshot(afterAvail)
                    .idempotencyKey(idemKey)
                    .build();
            return historyRepository.save(h);

    }



    /**
     * 유저의 point 스냅샷 반환
     * @param userId
     * @return
     */
    public Point getSnapshot(Long userId) {
        return pointRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ExceptionType.POINT_WALLET_NOT_FOUND));
    }





    /** 적립(EARN) 처리 */
    public PointSnapshotResponseDto earn(Long userId, long amount, String idemKey, PaymentHistory ph) {

        /**
         * 이미 처리된 안건이면 현재 스냅샷 반환
         */
        if (alreadyProcessed(idemKey)) return PointSnapshotResponseDto.fromEntity( getSnapshot(userId));


        Point pointWallet = getSnapshot(userId);


        long beforeAvail = pointWallet.getBalance() - pointWallet.getHold(); //업데이트 전 사용 가능한 포인트
        long nextBal = pointWallet.getBalance() + amount; // 업데이트 후 총 포인트

        pointWallet = mutate(pointWallet); // 수정 전 동기화 처리
        pointWallet.updateBalance(nextBal);


        long afterAvail = pointWallet.getBalance() - pointWallet.getHold(); // 업데이트 후 사용 가능한 포인트

        persistHistory(pointWallet, PointEventType.EARN, amount, ph, idemKey, beforeAvail, afterAvail); // earn 이벤트에 대한 기록 저장

        return PointSnapshotResponseDto.builder()
                .userId(userId)
                .hold(pointWallet.getHold())
                .balance(pointWallet.getBalance())
                .available(afterAvail).build();
    }

    /**
     *  포인트 차감 시도와 함께 lock 을 겁니다( 해당 포인트량만큼 보류 처리 ).
     *  포인트 차감 시도에 대한 기록을 남깁니다.
     *  (포인트 차감 시도 != 실제 포인트 차감 임을 유의)
     */
    public PointSnapshotResponseDto hold(Long userId, long amount, String idemKey, PaymentHistory ph) {
        /**
         * 이미 처리된 안건이면 현재 스냅샷 반환
         */
        if (alreadyProcessed(idemKey)) return PointSnapshotResponseDto.fromEntity( getSnapshot(userId));

        Point pointWallet = getSnapshot(userId);

        long beforeAvail = pointWallet.getBalance() - pointWallet.getHold();
        if (beforeAvail < amount) throw new BusinessException(ExceptionType.POINT_INSUFFICIENT_AVAILABLE);


        long nextHold =  pointWallet.getHold() + amount; // hold 중인 포인트로 추가

        pointWallet.updateHold(nextHold);

        long afterAvail = pointWallet.getBalance() - pointWallet.getHold();

        persistHistory(pointWallet, PointEventType.LOCK,  -amount, ph, idemKey, beforeAvail, afterAvail);

        return  PointSnapshotResponseDto.builder()
                .userId(userId)
                .hold(pointWallet.getHold())
                .balance(pointWallet.getBalance())
                .available(afterAvail).build();
    }

    /**
     * 포인트 차감 실패/취소 상황에서 사용합니다.
     * lock 을 해제(UN_LOCK)하며, 복구에 대한 기록을 남깁니다.
     */
    public PointSnapshotResponseDto release(Long userId,
                                            long amount, // 복구 금액
                                            String idemKey, PaymentHistory ph) {
        /**
         * 이미 처리된 안건이면 현재 스냅샷 반환
         */
        if (alreadyProcessed(idemKey)) return PointSnapshotResponseDto.fromEntity( getSnapshot(userId));


        Point pointWallet = getSnapshot(userId);
        if (pointWallet.getHold() < amount) throw new BusinessException(ExceptionType.POINT_HOLD_INSUFFICIENT);

        long beforeAvail = pointWallet.getBalance() - pointWallet.getHold();

        pointWallet.updateHold(pointWallet.getHold() - amount);  // 해당 amount 대해 걸려있던 lock 해제

        long afterAvail = pointWallet.getBalance() - pointWallet.getHold();

        persistHistory(pointWallet, PointEventType.UN_LOCK, (int) amount, ph, idemKey, beforeAvail, afterAvail);

        return PointSnapshotResponseDto.builder()
                .userId(userId)
                .hold(pointWallet.getHold())
                .balance(pointWallet.getBalance())
                .available(afterAvail).build();
    }

    /**
     * 포인트 최종 차감 상황에서 사용합니다.
     * 실제로 포인트를 총 보유포인트량에서 차감하며, 보류 상태인 포인트를 해제(UN_LOCK)하고, 실제 포인트 차감에 대한 기록을 남깁니다.
     */
    public PointSnapshotResponseDto burn(Long userId, long amount, String idemKey, PaymentHistory ph) {
        /**
         * 이미 처리된 안건이면 현재 스냅샷 반환
         */
        if (alreadyProcessed(idemKey)) return PointSnapshotResponseDto.fromEntity( getSnapshot(userId));

        Point pointWallet = getSnapshot(userId);
        if (pointWallet.getHold() < amount) throw new BusinessException(ExceptionType.POINT_HOLD_INSUFFICIENT);
        if (pointWallet.getBalance() < amount) throw new BusinessException(ExceptionType.POINT_BALANCE_INSUFFICIENT);

        long beforeAvail = pointWallet.getBalance() - pointWallet.getHold();

        pointWallet = mutate(pointWallet); // 동기화

        /**
         * payment 관련 차감이기에 이전에 lock 을 걸었던 경우
         * 해당 amount 대해 걸려있던 lock 을 해제
         */
        if (ph == null) pointWallet.updateHold(pointWallet.getHold() - amount);

        pointWallet.updateBalance(pointWallet.getBalance() - amount); // 보유 포인트 총량에서 실제로 차감 적용

        long afterAvail = pointWallet.getBalance() - pointWallet.getHold();

        persistHistory(pointWallet, PointEventType.REDEEM, amount, ph, idemKey, beforeAvail, afterAvail);

        return PointSnapshotResponseDto.builder()
                .userId(userId)
                .hold(pointWallet.getHold())
                .balance(pointWallet.getBalance())
                .available(afterAvail).build();
    }







}
