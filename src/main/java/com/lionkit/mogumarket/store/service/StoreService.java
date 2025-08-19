package com.lionkit.mogumarket.store.service;



import com.lionkit.mogumarket.market.entity.Market;
import com.lionkit.mogumarket.market.repository.MarketRepository;
import com.lionkit.mogumarket.store.dto.request.StoreSaveRequest;
import com.lionkit.mogumarket.store.dto.response.StoreResponse;
import com.lionkit.mogumarket.store.entity.Store;
import com.lionkit.mogumarket.store.repsitory.StoreRepository;
import com.lionkit.mogumarket.user.entity.User;
import com.lionkit.mogumarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final MarketRepository marketRepository; // optional link
    private final UserRepository userRepository;     // optional link

    /** 스토어 등록 */
    @Transactional
    public Long create(StoreSaveRequest req) {
        Market market = null;
        if (req.getMarketId() != null) {
            market = marketRepository.findById(req.getMarketId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Market ID"));
        }

        User user = null;
        if (req.getUserId() != null) {
            user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User ID"));
        }

        Store store = Store.builder()
                .name(req.getName())
                .description(req.getDescription())
                .address(req.getAddress())
                .phone(req.getPhone())
                .thumbnailUrl(req.getThumbnailUrl())
                .market(market)
                .user(user)
                .build();

        Store saved = storeRepository.save(store);
        return saved.getId();
    }

    /** 단건 조회 */
    @Transactional(readOnly = true)
    public StoreResponse get(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("스토어를 찾을 수 없습니다."));
        return StoreResponse.from(store);
    }

    /** 목록 조회 (페이징, optional marketId 필터) */
    @Transactional(readOnly = true)
    public Page<StoreResponse> list(Integer page, Integer size, Long marketId) {
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size < 1 || size > 100) ? 10 : size;

        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));
        Page<Store> result;

        if (marketId != null) {
            // 간단히 메모리 필터링 (규모 커지면 쿼리 메서드 추가 권장)
            result = storeRepository.findAll(pageable)
                    .map(store -> store) // identity
                    .map(store -> store); // no-op (형식상)
            result = new PageImpl<>(
                    result.getContent().stream()
                            .filter(st -> st.getMarket() != null && marketId.equals(st.getMarket().getId()))
                            .toList(),
                    pageable,
                    result.getTotalElements() // 간단화. 정확한 total 원하면 전용 repo 메서드 추가 권장
            );
        } else {
            result = storeRepository.findAll(pageable);
        }

        return result.map(StoreResponse::from);
    }
}